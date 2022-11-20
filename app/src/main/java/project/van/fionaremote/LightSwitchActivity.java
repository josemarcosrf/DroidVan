package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LightSwitchActivity extends BaseLayout {

    // TODO: Handle continous attempts to reconnect (or on every click? by BTClient...)
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
    private Switch aSwitch;

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

        prepareBT();
    }

    private void prepareBT() {
        BTClient = new BTClient(executorService, mainThreadHandler);
        BTClient.findDevice("raspberrypi");
        BTClient.pairWith("raspberrypi", new BTCallback(this));
        BTClient.connect(getRPIServerUUID(), new BTCallback(this));
        connMsg.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BTClient.connect(getRPIServerUUID(), new BTCallback(this));
        prepareBT();
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

    private Boolean checkToggleState(@NonNull Switch aSwitch) {
        // check current state of a Switch (true or false).
        final Boolean switchState = aSwitch.isChecked();
        return switchState;
    }

    private void callServerLightState() {
        String payload = "{\"cmd\": \"/read\"}";
        BTClient.request(payload, new BTCallback(this));
    }

    private void callServerSwitch(Integer channel, Boolean switchState) {
        // TODO: Handle proper JSON payloads
        String mode = switchState ? "1" : "0";
        String payload = "{\"cmd\": \"/switch\", \"channels\": [" + channel + "], \"mode\": " + mode + "}";
        BTClient.request(payload, new BTCallback(this));
    }

    public void updateLightSwitches(@NonNull JSONArray state) {
        Log.d(TAG, "In updateLightSwitches! " + state + " (len: " + state.length() + ")");
        String lKey = "main";
        for (int i = 0; i < state.length(); i++) {
            if (i > 0)
                lKey = "l" + i;
            try {
                boolean value = state.getInt(i) < 1;
                Log.d(TAG, lKey + " (bool) ==> " + value);
                // Move the switches accordingly
                int resID = getResources().getIdentifier(lKey + "_switch", "id", getPackageName());
                Switch aSwitch = findViewById(resID);
                aSwitch.setChecked(value);
            } catch (JSONException e) {
                Log.e(TAG, "Error looping through light state: " + e);
                e.printStackTrace();
            }
        }
    }

    public void requestLightState(View view) {
        callServerLightState();
    }

    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServerSwitch(1, switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServerSwitch(2, switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServerSwitch(3, switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.callServerSwitch(4, switchState);
    }

}



