package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class BTCLient extends Thread {

    // TODO: Handle recoonections
    // TODO:

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket btSocket;
    private BluetoothDevice btDevice;

    private static final String TAG = "FionaBTClient";
    private SharedPreferences sharedPref;
    private Context context;


    public BTCLient(Context context, BluetoothDevice device) {
        // Shared Preferences where to store app settings (IP, port, ...)
        this.context = context;
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.settings_file_key), Context.MODE_PRIVATE);


        Log.d(TAG, "BT Client init function");
        try {
            this.ConnectThread(device);
        } catch (IOException ioe) {
            Log.e(TAG, "Error: " + ioe);
        }
    }

    private void ConnectThread(BluetoothDevice device) throws IOException {
        /*
        if (btSocket != null) {
            if(btSocket.isConnected()) {
                send();
            }
        }
        */
        BluetoothSocket sock = null;
        BluetoothDevice mmDevice = device;
        try {
            String uuidStr = getRPIServerUUID();
            Log.d(TAG, "RPI UUID at BTClient: " + uuidStr);
            sock = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidStr));
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        btSocket = sock;
        btAdapter.cancelDiscovery();
        try {
            btSocket.connect();
        } catch (IOException connectException) {
            Log.v(TAG, "Connection exception!");
            try {
                btSocket.close();
                /*
                btSocket = (BluetoothSocket) btDevice.getClass().getMethod(
                    "createRfcobtSocket", new Class[]{int.class}).invoke(btDevice, 1);
                btSocket.connect();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                */
            } catch (IOException closeException) {
                Log.e(TAG, "Connection closed!");
            }
        }
    }

    public void send(String msg) throws IOException {
        OutputStream mmOutputStream = btSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes());
        String r = receive();
        Log.i(TAG, "Result: " + r);
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
            Log.e(TAG, "Problems occurred!");
            return "";
        }
    }

    private String getRPIServerUUID() {
        String BtKey = context.getResources().getString(R.string.rpi_bt_uuid);
        String BtServerUUID = context.getResources().getString(R.string.sample_uuid);
        String address = sharedPref.getString(BtKey, BtServerUUID);
        return address;
    }
}
