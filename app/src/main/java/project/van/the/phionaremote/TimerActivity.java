package project.van.the.phionaremote;

import project.van.the.phionaremote.RaspVanRequests;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimerActivity extends BaseLayout {

    private static final String TAG = "PhionaNavActivity";

    private RaspVanRequests req;
    private Response.Listener<JSONArray> timersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();

        Context context = this;

        // RaspVan request class
        req = new RaspVanRequests(this);

        // Floating action button on the right bottom side of the screen
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                defaultTimerForTesting();
            }
        });

        timersListener = response -> {
            Log.d(TAG, response.toString());
            Toast.makeText(context,
                    "Timers: " + response.toString(), Toast.LENGTH_SHORT).show();
        };

        // check the current lights state to adjust the switches
        req.getTimers(timersListener);
    }

    public void defaultTimerForTesting() {
        Toast.makeText(this,
                "Sending a timer to switch off in 1o seconds...", Toast.LENGTH_SHORT).show();
        req.setTimerRequest("main", false, 15);
    }


}
