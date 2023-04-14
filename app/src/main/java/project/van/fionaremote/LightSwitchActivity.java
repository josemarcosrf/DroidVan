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


class LightsBTCallback extends BTCallback {

    private static final String TAG = "FionaLightsBTCallback";
    private final LightSwitchActivity ctx;

        public LightsBTCallback(Context ctx) {
            super(ctx);
            this.ctx = (LightSwitchActivity) ctx;
        }

        @Override
        public boolean onConnect(@NonNull JSONObject result) {
            boolean connected = super.onConnect(result);
            if (connected) {
                Log.d(TAG, "Fetching lights states after successful connection...");
                this.ctx.requestLightState(null);
            }
            this.ctx.connMsg.setVisibility(connected? View.INVISIBLE : View.VISIBLE);
            return true;
        }

        @Override
        public void onServerStateUpdate(@NonNull JSONObject result) {
            super.onServerStateUpdate(result);
            // Fetch light states
            try {
                boolean isOk = result.getBoolean("ok");
                if (isOk) {
                    Log.d(TAG, "Connection was Ok, now requesting light state...");
                    this.ctx.updateLightSwitches(result.getJSONArray("state"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


public class LightSwitchActivity extends BaseLayout {
    // Activity variables
    private static final String TAG = "FionaManualLight";
    // Bluetooth
    private BTClient btClient;
    // Thread variables
    protected TextView connMsg;
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_switches);
        super.onCreateDrawer();

        connMsg = findViewById(R.id.bt_connection_text);
        connMsg.setVisibility(View.VISIBLE);

        btClient = project.van.fionaremote.BTClient.getInstance(executorService, mainThreadHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btClient.prepareBT(getBTDeviceName(), getServerUUID(), new LightsBTCallback(this));
        setConnMsgVisibility(!btClient.isConnected());
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Closing BT connection", Toast.LENGTH_SHORT).show();
        btClient.close();
        super.onDestroy();
    }

    private Boolean checkToggleState(@NonNull Switch aSwitch) {
        // check current state of a Switch (true or false).
        return aSwitch.isChecked();
    }

    public void setConnMsgVisibility(boolean visibility) {
        connMsg.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    private void requestLightSwitch(Integer channel, Boolean switchState) {
        String mode = switchState ? "1" : "0";
        String payload = "{\"cmd\": \"/switch\", \"channels\": [" + channel + "], \"mode\": " + mode + "}";
        btClient.request(payload, new LightsBTCallback(this));
    }

    public void updateLightSwitches(@NonNull JSONArray state) {
        Log.d(TAG, "In updateLightSwitches! " + state + " (len: " + state.length() + ")");
        for (int i = 0; i < state.length(); i++) {
            String lKey = i > 0 ? "l" + i: "main";
            try {
                // Move the switches accordingly
                int resID = getResources().getIdentifier(lKey + "_switch", "id", getPackageName());
                Switch aSwitch = findViewById(resID);
                boolean value = state.getInt(i) > 0;
                aSwitch.setChecked(value);
            } catch (JSONException e) {
                Log.e(TAG, "Error looping through light state: " + e);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Called when the user presses the 'get light status' button
     */
    public void requestLightState(View view) {
        String payload = "{\"cmd\": \"/read\"}";
        btClient.request(payload, new LightsBTCallback(this));
    }

    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.requestLightSwitch(1, switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.requestLightSwitch(2, switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.requestLightSwitch(3, switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState(aSwitch);
        this.requestLightSwitch(4, switchState);
    }

}



