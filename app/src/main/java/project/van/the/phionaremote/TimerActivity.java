package project.van.the.phionaremote;

import project.van.the.phionaremote.TimePicker.MyTimePickerDialog;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPicker();
            }
        });

        // On response from Timers Server populate the listView
        final ListView listview = (ListView) findViewById(R.id.timer_listview);

        // TODO: Change this overriding of the Adapter. Make a non-static adapter
        // TODO: Change the tipe of list item to just text
        // TODO: Add onClickListener to each list element
        // TODO: Each element of the list should dissapear at the exact received datetime
        // received from the server response
        timersListener = response -> {
            Log.d(TAG, response.toString());

            ArrayList<String> receivedTimers = new ArrayList<>();
            for (int i = 0; i < response.length(); i++)
            {
                try {
                    receivedTimers.add(response.get(i).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Timers List: " + receivedTimers);

            StableArrayAdapter newAdapter = new StableArrayAdapter(this,
                    android.R.layout.simple_list_item_1, receivedTimers);
            listview.setAdapter(newAdapter);
        };

        // RaspVan request class
        req = new RaspVanRequests(this);

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
