package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LightSwitchActivity extends BaseLayout {

    // TODO: Handle continous attempts to reconnect (or on every click? by BTClient...)
    // TODO: Connect and send / receive with in a separate thread with callbacks or similar
    // TODO: Become aware of server disconnection
    // TODO: Handle Bluetooth Enabling intent

    // Logging Activity tag
    private static final String TAG = "FionaManualLight";
    private TextView connMsg;
    // Bluetooth
    private BTClient BTClient;
    // Thread variables
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    BTCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_switches);
        super.onCreateDrawer();

        connMsg = (TextView) findViewById(R.id.bt_connection_text);
        connMsg.setVisibility(View.VISIBLE);
        this.initBT();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.initBT();
        // TODO: check the current lights state to adjust the switches
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Closing BT connection", Toast.LENGTH_SHORT).show();
        BTClient.close();
        super.onDestroy();
    }

    private void initBT() {
        // TODO: Move the discovery away from the main view.
        // TODO: How do we start the activity when is not here?
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth :(", Toast.LENGTH_SHORT).show();
        }
        else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Integer REQUEST_ENABLE_BT = 0;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (BTClient != null) {
            // TODO: Check connectivity instead of the instance not being null
            return;
        }

        // TODO: Handle btAdapter being null
        assert btAdapter != null;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Found BT device: " + deviceName + " @" + deviceHardwareAddress);

                // TODO: Define RPI name in Resources
                if (deviceName.equals("raspberrypi")) {
                    Log.i(TAG, "Found raspberrypi!!!! Trying to connect!");
                    Toast.makeText(this, "Connecting to " + deviceName, Toast.LENGTH_SHORT).show();
                    BTClient = new BTClient(
                            this,
                            executorService,
                            mainThreadHandler,
                            device);
                    BTClient.connect(new BTCallback(this));
                    connMsg.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        } else {
            Log.d(TAG, "Couldn't find any BT device!");
        }
    }

    public void requestLightState(View view) {
        // TODO: Read from bluetooth socket
    }

    private void updateLightsState(JSONObject response) {
        // TODO: Read from bluetooth socket
        JSONArray keys = response.names();

        for (int i = 0; i < keys.length(); ++i) {
            String key = "N/D";
            try {
                // key is the light name (String), value is a light state (Boolean)
                key = keys.getString(i);
                Boolean value = response.getBoolean(key);

                Log.d(TAG, key + " ==> " + value + " | " + (value ? "ON" : "OFF"));

                // Move the switches accordingly
                int resID = getResources().getIdentifier(key + "_switch", "id", getPackageName());
                Switch aSwitch = findViewById(resID);
                aSwitch.setChecked(value);

            } catch (Exception e) {
                Log.e(TAG, "Error on reading light (" + key + ") status: " + e);
                e.printStackTrace();
            }
        }
    }

    private Boolean checkToggleState(Switch aSwitch) {
        // check current state of a Switch (true or false).
        final Boolean switchState = aSwitch.isChecked();
        return switchState;
    }

    private void send(Integer channel, Boolean switchState) {
        try {
            String mode = switchState ? "1" : "0";
            // TODO: Handle proper JSON payloads
            BTClient.send("{\"cmd\": \"switch\", \"channels\": [" + channel + "], \"mode\": " + mode + "}");
            String lightState = BTClient.receive();
            Log.d(TAG, "Light state: " + lightState);
            // TODO: Update switches accordingly
        } catch (IOException ioe) {
            String msg = "Error sending light switch command to RPI: " + ioe;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
        }
    }

    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.send(1, switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.send(2, switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.send(3, switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.send(4, switchState);
    }

}



