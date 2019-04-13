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

    private RaspVanRequests req;
    private Response.Listener<JSONArray> timersListener;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();

        context = this;

        // Floating action button on the right bottom side of the screen
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showPicker());

        // On response from Timers Server populate the listView
        final ListView listview = findViewById(R.id.timer_listview);

        // TODO: To create every time the CustomAdapter doesn't sound right... review!
        // TODO: Add onClickListener to each list element
        // TODO: Each element of the list should disappear at the exact received datetime
        // received from the server response
        timersListener = response -> {
            Log.d(TAG, response.toString());

            CustomAdapter adapter = new CustomAdapter(this, response);
            listview.setAdapter(adapter);
        };

        // RaspVan request class
        req = new RaspVanRequests(this);

        // check the current lights state to adjust the switches
        req.getTimers(timersListener);
    }

    public void showPicker(){
        Calendar now = Calendar.getInstance();
        // Create timePicker dialogue and pass a callback function (as a lambda function)
        MyTimePickerDialog mTimePicker;
        mTimePicker = new MyTimePickerDialog(this, (view, signal, light, hours, minutes, seconds) -> {

            String timePicked = getString(R.string.time) +
                    String.format("%02d", hours)+
                    ":" + String.format("%02d", minutes) +
                    ":" + String.format("%02d", seconds);

            Log.i(TAG, "Delay => " + timePicked);
            Log.i(TAG, "Light signal => " + signal);
            Log.i(TAG, "Light index => " + light);
            Toast.makeText(context,"L:" + light +
                            " Signal:" + signal +
                            " T:" + timePicked,
                            Toast.LENGTH_SHORT).show();

            // TODO: get all parameters to build a proper request
            req.setTimerRequest("main", false,
                    hours * 3600 + 60 * minutes + seconds);
        }, now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);

        mTimePicker.show();
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

    public class CustomAdapter extends BaseAdapter {

        private Context mContext;
        private JSONArray timers;

        public CustomAdapter(Context context, JSONArray timers) {
            this.mContext = context;
            this.timers = timers;
            Log.d(TAG, "Timers in CustomAdapter: " + timers);

        }

        public int getCount() {
            // TODO Auto-generated method stub
            return this.timers.length();
        }

        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.timer_row, parent, false);

            try {
                JSONArray timer = (JSONArray) this.timers.get(position);
                String name = timer.get(0).toString();
                String signal = timer.get(1).toString();
                String date = timer.get(2).toString();
                String day = date.split("T")[0];
                String hour = date.split("T")[1].split("\\.")[0];
                Log.d(TAG, "Timer pos=" + position + ": " + timer);

                ImageView i1 = row.findViewById(R.id.imgIcon);
                TextView title = row.findViewById(R.id.txtTitle);
                title.setText(name + " | " + day + " | " + hour);
                if (signal.equals("ON"))
                    i1.setImageResource(R.drawable.bulb_on);
                else
                    i1.setImageResource(R.drawable.bulb_off);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (row);
        }
    }

}
