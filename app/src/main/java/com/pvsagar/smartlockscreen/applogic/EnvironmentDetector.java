package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.os.AsyncTask;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Created by aravind on 6/9/14.
 */
public class EnvironmentDetector {
    private static final Semaphore manageEnvironmentDetectionCriticalSection = new Semaphore(1);

    public Environment detectCurrentEnvironment(Context context){
        try {
            return (new EnvironmentDetectorAsyncTask()).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class EnvironmentDetectorAsyncTask extends AsyncTask<Void, Void, Environment>{

        @Override
        protected Environment doInBackground(Void... params) {
            manageEnvironmentDetectionCriticalSection.acquireUninterruptibly();
            manageEnvironmentDetectionCriticalSection.release();
            return null;
        }
    }
}
