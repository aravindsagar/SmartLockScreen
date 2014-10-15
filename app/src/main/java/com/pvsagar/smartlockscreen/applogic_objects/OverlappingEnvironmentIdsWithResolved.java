package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 9/10/14.
 * A class to store a list of overlapping environment ids, along with the chosen (resolved) environment
 * id to use from them.
 */
public class OverlappingEnvironmentIdsWithResolved{
    private List<Long> overlappingEnvIds;
    private long resolvedEnvId;
    int resolvedEnvIndex = -1;

    public long getResolvedEnvId() {
        return resolvedEnvId;
    }

    public void setResolvedEnvId(long resolvedEnvId) {
        this.resolvedEnvId = resolvedEnvId;
    }

    public List<Long> getOverlappingEnvIds() {
        return overlappingEnvIds;
    }

    public void setOverlappingEnvIds(List<Long> overlappingEnvIds) {
        this.overlappingEnvIds = overlappingEnvIds;
    }

    public List<Environment> getBareboneEnvironmentList(Context context){
        if (overlappingEnvIds == null) {
            return null;
        }
        ArrayList<Environment> environments = new ArrayList<Environment>();
        for (int i = 0; i < overlappingEnvIds.size(); i++) {
            long envId = overlappingEnvIds.get(i);
            environments.add(SimpleEnvironmentRetrievalFactory.getBareboneEnvironmentWithId(envId, context));
            if(envId == resolvedEnvId){
                resolvedEnvIndex = i;
            }
        }
        return environments;
    }

    public int getResolvedEnvIndex(){
        return resolvedEnvIndex;
    }

    public static class SimpleEnvironmentRetrievalFactory {
        private static ArrayList<Environment> environments = new ArrayList<Environment>();
        public static Environment getBareboneEnvironmentWithId(Long id, Context context){
            for(Environment e: environments){
                if(e.getId() == id){
                    return e;
                }
            }
            Environment environment = Environment.getBareboneEnvironment(context, id);
            environments.add(environment);
            return environment;
        }
    }
}

