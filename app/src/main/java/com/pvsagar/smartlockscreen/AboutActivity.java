package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.backend_helpers.AppUpdateManager;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;


public class AboutActivity extends Activity implements AppUpdateManager.OnUpdateCheckFinishedListener{

    private static final String LOG_TAG = AboutActivity.class.getSimpleName();
    private Button updateButton;
    private AppUpdateManager.AppInfo updateInfo;
    private int currentVersionCode;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_about);
        setupActionBar();
        TextView appNameView = (TextView) findViewById(R.id.text_view_app_name_version);
        String appName = getString(R.string.app_name);
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appName += (" v" + info.versionName);
            currentVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        appNameView.setText(appName);
        appNameView.setTextColor(Color.BLACK);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_update_check);

        updateInfo = SharedPreferencesHelper.getLatestVersionInfo(this);
        updateButton = (Button) findViewById(R.id.button_check_update);
        Button forumButton = (Button) findViewById(R.id.button_join_discussion);
        forumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://forum.xda-developers.com/android/apps-games/app-smartlockscreen-android-enjoy-t2919989"));
                startActivity(browserIntent);
            }
        });
        startUpdateCheck();

        setUpButtons();
    }

    private void setUpButtons() {
        if(updateInfo != null && updateInfo.versionCode > currentVersionCode) {
            updateButton.setText(getString(R.string.update_available) + updateInfo.versionName);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buildUpdateDialog().show();
                }
            });
        } else {
            updateButton.setText(R.string.check_update);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpdateCheck();
                }
            });
            if(updateInfo != null) {
                Log.d(LOG_TAG, "Current version code: " + currentVersionCode + ", update version code: " + updateInfo.versionCode);
            } else {
                Log.d(LOG_TAG, "Current version code: " + currentVersionCode);
            }
        }

    }

    private AlertDialog buildUpdateDialog() {
        return new AlertDialog.Builder(AboutActivity.this).setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl.toString()));
                startActivity(browserIntent);
            }
        }).setNeutralButton(R.string.view_change_log, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.changeLogUrl.toString()));
                startActivity(browserIntent);
            }
        }).setTitle(getString(R.string.update_available_dialog) + updateInfo.versionName).create();

    }

    private void startUpdateCheck(){
        setProgressBarIndeterminateVisibility(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        updateButton.setText(getString(R.string.checking_update));
        new AppUpdateManager(AboutActivity.this).checkForUpdates(AboutActivity.this);
    }

    private void setupActionBar() {
        // get the root container of the preferences list
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_about_activity);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bar.setTitle(R.string.title_activity_about);
        bar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public void onUpdateCheckFinished(AppUpdateManager.AppInfo updateInfo) {
        hideProgressBar();
        updateButton.setText(getString(R.string.check_update));
        this.updateInfo = updateInfo;
        setUpButtons();
        if(updateInfo != null && updateInfo.versionCode <= currentVersionCode) {
            Toast.makeText(this, getString(R.string.no_update_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateCheckFailed() {
        hideProgressBar();
        updateButton.setText(getString(R.string.check_update));
        Toast.makeText(this, getString(R.string.update_check_failed), Toast.LENGTH_SHORT).show();
    }

    private void hideProgressBar(){
        setProgressBarIndeterminateVisibility(false);
        progressBar.setVisibility(View.GONE);
    }
}
