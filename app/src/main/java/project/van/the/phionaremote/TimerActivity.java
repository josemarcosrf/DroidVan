package project.van.the.phionaremote;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

public class TimerActivity extends baseLayout {

    private static final String TAG = "PhionaNavActivity";

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        super.onCreateDrawer();
    }


}
