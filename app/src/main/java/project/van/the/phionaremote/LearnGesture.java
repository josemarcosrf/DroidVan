package project.van.the.phionaremote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Gesture Listener class...
 * (Should this be in every Activity to be able to listen to the gestures?)
 */
class LearnGesture extends GestureDetector.SimpleOnGestureListener {

    // Logging Activity tag
    private static final String TAG = "PhionaGestureActivity";

    // here just to make Toasts...
    private Context context;

    public LearnGesture(Context context){
        this.context = context;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        Log.d(TAG, "Inside onFling.....  e2: " + e2.getX() + " e1: " +  e1.getX());

        if (e2.getX() > e1.getX()) {
            onSwipeRight();
        }
        else {
            onOtherSwipe();
        }
        return true;
    }

    public void onSwipeRight() {
        Log.d(TAG, "Inside onSwipeRight....");
        Toast.makeText(context, "The good motion detected...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, ManualLightSwitchActivity.class);
//        context.finish();
        context.startActivity(intent);
    }

    public void onOtherSwipe() {
        Toast.makeText(context, "Some other motion detected...", Toast.LENGTH_SHORT).show();
    }
}
