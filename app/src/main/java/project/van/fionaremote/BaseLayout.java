package project.van.fionaremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

public class BaseLayout extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FionaBaseActivity";
    private DrawerLayout mDrawerLayout;

    ActionBarDrawerToggle toggle;

    protected void onCreateDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // The actual drawer we were looking for...
        mDrawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,                           /* host Activity */
                mDrawerLayout,                        /* DrawerLayout  */
                toolbar,                              /* menu toolbar view */
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public UUID getServerUUID() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                this.getString(R.string.settings_file_key), Context.MODE_PRIVATE);
        String settingKey = this.getResources().getString(R.string.bt_uuid);
        String BtServerUUID = this.getResources().getString(R.string.sample_uuid);
        String uuidStr = sharedPref.getString(settingKey, BtServerUUID);
        return UUID.fromString(uuidStr);
    }

    public String getBTDeviceName() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                this.getString(R.string.settings_file_key), Context.MODE_PRIVATE);
        String settingKey = this.getResources().getString(R.string.bt_device_name);
        String BtDeviceName = this.getResources().getString(R.string.sample_device_name);
        return sharedPref.getString(settingKey, BtDeviceName);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action when action_search was selected
            case R.id.rpi_settings:
                ConnectionDialog diag = new ConnectionDialog(this);
                diag.showConnectionDialog();
                break;
            default:
                Toast.makeText(this, "Default option...", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent intent;
        switch (item.getItemId()) {
            // Instead of creating new Activities we bring them to from if they exists
            // with the 'FLAG_ACTIVITY_REORDER_TO_FRONT' flag
            // https://stackoverflow.com/questions/15359124/resume-the-activity-instead-of-starting-if-already-exists-in-back-stack

            case R.id.light_switches:
                intent = new Intent(this, LightSwitchActivity.class);
                // finish();
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent);
                break;
            case R.id.light_timers:
                intent = new Intent(this, TimerActivity.class);
                // finish();
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent);
                break;
            /*case R.id.light_voice_ctl:
                intent = new Intent(this, VoiceLightCtlActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                // finish();
                this.startActivity(intent);
                break;*/
            /*case R.id.web_control_pannel:
                intent = new Intent(this, WebControlPanelActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                this.startActivity(intent);
                break;*/
            case R.id.settings:
                Toast.makeText(this, "No settings yet my friend. Ouch!", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Default Navigation Item...", Toast.LENGTH_SHORT).show();
                break;
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
