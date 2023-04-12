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


public class BTClient {
    // Bluetooth
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;
    private String namePairedDevice;
    private UUID connUUID;
    // Threading
    private final Executor executor;
    private final Handler resultHandler;
    // Other
    private static final String TAG = "FionaBTClient";
    // Singleton
    private static BTClient btClientInstance;

    public static BTClient getInstance(Executor executor, Handler resultHandler) {
        if (btClientInstance == null) {
            btClientInstance = new BTClient(executor, resultHandler);
        }
        return btClientInstance;
    }

    private BTClient(Executor executor, Handler resultHandler) {
        Log.d(TAG, "BT Client init");
        this.executor = executor;
        this.resultHandler = resultHandler;
    }

    private JSONObject buildPayload(boolean ok, String msg) {
        JSONObject result = new JSONObject();
        try {
            result.put("ok", ok);
            if (ok)
                result.put("msg", msg);
            else
                result.put("error", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void notifyResult(JSONObject payload, BTCallback callback) {
        resultHandler.post(() -> {
            callback.onMessage(payload);
        });
    }

    public void prepareBT(UUID serverUUID, BTCallback callback) {
        // TODO: Make the device name configurable as a setting (as the UUID)
        String deviceName = "jose-N501VW";  //raspberrypi
        boolean btFound = findDevice(deviceName, callback);
        if (btFound) {
            pairWith(deviceName, callback);
            connect(serverUUID, callback);
        } else {
            notifyResult(buildPayload(false, "Closing BT connection"), callback);
        }
    }

    private boolean findDevice(String devicePairName, BTCallback callback) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            throw new RuntimeException("Device doesn't support Bluetooth :(");
        } else if (!btAdapter.isEnabled()) {
            resultHandler.post(callback::onBTNotEnabled);
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
                    namePairedDevice = devicePairName;
                    return true;
                }
            }
        }

        Log.w(TAG, "Couldn't find any BT device!");
        return false;
    }

    private boolean blockingConnect(UUID uuid) throws IOException {
        btAdapter.cancelDiscovery();
        if (btDevice == null)
             throw new IOException("btDevice is null");

        Log.d(TAG, "BTSocket is null: " + (btSocket == null));
        if (btSocket == null) {
            try {
                Log.d(TAG, "Creating BT socket RFCOMM");
                btSocket = btDevice.createRfcommSocketToServiceRecord(uuid); // btDevice might be null
                connUUID = uuid;
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }
        if (btSocket != null) {
            if (btSocket.isConnected())
                Log.d(TAG, "BT Socket already connected");
            else {
                Log.d(TAG, "Connecting to RPI UUID: " + uuid);
                btSocket.connect();
                return true;
            }
        }
        return false;
    }

    private void pairWith(String deviceName, BTCallback callback) {
        executor.execute(() -> {
            try {
                if (btDevice == null) {
                    if (findDevice(deviceName, callback)) {
                        notifyResult(buildPayload(true, "BT Device found"), callback);
                    } else {
                        notifyResult(buildPayload(false, "Device not found!"), callback);
                    }
                }
            } catch (RuntimeException re) {
                Log.e(TAG, "Runtime exception while pairing: " + re);
                notifyResult(buildPayload(false, String.valueOf(re)), callback);
            } catch (Exception e) {
                Log.e(TAG, "Other exception while pairing: " + e);
            }
        });
    }

    private void send(@NonNull String msg) throws IOException {
        blockingConnect(this.connUUID);
        OutputStream mmOutputStream = btSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes());
    }

    private String receive() throws IOException {
        blockingConnect(this.connUUID);
        InputStream mmInputStream = btSocket.getInputStream();
        byte[] buffer = new byte[256];
        int bytes = mmInputStream.read(buffer);
        String readMessage = new String(buffer, 0, bytes);
        return readMessage;
    }

    public void connect(UUID uuid, BTCallback callback) {
        // Equivalent to 'new Runnable() { .... }
        // But here we use a lambda function instead
        // For more info see:
        // https://developer.android.com/guide/background/threading#creating-multiple-threads
        executor.execute(() -> {
            try {
                boolean hasConnected = blockingConnect(uuid);
                JSONObject payload = buildPayload(true, "Connection OK :)");
                if (hasConnected)
                    resultHandler.post(() -> {
                        callback.onConnect(payload);
                    });
            } catch (IOException e) {
                String btDeviceName = btDevice != null? btDevice.getName() : "RPI";
                String err_msg = "Error connecting to " + btDeviceName + ": " + e;
                notifyResult(buildPayload(false, err_msg), callback);
                findDevice(namePairedDevice, callback);
            }
        });
    }

    public void request(String reqBody, BTCallback callback) {
        executor.execute(() -> {
            try {
                send(reqBody);
                String res = receive();
                Log.d(TAG, "Response from BT Server: " + res);
                JSONObject response_payload = new JSONObject(res);
                resultHandler.post(() -> {
                    callback.onServerStateUpdate(response_payload);
                });
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

    public boolean isConnected() {
        return btSocket != null && btSocket.isConnected();
    }

    public void close() {
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "BT Socket is already closed");
        }
    }

}

