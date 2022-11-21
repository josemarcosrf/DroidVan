package project.van.fionaremote;

import android.app.Activity;
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
    private final String TAG = "FionaBTCallback";

    public BTCallback(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onBTNotEnabled() {
        Log.w(TAG, "Adapter is not enabled");
        int REQUEST_ENABLE_BT = 0;
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((Activity) ctx).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onConnect(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            ((LightSwitchActivity) ctx).setConnMsgVisibility(!isOk);
            if (isOk) {
                String msg = "Succesfully connected to RPI BT server";
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
                // Fetch light states
                Log.d(TAG, msg);
                Log.d(TAG, String.valueOf(ctx));
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
                String msg = "BT Server returned error onSwitch: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onSwitch JSON object: " + e);
        }
    }

    @Override
    public void onSchedule(@NonNull JSONObject result) {
        try {
            boolean isOk = result.getBoolean("ok");
            if (isOk) {
                JSONArray schedules = result.getJSONArray("scheduled");
                Log.d(TAG, "schedules: " + schedules.length());
                // TODO: Update ListView
            } else {
                String notOkReason = result.getString("error");
                String msg = "BT Server returned error onSchedule: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onSchedule JSON object: " + e);
        }
    }

    @Override
    public void onComplete(@NonNull JSONObject result) {
        //This is a generic result notifier method. Mostly to debug and show Toasts
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
                String msg = "BT Server Error: " + notOkReason;
                Log.e(TAG, msg);
                Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading onComplete JSON object: " + e);
        }
    }
}
