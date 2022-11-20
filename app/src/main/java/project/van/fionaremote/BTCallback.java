package project.van.fionaremote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BTCallback implements BTCallbackInterface {
    private final Context ctx;
    private final String TAG = "BTCallback";

    public BTCallback(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onComplete(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            if (isOk) {
                // JSONArray keys = result.names();
                // Perhaps iterate over keys instead?
                try {
                    String msg = result.getString("msg");
                    Toast.makeText(this.ctx, "Response from BTClient:" + msg, Toast.LENGTH_SHORT).show();
                } catch (JSONException ignored) {}
                try {
                    JSONArray lState = result.getJSONArray("state");
                    String msg = "Lights: " + lState;
                    Log.d(TAG, msg);
                    Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
                    // Update switches
                    ((LightSwitchActivity)ctx).updateLightSwitches(lState);

                } catch (JSONException ignored) {}
            }
            else {
                String notOkReason = result.getString("error");
                String msg = "BT Server returned error: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading BTResponse JSON object");
        }
    }
}
