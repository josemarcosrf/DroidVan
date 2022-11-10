package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class ConnectionDialog {

    private static final String TAG = "FionaRPIConnectionDialog";
    private Context context;
    private String settingsName;


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

        final EditText etAddr = promptsView.findViewById(R.id.conn_rpi_dialog_text);


        String address = this.getAddress();

        etAddr.setText(address);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.conn_rpi_settings)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        (dialog, id) -> {
                            String bt_server_uuid = etAddr.getText().toString();
                            setAddress(bt_server_uuid);
                            Log.d(TAG, "RPI UUID is =>" + bt_server_uuid);
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public String getAddress() {
        String BtKey = context.getResources().getString(R.string.rpi_bt_uuid);
        String defaultBtServerUUID = context.getResources().getString(R.string.sample_uuid);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        String address = sharedPref.getString(BtKey, defaultBtServerUUID);
        return address;
    }

    public void setAddress(String uuid) {
        String BtKey = context.getResources().getString(R.string.rpi_bt_uuid);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(BtKey, uuid);
        editor.commit();
    }
}
