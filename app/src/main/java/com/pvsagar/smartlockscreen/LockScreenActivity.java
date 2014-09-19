package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.receivers.AdminActions;
import com.pvsagar.smartlockscreen.backend_helpers.WakeLockHelper;
import com.pvsagar.smartlockscreen.receivers.ScreenReceiver;

public class LockScreenActivity extends Activity implements GestureDetector.OnGestureListener {
    private static final String LOG_TAG = LockScreenActivity.class.getSimpleName();
    private static final int REQUEST_ENTER_PATTERN = 33;
    private static final String LOG_TAG = LockScreenActivity.class.getSimpleName();
    private static final String PACKAGE_NAME = "com.pvsagar.smartlockscreen";
    private static final String KEY_LOCKSCREEN_FRAGMENT = PACKAGE_NAME + ".keys.LOCKSCREEN_FRAGMENT";
    LockScreenFragment lockScreenFragment;
    GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "LockscreenActivity onCreate()");
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_lock_screen);
        if (savedInstanceState == null) {
            lockScreenFragment = new LockScreenFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, lockScreenFragment)
                    .commit();
        } else{
            lockScreenFragment = (LockScreenFragment) savedInstanceState.get(KEY_LOCKSCREEN_FRAGMENT);
        }*/
        setContentView(R.layout.fragment_lock_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount((float) 0.4);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        int systemUiVisibilityFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibilityFlags = systemUiVisibilityFlags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilityFlags);
//        startService(BaseService.getServiceIntent(this, null, null));

        //Gesture Detector
        gestureDetectorCompat = new GestureDetectorCompat(this,this);

        //Unlock button
        Button unlockButton = (Button) findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlock();
            }
        });
    }

    @Override
    protected void onResume() {
        WakeLockHelper.releaseWakeLock(ScreenReceiver.WAKELOCK_TAG);
        super.onResume();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lock_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(LOG_TAG, "on down. ");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(LOG_TAG, "on showpress ");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(LOG_TAG, "on singleTapUp ");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(LOG_TAG, "on scroll ");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.v(LOG_TAG,"Fling");
        unlock();
        return true;
    }
    private void unlock(){
        String currentPassphraseType = AdminActions.getCurrentPassphraseType();
        if (!Utility.checkForNullAndWarn(currentPassphraseType, LOG_TAG)){
            if (currentPassphraseType.equals(Passphrase.TYPE_PATTERN)) {
                Intent patternIntent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                        this, LockPatternActivity.class);
                patternIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                patternIntent.putExtra(LockPatternActivity.EXTRA_THEME, R.style.TransparentThemeNoActionBar);
                if (AdminActions.getCurrentPassphraseString() != null) {
                    patternIntent.putExtra(LockPatternActivity.EXTRA_PATTERN,
                            AdminActions.getCurrentPassphraseString().toCharArray());
                    startActivityForResult(patternIntent, REQUEST_ENTER_PATTERN);
                    //                        getActivity().overridePendingTransition(0, 0);
                } else {
                    this.finish();
                }
            } else if(currentPassphraseType.equals(Passphrase.TYPE_NONE)) {
                dismissKeyguard();
            } else {
                this.finish();
            }
        } else {
            this.finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENTER_PATTERN: {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        AdminActions.changePassword("", Passphrase.TYPE_NONE);
                        dismissKeyguard();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user cancelled the task
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        // The user failed to enter the pattern
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        break;
                }
                int retryCount = data.getIntExtra(
                        LockPatternActivity.EXTRA_RETRY_COUNT, 0);

                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void dismissKeyguard(){
        Intent intent = new Intent(this, DismissKeyguardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
        this.overridePendingTransition(0, 0);
        this.finish();
        this.overridePendingTransition(0, 0);
    }
}
