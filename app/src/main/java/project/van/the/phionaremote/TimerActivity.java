package project.van.the.phionaremote;

import project.van.the.phionaremote.RaspVanRequests;
import project.van.the.phionaremote.TimePicker.MyTimePickerDialog;
import project.van.the.phionaremote.TimePicker.TimePicker;

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
import java.util.Calendar;
import java.util.List;

public class TimerActivity extends BaseLayout {

    private static final String TAG = "PhionaTimerActivity";

    private RaspVanRequests req;
    private Response.Listener<JSONArray> timersListener;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();

        context = this;

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
            Toast.makeText(context, "Timers: " + response.toString(), Toast.LENGTH_SHORT).show();
        };

        // check the current lights state to adjust the switches
        req.getTimers(timersListener);
    }

    public void showPicker(){
        Calendar now = Calendar.getInstance();
        MyTimePickerDialog mTimePicker = new MyTimePickerDialog(this, (view, hours, minutes, seconds) -> {
            // TODO Auto-generated method stub
            String timePicked = getString(R.string.time) + String.format("%02d", hours)+
                    ":" + String.format("%02d", minutes) +
                    ":" + String.format("%02d", seconds);
            Log.i(TAG, "Delay " + timePicked);
            Toast.makeText(context,"Setting: " + timePicked, Toast.LENGTH_SHORT).show();
            req.setTimerRequest("main", false,
                    hours * 3600 + 60 * minutes + seconds);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND), true);
        mTimePicker.show();
    }

    public void defaultTimerForTesting() {
//        Toast.makeText(this,
//                "Sending a timer to switch off in 1o seconds...", Toast.LENGTH_SHORT).show();
//        req.setTimerRequest("main", false, 15);
        showPicker();
    }


}
