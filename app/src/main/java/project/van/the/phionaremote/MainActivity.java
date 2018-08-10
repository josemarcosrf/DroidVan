package project.van.the.phionaremote;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

    // Logging Activity tag
    private static final String TAG = "MainPhionaActivity";

    // Default connection parameters
    private static String address = "192.168.1.47";
    private static String port = "5000";
    private static String light_endpoint = "/lights";
    private static String timer_endpoint = "/timer";

    // Connection request queue
    private static RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(this);
        getLightsState(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            // action when action_search was selected
            case R.id.ip_setting:
                //****do something when the action_search item is clicked
                Toast.makeText(this, "A window should open to set the IP", Toast.LENGTH_SHORT).show();
                show_ip_dialog();
                break;
            default:
                Toast.makeText(this, "Default option...", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    /**
     * Fetches from the server the current lights status via a GET request
     */
    public void getLightsState(View view) {

        // Build the endpoint
        String url = "http://" + address + ":" + port + light_endpoint;

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

    private void show_ip_dialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_set_ip, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog_set_ip.xml to alertDialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText etAddr = (EditText) promptsView
                .findViewById(R.id.ip_dialog_text);

        final EditText etPort = (EditText) promptsView
                .findViewById(R.id.port_dialog_text);

        etAddr.setText(address);
        etPort.setText(port);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.ip_settings)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                address = etAddr.getText().toString();
                                port = etPort.getText().toString();
                                Log.d(TAG, "IP is =>" + address + ":" + port);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Send a POST request to switch ON / OFF a light controlled by the RaspberryPi Flask server
     *
     * @param lightName   (String): one of [main, l1, l2, l3]
     * @param switchState (Boolean): True to switch ON, False to switch OFF
     */
    private void sendSwitchLightRequest(String lightName, Boolean switchState) {
        // Build the endpoint
        String url = "http://" + address + ":" + port + light_endpoint;

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

}