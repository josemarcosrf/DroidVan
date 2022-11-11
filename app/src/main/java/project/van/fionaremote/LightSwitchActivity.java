package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LightSwitchActivity extends BaseLayout {

    // TODO: Handle continous attempts to reconnect (or on every click? by BTClient...)
    // TODO: Connect and send / receive with in a separate thread with callbacks or similar
    // TODO: Become aware of server disconnection
    // TODO: Handle Bluetooth Enabling intent

    // Activity variables
    private static final String TAG = "FionaManualLight";
    private SharedPreferences sharedPref;
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

        connMsg = findViewById(R.id.bt_connection_text);
        connMsg.setVisibility(View.VISIBLE);

        // TODO: Get directly the UUID instead of having the preferences here?
        sharedPref = this.getSharedPreferences(
                this.getString(R.string.settings_file_key), Context.MODE_PRIVATE);

        UUID serverUUID = getRPIServerUUID();
        BTClient = new BTClient(executorService, mainThreadHandler);
        BTClient.findDevice("raspberrypi");
        BTClient.pairWith("raspberrypi", new BTCallback(this));
        BTClient.connect(serverUUID, new BTCallback(this));
        connMsg.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: check the current lights state to adjust the switches
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Closing BT connection", Toast.LENGTH_SHORT).show();
        BTClient.close();
        super.onDestroy();
    }

    private UUID getRPIServerUUID() {
        String BtKey = this.getResources().getString(R.string.rpi_bt_uuid);
        String BtServerUUID = this.getResources().getString(R.string.sample_uuid);
        String uuidStr = sharedPref.getString(BtKey, BtServerUUID);
        return UUID.fromString(uuidStr);
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
                boolean value = response.getBoolean(key);

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

    private void callServer(Integer channel, Boolean switchState) {
        // TODO: Handle proper JSON payloads
        String mode = switchState ? "1" : "0";
        String payload = "{\"cmd\": \"switch\", \"channels\": [" + channel + "], \"mode\": " + mode + "}";
        BTClient.request(payload, new BTCallback(this));
    }

    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServer(1, switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServer(2, switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServer(3, switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServer(4, switchState);
    }

}



