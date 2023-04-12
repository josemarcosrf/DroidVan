package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class ConnectionDialog {

    private static final String TAG = "FionaConnectionDialog";
    private final Context context;
    private final String settingsName;


    public ConnectionDialog(Context context) {
        this.context = context;
        this.settingsName = context.getResources().getString(R.string.settings_file_key);
    }

    public void showConnectionDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this.context);
        View promptsView = li.inflate(R.layout.dialog_connection_rpi, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);

        // set dialog_set_ip.xml to alertDialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText serverUUID = promptsView.findViewById(R.id.conn_uuid_text);
        final EditText serverDeviceName = promptsView.findViewById(R.id.conn_device_name_text);

        String uuid = this.getServerUUID();
        String deviceName = this.getDeviceName();

        serverUUID.setText(uuid);
        serverDeviceName.setText(deviceName);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.conn_settings)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        (dialog, id) -> {
                            String _uuid = serverUUID.getText().toString();
                            String _deviceName = serverDeviceName.getText().toString();
                            setServerUUID(_uuid);
                            setDeviceName(_deviceName);
                            Log.d(TAG, "BT Server =>" + _deviceName + ":" + _uuid);
                            String bt_server_uuid = serverUUID.getText().toString();
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public String getServerUUID() {
        String BtKey = context.getResources().getString(R.string.bt_uuid);
        String defaultBtServerUUID = context.getResources().getString(R.string.sample_uuid);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        String address = sharedPref.getString(BtKey, defaultBtServerUUID);
        return address;
    }

    public void setServerUUID(String uuid) {
        String BtKey = context.getResources().getString(R.string.bt_uuid);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(BtKey, uuid);
        editor.commit();
    }

    public String getDeviceName() {
        String BtSettingKey = context.getResources().getString(R.string.bt_device_name);
        String defaultBtDeviceName = context.getResources().getString(R.string.sample_device_name);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        String deviceName = sharedPref.getString(BtSettingKey, defaultBtDeviceName);
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        String BtSettingKey = context.getResources().getString(R.string.bt_device_name);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(BtSettingKey, deviceName);
        editor.commit();
    }
}
