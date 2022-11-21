package project.van.fionaremote;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FionaRemote extends Application {
    private final String TAG = "FionaRemoteAPP";
    // Bluetooth
    BTClient btClient;
    // Thread variables
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public FionaRemote() {
        Log.d(TAG , "Creating FionaRemote APP");
    }

    void prepareBT(BTCallback callback) {
        btClient = new BTClient(executorService, mainThreadHandler);
        boolean btFound = btClient.findDevice("raspberrypi", callback);
        if (btFound) {
            btClient.pairWith("raspberrypi", callback);
            btClient.connect(getRPIServerUUID(), callback);
        } else {
            Toast.makeText(this, "Closing BT connection", Toast.LENGTH_SHORT).show();
        }
    }

    UUID getRPIServerUUID() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                this.getString(R.string.settings_file_key), Context.MODE_PRIVATE);
        String BtKey = this.getResources().getString(R.string.rpi_bt_uuid);
        String BtServerUUID = this.getResources().getString(R.string.sample_uuid);
        String uuidStr = sharedPref.getString(BtKey, BtServerUUID);
        return UUID.fromString(uuidStr);
    }

    public BTClient getBtClient() {
        return btClient;
    }

   public void callServerLightState(BTCallback callback) {
        String payload = "{\"cmd\": \"/read\"}";
        btClient.request(payload, callback);
    }
}
