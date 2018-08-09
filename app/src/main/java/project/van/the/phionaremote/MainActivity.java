package project.van.the.phionaremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

// Requests Android library
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Hardcoded URI string for the time being
    private static final String url = "http://192.168.1.47:5000/lights";
    private static final String TAG = "MainPhionaActivity";
    private static RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(this);
        getLightsState(null);
    }

    private void sendSwitchLightRequest(String lightName, Boolean switchState) {
        // Request a string response from the provided URL.
        Map map = new HashMap();
        map.put(lightName, switchState);
        JSONObject body = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                url, body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
        // Add the request to the Queue
        requestQueue.add(jsonObjReq);
    }

    public void getLightsState(View view) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
            Request.Method.GET,
            url, null,
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    JSONArray keys = response.names ();

                    for (int i = 0; i < keys.length (); ++i) {
                        String key = "N/D";
                        try {
                            // key is the light name (String), value is a light state string (ON / OFF)
                            key = keys.getString(i);
                            String value = response.getString(key);

                            Log.d(TAG, key + " ==> " + value + " | " + (value.equals("ON")? true:false));

                            // Move the switches accordingly
                            int resID = getResources().getIdentifier(key + "_switch", "id", getPackageName());
                            Switch aSwitch = (Switch) findViewById(resID);
                            aSwitch.setChecked(value.equals("ON")? true:false);

                        } catch (Exception e) {
                            Log.e(TAG, "Error on reading light (" + key + ") status: " + e);
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
        // Add the request to the Queue
        requestQueue.add(jsonObjReq);
    }

    private Boolean checkToggleState(String lightName, Switch aSwitch) {
        // check current state of a Switch (true or false).
        final Boolean switchState = aSwitch.isChecked();

        // Switch status logging and display
        String status = switchState ? "ON" : "OFF";
        String msg = "Turning the " + lightName + " lights " + status;
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        return switchState;
    }

    /** Called when the user toggles the main lights toggle */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState("main", aSwitch);
        sendSwitchLightRequest("main", switchState);
    }

    /** Called when the user toggles the L1 lights toggle */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState("L1", aSwitch);
        sendSwitchLightRequest("l1", switchState);
    }

    /** Called when the user toggles the L2 lights toggle */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState("L2", aSwitch);
        sendSwitchLightRequest("l2", switchState);
    }

    /** Called when the user toggles the L3 lights toggle */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState("L3", aSwitch);
        sendSwitchLightRequest("l3", switchState);
    }

}
