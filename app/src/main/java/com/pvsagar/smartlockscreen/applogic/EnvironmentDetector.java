package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.os.AsyncTask;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.services.GeoFenceIntentService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Created by aravind on 6/9/14.
 */
public class EnvironmentDetector {
    private static final Semaphore manageEnvironmentDetectionCriticalSection = new Semaphore(1);

    public Environment detectCurrentEnvironment(Context context){
        try {
            return (new EnvironmentDetectorAsyncTask()).execute(context).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class EnvironmentDetectorAsyncTask extends AsyncTask<Context, Void, Environment>{

        @Override
        protected Environment doInBackground(Context... params) {
            if(params.length == 0 || params[0] == null){
                throw new IllegalArgumentException
                        ("The current activity/service context should be passed as argument.");
            }
            manageEnvironmentDetectionCriticalSection.acquireUninterruptibly();
            for(LocationEnvironmentVariable location: GeoFenceIntentService.getCurrentGeofences()){
                List<Environment> potentialEnvironments = Environment.
                        getAllEnvironmentBarebonesForLocation(params[0], location);
            }
            manageEnvironmentDetectionCriticalSection.release();
            return null;
        }
    }
}
