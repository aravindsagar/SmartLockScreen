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

    /**
     * A list of environment ids corresponding to a set of overlapping environments
     */
    private List<Long> overlappingEnvIds;

    /**
     * The id of the environment which is preferred(resolved) among the overlapping environments
     */
    private long resolvedEnvId;

    /**
     * Index of the preferred environment's id in the overlapping environment list
     */
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

    /**
     * Get barebone environments of all the environment ids in this instance
     * @param context Activity/Service context
     * @return barebone environments
     */
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

    /**
     * A simple factory class which stores environments retrieved from the database in memory and reuses those instances,
     * instead of fetching them again from the database when the same environment is requested to be read multiple times.
     */
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

