package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executor;


// The callback interface
interface BTCallbackInterface {
    // TODO: Make result a class or a JSONObject
    void onComplete(JSONObject result);
}

public class BTClient {

    // TODO: Handle reconnections

    // Bluetooth
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket btSocket;
    private final BluetoothDevice btDevice;
    // Threading
    private final Executor executor;
    private final Handler resultHandler;
    // Other
    private static final String TAG = "FionaBTClient";
    private final SharedPreferences sharedPref;
    private final Context context;

    public BTClient(Context context, Executor executor, Handler resultHandler, BluetoothDevice device) {
        Log.d(TAG, "BT Client init");
        // Shared Preferences where to store app settings (IP, port, ...)
        this.context = context;
        this.btDevice = device;
        this.executor = executor;
        this.resultHandler = resultHandler;

        // TODO: Get directly the UUID instead of having the preferences here?
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.settings_file_key), Context.MODE_PRIVATE);
    }

    private String getRPIServerUUID() {
        String BtKey = context.getResources().getString(R.string.rpi_bt_uuid);
        String BtServerUUID = context.getResources().getString(R.string.sample_uuid);
        return sharedPref.getString(BtKey, BtServerUUID);
    }

    private void notifyResult(Boolean error, String res, BTCallback callback) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject result = new JSONObject();
                    result.put("error", error);
                    result.put("result", res);
                    callback.onComplete(result);
                } catch (JSONException je) {
                    Log.e(TAG, "Error composing JSON result: " + je);
                }
            }
        });
    }

    private void blockingConnect() throws IOException {
        btAdapter.cancelDiscovery();
        Log.d(TAG, "BTSocket is null: " + (btSocket == null));
        if (btSocket == null) {
            try {
                String uuidStr = getRPIServerUUID();
                Log.d(TAG, "RPI UUID at BTClient: " + uuidStr);
                btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidStr));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }
        if (btSocket.isConnected()) Log.i(TAG, "BT Socket already connected");
        else {
            btSocket.connect();
        }
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

    public void connect(BTCallback callback) {
        // Equivalent to 'new Runnable() { .... }
        // But here we use a lambda function instead
        // For more info see:
        // https://developer.android.com/guide/background/threading#creating-multiple-threads
        executor.execute(() -> {
            try {
                blockingConnect();
                notifyResult(false, "Connection OK :)", callback);
            } catch (IOException e) {
                String err_msg = "Error connecting to " + btDevice.getName() + ": " + e;
                notifyResult(true, err_msg, callback);
            }
        });
    }

    public void request(String reqBody, BTCallback callback) {
        executor.execute(() -> {
            try {
                send(reqBody);
                String res = receive();
                notifyResult(false, res, callback);
            } catch (IOException e) {
                String err_msg = "Problems reading from socket: " + e;
                Log.e(TAG, err_msg);
                notifyResult(true, err_msg, callback);
            }
        });
    }
}

