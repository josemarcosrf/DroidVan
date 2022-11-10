package project.van.fionaremote;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class WebControlPanelActivity extends BaseLayout {

    private static final String TAG = "PhionaWebControlPanel";

    private SharedPreferences sharedPref;
    private Context context;
    private Activity activity;

    private WebView wv;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_control_panel);
        super.onCreateDrawer();

        context = this;
        activity = this;

        // Shared Preferences where to store app settings (IP, port, ...)
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.settings_file_key), Context.MODE_PRIVATE);

        String urlToLoad = "http://" + getIP() + "/GumCP/index.php";
        // Toast.makeText(this, "url: " + urlToLoad, Toast.LENGTH_LONG).show();

        wv = findViewById(R.id.web_control_panel_view);
        wv.setWebViewClient(new CustomWebViewClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.canGoBack();

        this.loadResource(wv, urlToLoad);

    }

    private void loadResource(WebView wv, String resource) {
        wv.loadUrl(resource);
        wv.getSettings().setJavaScriptEnabled(true);
    }

    private String getIP() {
        String ipKey = this.context.getResources().getString(R.string.raspvan_ip);
        String defaultIP = this.context.getResources().getString(R.string.sample_ip);
        String address = this.sharedPref.getString(ipKey, defaultIP);
        return address;
    }

    private class CustomWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        public void onLoadResource(WebView view, String url) {
            // Check to see if there is a progress dialog
            if (progressDialog == null) {
                // If no progress dialog, make one and set message
                progressDialog = new ProgressDialog(activity);
                progressDialog.setMessage("Loading please wait...");
                progressDialog.show();

                // Hide the webview while loading
                wv.setEnabled(false);
            }
        }

        public void onPageFinished(WebView view, String url) {
            // Page is done loading;
            // hide the progress dialog and show the webview
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                wv.setEnabled(true);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            //Your code to do
            Toast.makeText(activity, "Your Internet Connection May not be active Or " + error , Toast.LENGTH_LONG).show();
        }
    }
}


