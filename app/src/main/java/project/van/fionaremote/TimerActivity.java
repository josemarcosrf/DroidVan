package project.van.fionaremote;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.van.fionaremote.TimePicker.MyTimePickerDialog;


class TimersBTCallback extends BTCallback {

    private static final String TAG = "FionaTimersBTCallback";
    private final TimerActivity ctx;

        public TimersBTCallback(Context ctx) {
            super(ctx);
            this.ctx = (TimerActivity) ctx;
        }

        @Override
        public boolean onConnect(@NonNull JSONObject result) {
            boolean connected = super.onConnect(result);
            if (connected) {
                Log.d(TAG, "Fetching pending tasks after successful connection...");
                this.ctx.requestPendingTasks();
            }
            return true;
        }

        @Override
        public void onServerStateUpdate(@NonNull JSONObject result) {
            super.onServerStateUpdate(result);
            // Fetch scheduled tasks
            try {
                boolean isOk = result.getBoolean("ok");
                if (isOk) {
                    JSONArray schedules = result.getJSONArray("scheduled");
                    this.ctx.updateTaskList(schedules);
                } else {
                    String notOkReason = result.getString("error");
                    String msg = "BT Server returned error: " + notOkReason;
                    Log.e(TAG, msg);
                    Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error reading onServerStateUpdate BTResponse JSON object");
            }
        }
    }

public class TimerActivity extends BaseLayout {

    private static final String TAG = "FionaTimerActivity";
    private ListView listView;
    // Bluetooth
    private BTClient btClient;
    // Thread variables
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Create drawer and main view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();

        // Floating action button on the right bottom side of the screen
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showPicker());

        // On response from Timer-Server populate the listView
        // TODO: To create every time the CustomAdapter doesn't sound right... review!
        // TODO: Add onClickListener to each list element
        // TODO: Each element of the list should disappear at the exact received datetime received from the server response
        listView = findViewById(R.id.timer_listview);

        btClient = project.van.fionaremote.BTClient.getInstance(executorService, mainThreadHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btClient.prepareBT(getRPIServerUUID(), new TimersBTCallback(this));
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Closing BT connection", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        btClient.close();
    }

    protected void requestPendingTasks() {
        String payload = "{\"cmd\": \"/read\"}";
        btClient.request(payload, new TimersBTCallback(this));
    }

    private void scheduleServerTask(Integer channel, Boolean switchState, int delay) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("cmd", "/schedule");
            payload.put("mode", switchState);
            payload.put("delay", delay);
            JSONArray channels = new JSONArray();
            channels.put(channel + 1);
            payload.put("channels", channels);

            Log.d(TAG, "Payload: " + payload);
            btClient.request(payload.toString(), new TimersBTCallback(this));
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling Server task: " + e);
            e.printStackTrace();
        }
    }

    public void updateTaskList(JSONArray tasks) {
        Log.d(TAG, "BT Server response: Task to add to list: " + tasks);
        CustomAdapter adapter = new CustomAdapter(this, tasks);
        listView.setAdapter(adapter);
    }

    public void showPicker() {
        Calendar now = Calendar.getInstance();
        // Create timePicker dialogue and pass a callback function (as a lambda function)
        MyTimePickerDialog mTimePicker;
        mTimePicker = new MyTimePickerDialog(this, (view, signal, light, hours, minutes, seconds) -> {
            /*
              TimePicker Listener:
               - Format the received data,
               - Make a REST API call to the Lights server,
               - Add response to the ListAdapter
             */
            // format the received time delay
            String timePicked = getString(R.string.time) +
                    String.format(Locale.getDefault(), "%02d", hours) +
                    ":" + String.format(Locale.getDefault(), "%02d", minutes) +
                    ":" + String.format(Locale.getDefault(), "%02d", seconds);

            // Logging
            Log.i(TAG, "Delay => " + timePicked + " Signal => " + signal + " Light index => " + light);
            Toast.makeText(this, "Light: " + light + " Signal: " + signal +
                    " Delay: " + timePicked, Toast.LENGTH_SHORT).show();

            // send a request to the RaspberryPi
            int delay = hours * 3600 + minutes * 60 + seconds;
            Log.d(TAG , "Total delay: " + delay);
            scheduleServerTask(light, signal, delay);

        }, now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);

        mTimePicker.show();
    }

    public class CustomAdapter extends BaseAdapter {

        private final JSONArray timers;

        public CustomAdapter(Context context, JSONArray timers) {
            this.timers = timers;
            Log.d(TAG, "Timers in CustomAdapter: " + timers);
        }

        public int getCount() {
            return this.timers.length();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "In getView. pos " + position);
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.timer_row, parent, false);

            try {
                JSONArray timerData = (JSONArray) this.timers.get(position);

                // Task time, light and signal
                String time = timerData.getString(0);
                JSONObject kwargs = timerData.getJSONObject(1);
                int channel = kwargs.getJSONArray("channels").getInt(0);
                int signal = kwargs.getInt("mode");

                // parse the date and compose a light namme
                String hour = time.split("T")[1].split("\\.")[0];
                String lightName = channel > 1 ? "L" + (channel-1)  : "main";

                ImageView icon = row.findViewById(R.id.imgIcon);
                TextView title = row.findViewById(R.id.txtTitle);
                title.setText(String.format("H: %s | light: %s", hour, lightName));
                if (signal > 0)
                    icon.setImageResource(R.drawable.bulb_on);
                else
                    icon.setImageResource(R.drawable.bulb_off);

            } catch (Exception e) {
                Log.e(TAG, "Error populating listView: " + e);
                e.printStackTrace();
            }
            return (row);
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
