package project.van.the.phionaremote;

import project.van.the.phionaremote.TimePicker.MyTimePickerDialog;

import android.content.Context;
import android.os.Bundle;
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

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class TimerActivity extends BaseLayout {

    private static final String TAG = "PhionaTimerActivity";

    private RaspVanRequests lightsServer;
    private Response.Listener<JSONArray> timersListener;
    private Context context;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Create drawer and main view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();

        context = this;

        // Floating action button on the right bottom side of the screen
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showPicker());

        // On response from Timer-Server populate the listView
        // TODO: To create every time the CustomAdapter doesn't sound right... review!
        // TODO: Add onClickListener to each list element
        // TODO: Each element of the list should disappear at the exact received datetime received from the server response
        listView = findViewById(R.id.timer_listview);
        timersListener = response -> {
            Log.d(TAG, "LightServer response => " + response.toString());
            CustomAdapter adapter = new CustomAdapter(this, response);
            listView.setAdapter(adapter);
        };

        // RaspVan request class
        lightsServer = new RaspVanRequests(this);

        // Get timers response to the listener queue
        lightsServer.getTimers(timersListener);
    }

    public void showPicker(){
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
            String[] light_names = new String[] { "main", "l1", "l2", "l3" };

            // format the received time delay
            String timePicked = getString(R.string.time) +
                    String.format("%02d", hours)+
                    ":" + String.format("%02d", minutes) +
                    ":" + String.format("%02d", seconds);

            // Logging
            Log.i(TAG, "Delay => " + timePicked + " Signal => " + signal + " Light index => " + light);
            Toast.makeText(context,"Light: " + light + " Signal: " + signal +
                                       " Delay: " + timePicked, Toast.LENGTH_SHORT).show();

            // send a POST request to the RaspberryPi
            lightsServer.setTimerRequest(light_names[light], signal,
                    hours * 3600 + 60 * minutes + seconds);

            // Update timers list
            lightsServer.getTimers(timersListener);

        }, now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);

        mTimePicker.show();
    }

    public class CustomAdapter extends BaseAdapter {

        private Context mContext;
        private JSONArray timers;

        public CustomAdapter(Context context, JSONArray timers) {
            this.mContext = context;
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
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.timer_row, parent, false);

            try {
                JSONArray timer = (JSONArray) this.timers.get(position);

                // Light name and status
                String name = timer.get(0).toString();
                Boolean signal = timer.getBoolean(1);

                // parse the date
                String date = timer.get(2).toString();
                String day = date.split("T")[0];
                String hour = date.split("T")[1].split("\\.")[0];
                Log.d(TAG, "Timer pos=" + position + ": " + timer);

                ImageView i1 = row.findViewById(R.id.imgIcon);
                TextView title = row.findViewById(R.id.txtTitle);
                title.setText(name + " => " + hour);
                if (signal)
                    i1.setImageResource(R.drawable.bulb_on);
                else
                    i1.setImageResource(R.drawable.bulb_off);

            } catch (JSONException e) {
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
