package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic.EnvironmentDetector;
import com.pvsagar.smartlockscreen.services.BaseService;


public class DismissKeyguardActivity extends Activity {
    private static final String LOG_TAG = DismissKeyguardActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss_keyguard);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        int systemUiVisibilityFlags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibilityFlags = systemUiVisibilityFlags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilityFlags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new WaitTask().execute();
    }

    private class WaitTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(100);
                while (true){
                    KeyguardManager keyguardManager = (KeyguardManager) DismissKeyguardActivity.this.getSystemService(KEYGUARD_SERVICE);
                    if(keyguardManager.isKeyguardSecure()){
                        Log.d(LOG_TAG, "keyguard locked");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DismissKeyguardActivity.this, "keyguard locked", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.d(LOG_TAG, "keyguard unlocked");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DismissKeyguardActivity.this, "keyguard unlocked", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EnvironmentDetector.manageEnvironmentDetectionCriticalSection.release();
            startService(BaseService.getServiceIntent(getBaseContext(), null, BaseService.ACTION_DETECT_ENVIRONMENT));
            finish();
            overridePendingTransition(0, 0);
            startService(BaseService.getServiceIntent(getBaseContext(), null, BaseService.ACTION_DISMISS_PATTERN_OVERLAY));

        }

        @Override
        protected void onCancelled(Void aVoid) {
            EnvironmentDetector.manageEnvironmentDetectionCriticalSection.release();
            startService(BaseService.getServiceIntent(getBaseContext(), null, BaseService.ACTION_DISMISS_PATTERN_OVERLAY));
        }
    }
}
