package com.pvsagar.smartlockscreen.backend_helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aravind on 4/12/14.
 */
public class AppUpdateManager {
    private static final String LOG_TAG = AppUpdateManager.class.getSimpleName();
    private static final String UPDATE_URL_STRING = "http://aravindsagar.github.io/SmartLockScreen/info/info.txt";

    private static final String KEY_VERSION_NAME = "versionName";
    private static final String KEY_VERSION_CODE = "versionCode";
    private static final String KEY_DOWNLOAD_LINK = "downloadLink";
    private static final String KEY_CHANGE_LOG_LINK = "changelogLink";

    private Context mContext;

    public AppUpdateManager(Context context){
        mContext = context;
    }
    public void checkForUpdates(OnUpdateCheckFinishedListener listener){
        ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadAppInfo().execute(listener);
        } else {
            listener.onUpdateCheckFailed();
        }
    }

    public class DownloadAppInfo extends AsyncTask<OnUpdateCheckFinishedListener, Void, AppInfo>{
        OnUpdateCheckFinishedListener listener;

        @Override
        protected AppInfo doInBackground(OnUpdateCheckFinishedListener... params) {
            listener = params[0];
            try {
                return downloadLatestVersionInfo();
            } catch (IOException e) {
                return null;
            }
        }

        private AppInfo downloadLatestVersionInfo() throws IOException {
            InputStream is = null;
            int len = 5000;
            try {
                URL url = new URL(UPDATE_URL_STRING);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();

                // Convert the InputStream into AppInfo
                return parseAppInfoFromInputStream(is, len);

                // Makes sure that the InputStream is closed after the app has finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        private AppInfo parseAppInfoFromInputStream(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            String appData = new String(buffer);
            String[] dataParts = appData.split("\n");
            AppInfo info = new AppInfo();
            boolean hasCode, hasName, hasDownloadLink, hasChangeLogLink;
            hasCode = hasName = hasDownloadLink = hasChangeLogLink = false;
            for(String dataPart:dataParts){
                if(dataPart.startsWith("#")){
                    continue;
                }
                if(dataPart.startsWith(KEY_VERSION_CODE)){
                    info.versionCode = Integer.parseInt(dataPart.split("=")[1]);
                    hasCode = true;
                } else if(dataPart.startsWith(KEY_VERSION_NAME)){
                    info.versionName = dataPart.split("=")[1];
                    hasName = true;
                } else if(dataPart.startsWith(KEY_CHANGE_LOG_LINK)){
                    info.changeLogUrl = new URL(dataPart.split("=")[1]);
                    hasChangeLogLink = true;
                } else if(dataPart.startsWith(KEY_DOWNLOAD_LINK)){
                    info.downloadUrl = new URL(dataPart.split("=")[1]);
                    hasDownloadLink = true;
                }
            }
            if(hasChangeLogLink && hasDownloadLink && hasCode && hasName){
                return info;
            }
            return null;
        }

        @Override
        protected void onPostExecute(AppInfo appInfo) {
            if(appInfo == null){
                if(listener != null) {
                    listener.onUpdateCheckFailed();
                }
            } else {
                SharedPreferencesHelper.setLatestVersionInfo(appInfo, mContext);
                if(listener != null) {
                    listener.onUpdateCheckFinished(appInfo);
                }
            }
        }
    }

    public static class AppInfo {

        public AppInfo(){

        }

        public AppInfo(int versionCode, String versionName, URL downloadUrl, URL changeLogUrl){
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.downloadUrl = downloadUrl;
            this.changeLogUrl = changeLogUrl;
        }

        public int versionCode;
        public String versionName;

        public URL downloadUrl, changeLogUrl;
    }

    public static interface OnUpdateCheckFinishedListener {
        public void onUpdateCheckFinished(AppInfo updateInfo);

        public void onUpdateCheckFailed();
    }
}
