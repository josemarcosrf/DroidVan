package project.van.fionaremote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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
        String msg = "Response from BTClient: " + result;
        Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
        try {
            boolean isError = result.getBoolean("ok");
            if (isError)
                Log.e(TAG, msg);
            else
                // TODO: Update light state
                Log.d(TAG, msg);
        } catch (JSONException e) {
            Log.e(TAG, "Error reading BTResponse JSON object");
        }
    }
}
