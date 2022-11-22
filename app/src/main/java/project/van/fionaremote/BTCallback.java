package project.van.fionaremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


// The callback interface
interface BTCallbackInterface {

    void onBTNotEnabled();

    boolean onConnect(@NonNull JSONObject result);

    void onMessage(JSONObject result);

    void onServerStateUpdate(@NonNull JSONObject result);
}


public class BTCallback implements BTCallbackInterface {
    private final Context ctx;
    private final String TAG = "BTCallback";

    public BTCallback(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onBTNotEnabled() {
        Log.w(TAG, "Adapter is not enabled");
        int REQUEST_ENABLE_BT = 0;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((Activity)ctx).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public boolean onConnect(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            if (isOk) {
                Toast.makeText(this.ctx,
                        "Successfully connected to RPI BT server",
                        Toast.LENGTH_SHORT).show();
            } else {
                String notOkReason = result.getString("error");
                String msg = "Error connecting to BT server: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
            return isOk;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onServerStateUpdate BTResponse JSON object");
            return false;
        }
    }

    @Override
    public void onServerStateUpdate(@NonNull JSONObject result) {}

    @Override
    public void onMessage(@NonNull JSONObject result) {
        /**
         * This is a generic result notifier method. Mostly to debug and show Toasts
         * */
        try {
            boolean isOk = result.getBoolean("ok");
            if (isOk) {
                String msg = result.getString("msg");
                Toast.makeText(this.ctx,
                        "Response from BTClient:" + msg,
                        Toast.LENGTH_SHORT).show();
            }
            else {
                String notOkReason = result.getString("error");
                String msg = "BT Server returned error: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onMessage BTResponse JSON object");
        }
    }
}
