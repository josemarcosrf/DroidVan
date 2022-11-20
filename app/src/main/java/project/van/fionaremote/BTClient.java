package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;


// The callback interface
interface BTCallbackInterface {
    void onComplete(JSONObject result);
}

public class BTClient {

    // TODO: Handle reconnections
    // TODO: Each `notify` call should have a function ID to handle responses accordingly

    // Bluetooth
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;
    private UUID connUUID;
    // Threading
    private final Executor executor;
    private final Handler resultHandler;
    // Other
    private static final String TAG = "FionaBTClient";

    public BTClient(Executor executor, Handler resultHandler) {
        Log.d(TAG, "BT Client init");
        this.executor = executor;
        this.resultHandler = resultHandler;
    }

    public boolean findDevice(String devicePairName) {
        // TODO: How do we start the activity when is not here?
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            throw new RuntimeException("Device doesn't support Bluetooth :(");
        } else if (!btAdapter.isEnabled()) {
            // TODO: Handle this with a special callback
            Log.w(TAG, "Adapter is not enabled");
            /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 0;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);*/
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Found BT device: " + deviceName + " @" + deviceHardwareAddress);

                if (deviceName.equals(devicePairName)) {
                    Log.i(TAG, "Device " + devicePairName + " found!");
                    btDevice = device;
                    return true;
                }
            }
        }

        Log.w(TAG, "Couldn't find any BT device!");
        return false;
    }

    private void blockingConnect(UUID uuid) throws IOException {
        btAdapter.cancelDiscovery();
        Log.d(TAG, "BTSocket is null: " + (btSocket == null));
        if (btSocket == null) {
            try {
                Log.d(TAG, "Creating bt socker RFCOMM");
                btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
                connUUID = uuid;
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }
        if (btSocket.isConnected()) Log.i(TAG, "BT Socket already connected");
        else {
            Log.d(TAG, "Connecting to RPI UUID: " + uuid);
            btSocket.connect();
        }
    }

    private void notifyResult(JSONObject payload, BTCallback callback) {
        resultHandler.post(() -> {
            callback.onComplete(payload);
        });
    }

    public void pairWith(String deviceName, BTCallback callback) {
        executor.execute(() -> {
            try {
                if (findDevice(deviceName)) {
                    notifyResult(buildPayload(true, "BT Device found"), callback);
                } else {
                    notifyResult(buildPayload(false, "Device not found!"), callback);
                }
            } catch (RuntimeException re) {
                Log.e(TAG, "Runtime exception while pairing: " + re);
                notifyResult(buildPayload(false, String.valueOf(re)), callback);
            } catch (Exception e) {
                Log.e(TAG, "Other exception while pairing: " + e);
            }
        });
    }

    public void connect(UUID uuid, BTCallback callback) {
        // Equivalent to 'new Runnable() { .... }
        // But here we use a lambda function instead
        // For more info see:
        // https://developer.android.com/guide/background/threading#creating-multiple-threads
        executor.execute(() -> {
            try {
                blockingConnect(uuid);
                notifyResult(buildPayload(true, "Connection OK :)"), callback);
            } catch (IOException e) {
                String err_msg = "Error connecting to " + btDevice.getName() + ": " + e;
                notifyResult(buildPayload(false, err_msg), callback);
            }
        });
    }

    public void request(String reqBody, BTCallback callback) {
        executor.execute(() -> {
            try {
                send(reqBody);
                String res = receive();
                Log.d(TAG, "Response from BT Server: " + res);
                JSONObject json = new JSONObject(res);
                notifyResult(json, callback);
            } catch (IOException e) {
                String err_msg = "Problems reading from socket: " + e;
                Log.e(TAG, err_msg);
                notifyResult(buildPayload(false, err_msg), callback);
            } catch (JSONException je) {
                Log.e(TAG, "Error building JSON response: " + je);
                je.printStackTrace();
            }
        });
    }

    public void send(@NonNull String msg) throws IOException {
        OutputStream mmOutputStream = btSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes());
    }

    public String receive() throws IOException {
        InputStream mmInputStream = btSocket.getInputStream();
        byte[] buffer = new byte[256];
        int bytes = mmInputStream.read(buffer);
        String readMessage = new String(buffer, 0, bytes);
        Log.d(TAG, "Received: " + readMessage);
        return readMessage;
    }

    public void close() {
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "BT Socket is already closed");
        }
    }

    private JSONObject buildPayload(boolean ok, String msg) {
        JSONObject result = new JSONObject();
        try {
            result.put("ok", ok);
            result.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}

