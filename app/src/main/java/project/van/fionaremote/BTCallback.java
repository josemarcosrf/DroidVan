package project.van.fionaremote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

public class BTCallback implements BTCallbackInterface {
    private final Context ctx;

    public BTCallback(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onComplete(String result) {
        String msg = "Response from BTClient: " + result;
        Log.d("BTCallback", msg);
        Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResult(JSONObject result) {
        // TODO
    }
}
