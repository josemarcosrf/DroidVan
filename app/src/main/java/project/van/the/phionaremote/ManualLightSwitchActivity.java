package project.van.the.phionaremote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;


public class ManualLightSwitchActivity extends BaseLayout {

    // Logging Activity tag
    private static final String TAG = "PhionaManualLight";
    private RaspVanRequests req;
    private Response.Listener<JSONObject> lightsListener;
    //    private GestureDetectorCompat gestureObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_switches);
        super.onCreateDrawer();

        // RaspVan requests utility
        req = new RaspVanRequests(this);
        lightsListener = response -> {
            Log.d(TAG, response.toString());
            updateLightsState(response);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check the current lights state to adjust the switches
        req.getLightsState(lightsListener);
    }

    public void requestLightState(View view) {
        req.getLightsState(lightsListener);
    }

    private void updateLightsState(JSONObject response) {
        JSONArray keys = response.names();

        for (int i = 0; i < keys.length(); ++i) {
            String key = "N/D";
            try {
                // key is the light name (String), value is a light state (Boolean)
                key = keys.getString(i);
                Boolean value = response.getBoolean(key);

                Log.d(TAG, key + " ==> " + value + " | " + (value? "ON" : "OFF"));

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

    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState("main", aSwitch);
        req.sendSwitchLightRequest("main", switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState("L1", aSwitch);
        req.sendSwitchLightRequest("l1", switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState("L2", aSwitch);
        req.sendSwitchLightRequest("l2", switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState("L3", aSwitch);
        req.sendSwitchLightRequest("l3", switchState);
    }


    private Boolean checkToggleState(String lightName, Switch aSwitch) {
        // check current state of a Switch (true or false).
        final Boolean switchState = aSwitch.isChecked();
        return switchState;
    }
}



