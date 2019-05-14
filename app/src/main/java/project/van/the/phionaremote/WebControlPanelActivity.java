package project.van.the.phionaremote;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebControlPanelActivity extends BaseLayout {

    private static final String TAG = "PhionaWebControlPanel";
    private static final String LOCAL_RESOURCE = "file:///android_asset/html/HelloWorld.html";

    private SharedPreferences sharedPref;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_control_panel);
        super.onCreateDrawer();

        context = this;

        // Shared Preferences where to store app settings (IP, port, ...)
        sharedPref = context.getSharedPreferences(
                context.getString(R.string.settings_file_key), Context.MODE_PRIVATE);

        String urlToLoad = getIP() + "/GumCP/index.php";

        WebView wv = findViewById(R.id.web_control_panel_view);
        wv.setWebViewClient(new CustomWebViewClient());
        wv.getSettings().setJavaScriptEnabled(true);

        this.loadResource(wv, "https:google.com");

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
    }
}


