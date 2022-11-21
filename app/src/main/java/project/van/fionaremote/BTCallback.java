package project.van.fionaremote;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
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
    public void onBTNotEnabled() {
        Log.w(TAG, "Adapter is not enabled");
        int REQUEST_ENABLE_BT = 0;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((LightSwitchActivity) ctx).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onConnect(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            ((LightSwitchActivity) ctx).setConnMsgVisibility(!isOk);
            if (isOk) {
                Toast.makeText(this.ctx,
                        "Succesfully connected to RPI BT server",
                        Toast.LENGTH_SHORT).show();
                // Fetch light states
                Log.d(TAG, "Connection was Ok, now requesting light state...");
                ((LightSwitchActivity) ctx).requestLightState(null);
            } else {
                String notOkReason = result.getString("error");
                String msg = "Error connecting to BT server: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onSwitch BTResponse JSON object");
        }
    }

    @Override
    public void onSwitch(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            if (isOk) {
                JSONArray lState = result.getJSONArray("state");
                Log.d(TAG, "Lights: " + lState);
                // Update switches
                ((LightSwitchActivity) ctx).updateLightSwitches(lState);
            } else {
                String notOkReason = result.getString("error");
                String msg = "BT Server returned error: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onSwitch BTResponse JSON object");
        }
    }

    @Override
    public void onComplete(@NonNull JSONObject result) {
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
            Log.e(TAG, "Error reading onComplete BTResponse JSON object");
        }
    }
}
