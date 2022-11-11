package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPClient {

    private static final String TAG = "FionaRequests";

    // Default connection parameters
    private static final String light_endpoint = "/lights";
    private static final String timer_endpoint = "/timer";
    private static RequestQueue requestQueue;       // Connection request queue
    private final SharedPreferences sharedPref;
    private final Context context;


    public HTTPClient(Context context) {
        this.context = context;
        String settingsName = context.getResources().getString(R.string.settings_file_key);

        // Shared Preferences where to store app settings (IP, port, ...)
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.settings_file_key), Context.MODE_PRIVATE);

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Fetches from the server the current lights status via a GET request
     */
    public void getLightsState(Response.Listener<JSONObject> listener) {

        Log.d(TAG, "'getLightsState' not implemented yet!");

        /*
        // Build the endpoint
        String address = getIP();
        String port = getPort();
        String url = "http://" + address + ":" + port + light_endpoint;

        Log.d(TAG, "Hitting endpoint: " + url);

        // build request object
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url, null,
                listener,
                error -> Log.e(TAG, error.toString())
        );
        // Add the request to the Queue
        requestQueue.add(jsonObjReq);
        */
    }

    public void getTimers(Response.Listener<JSONArray> listener) {

        Log.d(TAG, "'getTimers' not implemented yet!");

        /*
        // Build the endpoint
        String address = getIP();
        String port = getPort();
        String url = "http://" + address + ":" + port + timer_endpoint;

        Log.d(TAG, "Hitting endpoint: " + url);

        // build request object
        JsonArrayRequest jsonArrReq = new JsonArrayRequest(
                Request.Method.GET,
                url, null,
                listener,
                error -> Log.e(TAG, error.toString())
        );
        // Add the request to the Queue
        requestQueue.add(jsonArrReq);
        */
    }

    /**
     * Send a POST request to switch ON / OFF a light controlled by the RaspberryPi server
     *
     * @param lightName   (String): one of [main, l1, l2, l3]
     * @param switchState (Boolean): True to switch ON, False to switch OFF
     */
    public void sendSwitchLightRequest(String lightName, Boolean switchState) {

        Log.d(TAG, "'sendSwitchLightRequest' not implemented yet!");

        /*
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
                response -> {
                    // Switch status logging and display
                    JSONArray keys = response.names();
                    for (int i = 0; i < keys.length(); ++i) {
                        String key = "";
                        try {
                            // key is the light name (String),
                            // value is a light state (boolean)
                            key = keys.getString(i);
                            String value = response.getString(key);
                            Toast.makeText(context,
                                    "Switching light '" + key + "' ==> " + value,
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error on switch light (" + key + ") response: " + e);
                            e.printStackTrace();
                        }
                    }
                }, error -> {
                    Toast.makeText(context,
                            "Switch request error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, error.toString());
                });
        // Add the request to the Queue
        requestQueue.add(jsonObjReq);
        */
    }

    public void setTimerRequest(String lightName, Boolean signal, Integer seconds) {

        Log.d(TAG, "'setTimerRequest' not implemented yet!");
        /*
        // Build the endpoint
        String address = getIP();
        String url = "http://" + address + ":" + port + timer_endpoint;

        Log.d(TAG, "Hitting endpoint: " + url);

        // build the JSON body.... so messy! Change this for something better
        Map map = new HashMap();
        Map switchMap = new HashMap();
        switchMap.put("signal", signal);
        switchMap.put("delay", seconds);
        map.put(lightName, switchMap);
        JSONObject body = new JSONObject(map);

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                url, body,
                response -> Log.d(TAG, response.toString()),
                error -> Log.e(TAG, error.toString())
        );
        // Add the request to the Queue
        requestQueue.add(jsonObjReq);
        */
    }

    private String getRPIServerUUID() {
        String BtKey = context.getResources().getString(R.string.rpi_bt_uuid);
        String BtServerUUID = context.getResources().getString(R.string.sample_uuid);
        String address = sharedPref.getString(BtKey, BtServerUUID);
        return address;
    }
}
