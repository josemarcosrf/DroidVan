package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executor;


// The callback interface
interface BTCallbackInterface {
    void onComplete(String result);
    void onResult(JSONObject result);
}

public class BTClient {

    // TODO: Handle reconnections

    // Bluetooth
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;
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
        String address = sharedPref.getString(BtKey, BtServerUUID);
        return address;
    }

    private void notifyResult(String result, BTCallback callback) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(result);
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

    public void connect(BTCallback callback) {
        // Equivalent to 'new Runnable() { .... }
        // But here we use a lambda function instead
        // For more info see:
        // https://developer.android.com/guide/background/threading#creating-multiple-threads
        executor.execute(() -> {
            try {
                blockingConnect();
                notifyResult("Connection OK :)", callback);
            } catch (IOException e) {
                String err_msg = "Error connecting to " + btDevice.getName() + ": " + e;
                notifyResult(err_msg, callback);
            }
        });
    }

    public void send(String msg) throws IOException {
        OutputStream mmOutputStream = btSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes());
    }

    public String receive() throws IOException {
        InputStream mmInputStream = btSocket.getInputStream();
        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = mmInputStream.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            Log.d(TAG, "Received: " + readMessage);
            // btSocket.close();
            return readMessage;
        } catch (IOException e) {
            Log.e(TAG, "Problems reading from socket: " + e);
            return "";
        }
    }

    public void close() {
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "BT Socket is already closed");
        }
    }
}

