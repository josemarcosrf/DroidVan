package project.van.the.phionaremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

public class ManualLightSwitchActivity extends baseLayout {

    // Logging Activity tag
    private static final String TAG = "PhionaManualLight";

    // Default connection parameters
    private static final String light_endpoint = "/lights";
    private static final String timer_endpoint = "/timer";


    private static RequestQueue requestQueue;       // Connection request queue
    private SharedPreferences sharedPref;
    private GestureDetectorCompat gestureObject;    // Gesture detector

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_switches);
        super.onCreateDrawer();

        sharedPref = this.getSharedPreferences(
                getString(R.string.settings_file_key), Context.MODE_PRIVATE);

        // swipe right to open navigation drawer
        gestureObject = new GestureDetectorCompat(this, new LearnGesture(this));

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(this);
        getLightsState(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "On ManualLightSwitchActivity-onTouchEvent...");
        Toast.makeText(this, "On ManualLightSwitchActivity-onTouchEvent...",
            Toast.LENGTH_SHORT).show();
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Fetches from the server the current lights status via a GET request
     */
    public void getLightsState(View view) {

        // Build the endpoint
        String address = getIP();
        String port = getPort();
        String url = "http://" + address + ":" + port + light_endpoint;

        Log.d(TAG, "Hitting endpoint: " + url);

        // build request object
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        JSONArray keys = response.names();

                        for (int i = 0; i < keys.length(); ++i) {
                            String key = "N/D";
                            try {
                                // key is the light name (String), value is a light state string (ON / OFF)
                                key = keys.getString(i);
                                String value = response.getString(key);

                                Log.d(TAG, key + " ==> " + value + " | " + (value.equals("ON") ? true : false));

                                // Move the switches accordingly
                                int resID = getResources().getIdentifier(key + "_switch", "id", getPackageName());
                                Switch aSwitch = (Switch) findViewById(resID);
                                aSwitch.setChecked(value.equals("ON") ? true : false);

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


    /**
     * Called when the user toggles the main lights toggle
     */
    public void switchMainLights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.main_switch);
        Boolean switchState = checkToggleState("main", aSwitch);
        sendSwitchLightRequest("main", switchState);
    }

    /**
     * Called when the user toggles the L1 lights toggle
     */
    public void switchL1Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l1_switch);
        Boolean switchState = checkToggleState("L1", aSwitch);
        sendSwitchLightRequest("l1", switchState);
    }

    /**
     * Called when the user toggles the L2 lights toggle
     */
    public void switchL2Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l2_switch);
        Boolean switchState = checkToggleState("L2", aSwitch);
        sendSwitchLightRequest("l2", switchState);
    }

    /**
     * Called when the user toggles the L3 lights toggle
     */
    public void switchL3Lights(View view) {
        // Get the switch
        Switch aSwitch = (Switch) findViewById(R.id.l3_switch);
        Boolean switchState = checkToggleState("L3", aSwitch);
        sendSwitchLightRequest("l3", switchState);
    }


    /**
     * Send a POST request to switch ON / OFF a light controlled by the RaspberryPi Flask server
     *
     * @param lightName   (String): one of [main, l1, l2, l3]
     * @param switchState (Boolean): True to switch ON, False to switch OFF
     */
    private void sendSwitchLightRequest(String lightName, Boolean switchState) {
        // Build the endpoint
        String address = getIP();
        String port = getPort();
        String url = "http://" + address + ":" + port + light_endpoint;

        Log.d(TAG, "Hitting endpoint: " + url);

        // Request a JSON response from the provided URL.
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

    private String getIP() {
        String ipKey = getResources().getString(R.string.raspvan_ip);
        String defaultIP = getResources().getString(R.string.sample_ip);
        String address = sharedPref.getString(ipKey, defaultIP);
        return address;
    }

    private String getPort() {
        String portKey = getResources().getString(R.string.raspvan_port);
        String defaultPort = getResources().getString(R.string.sample_port);
        String port = sharedPref.getString(portKey, defaultPort);
        return port;
    }

}



