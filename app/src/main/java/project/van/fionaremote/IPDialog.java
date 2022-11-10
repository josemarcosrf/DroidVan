package project.van.fionaremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class IPDialog {

    private static final String TAG = "PhionaIPDialog";
    private Context context;
    private String settingsName;


    public IPDialog(Context context) {
        this.context = context;
        this.settingsName = context.getResources().getString(R.string.settings_file_key);
    }

    public void show_ip_dialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this.context);
        View promptsView = li.inflate(R.layout.dialog_set_ip, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);

        // set dialog_set_ip.xml to alertDialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText etAddr = promptsView
                .findViewById(R.id.ip_dialog_text);

        final EditText etPort = promptsView
                .findViewById(R.id.port_dialog_text);

        String address = this.getAddress();
        String port = this.getPort();

        etAddr.setText(address);
        etPort.setText(port);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.ip_settings)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        (dialog, id) -> {
                            String address1 = etAddr.getText().toString();
                            String port1 = etPort.getText().toString();
                            setAddress(address1);
                            setPort(port1);
                            Log.d(TAG, "IP is =>" + address1 + ":" + port1);
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public String getAddress() {
        String ipKey = context.getResources().getString(R.string.raspvan_ip);
        String defaultIP = context.getResources().getString(R.string.sample_ip);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        String address = sharedPref.getString(ipKey, defaultIP);
        return address;
    }

    public String getPort() {
        String portKey = context.getResources().getString(R.string.raspvan_port);
        String defaultPort = context.getResources().getString(R.string.sample_port);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        String port = sharedPref.getString(portKey, defaultPort);
        return port;
    }

    public void setAddress(String address) {
        String ipKey = context.getResources().getString(R.string.raspvan_ip);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ipKey, address);
        editor.commit();
    }

    public void setPort(String port) {
        String portKey = context.getResources().getString(R.string.raspvan_port);
        SharedPreferences sharedPref = context.getSharedPreferences(settingsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(portKey, port);
        editor.commit();
    }
}
