package project.van.the.phionaremote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ai.snips.platform.SnipsPlatformClient;


public class VoiceLightCtlActivity extends BaseLayout  {

    // Logging Activity tag
    private static final String TAG = "PhionaVoiceLightCtl";
    private GestureDetectorCompat gestureObject;    // Gesture detector
    private RaspVanRequests req;
    private Response.Listener<JSONObject> lightsListener;

    private File assistantLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_ctl);

        // drawer stuff && swipe right to open navigation drawer
        super.onCreateDrawer();
        gestureObject = new GestureDetectorCompat(this, new LearnGesture(this));

        assistantLocation = new File(getFilesDir(), "snips");

        Log.d(TAG, "getExternal Storage Directory" + Environment.getExternalStorageDirectory().toString());
        Log.d(TAG, "Unzipping assistant file at:" + assistantLocation);

        extractAssistantIfNeeded(assistantLocation);
        if (ensurePermissions()) {
            startSnips(assistantLocation);
        }
    }

    private boolean ensurePermissions() {
        int status = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 0);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSnips(assistantLocation);
        }
    }

    private void startSnips(File snipsDir) {
        SnipsPlatformClient client = createClient(snipsDir);
        client.connect(this.getApplicationContext());
    }

    private void extractAssistantIfNeeded(File assistantLocation) {
        File versionFile = new File(assistantLocation,
                "android_version_" + BuildConfig.VERSION_NAME);

        if (versionFile.exists()) {
            return;
        }

        try {
            assistantLocation.delete();
            VoiceLightCtlActivity.unzip(getBaseContext().getAssets().open("assistant.zip"),
                    assistantLocation);
            versionFile.createNewFile();
        } catch (IOException e) {
            return;
        }
    }

    private SnipsPlatformClient createClient(File assistantLocation) {
        File assistantDir  = new File(assistantLocation, "assistant");

        final SnipsPlatformClient client =
                new SnipsPlatformClient.Builder(assistantDir)
                        .enableDialogue(true)
                        .enableHotword(true)
                        .enableSnipsWatchHtml(false)
                        .enableLogs(true)
                        .withHotwordSensitivity(0.5f)
                        .enableStreaming(false)
                        .enableInjection(false)
                        .build();

        client.setOnPlatformReady(() -> {
            Log.d(TAG, "Snips is ready. Say the wake word!");
            Toast.makeText(getApplicationContext(), "Snips is ready. Say the wake word!", Toast.LENGTH_SHORT).show();
            return null;
        });

        client.setOnPlatformError(
                snipsPlatformError -> {
                    // Handle error
                    Log.d(TAG, "Error: " + snipsPlatformError.getMessage());
                    return null;
                });

        client.setOnHotwordDetectedListener(() -> {
            // Wake word detected, start a dialog session
            Log.d(TAG, "Wake word detected!");
            Toast.makeText(getApplicationContext(), "Wake word detected!", Toast.LENGTH_SHORT).show();
            client.startSession(null, new ArrayList<String>(),
                    false, null);
            return null;
        });

        client.setOnIntentDetectedListener(intentMessage -> {
            // Intent detected, so the dialog session ends here
            client.endSession(intentMessage.getSessionId(), null);
            Toast.makeText(getApplicationContext(), "Intent detected: " +
                    intentMessage.getIntent().getIntentName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Intent detected: " +
                    intentMessage.getIntent().getIntentName());
            return null;
        });

        client.setOnSnipsWatchListener(s -> {
            Log.d(TAG, "Log: " + s);
            return null;
        });

        return client;
    }

    private static void unzip(InputStream zipFile, File targetDirectory)
            throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFile));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to make directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }
}
