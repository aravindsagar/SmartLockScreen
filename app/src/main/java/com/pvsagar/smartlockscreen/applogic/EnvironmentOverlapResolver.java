package com.pvsagar.smartlockscreen.applogic;

import android.content.Context;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aravind on 8/10/14.
 * Class containing methods to find the best environment when there's an overlap
 * (ie multiple environments detected at once)
 */
public class EnvironmentOverlapResolver {
    private static final String LOG_TAG = EnvironmentOverlapResolver.class.getSimpleName();

    public static class PassphraseComparator implements Comparator<Passphrase>{
        @Override
        public int compare(Passphrase lhs, Passphrase rhs) {
            int typeCompare = comparePassphraseType(lhs.getPassphraseType(), rhs.getPassphraseType());
            if(typeCompare != 0){
                return typeCompare;
            }
            if(lhs.getPassphraseType().equals(Passphrase.TYPE_PASSWORD)){
                return comparePasswords((String) lhs.getPassphraseRepresentation(), (String) rhs.getPassphraseRepresentation());
            } else if(lhs.getPassphraseType().equals(Passphrase.TYPE_PIN)){
                return comparePin((String) lhs.getPassphraseRepresentation(), (String) rhs.getPassphraseRepresentation());
            } else if(lhs.getPassphraseType().equals(Passphrase.TYPE_PATTERN)){
                return comparePattern((List<Integer>) lhs.getPassphraseRepresentation(), (List<Integer>) rhs.getPassphraseRepresentation());
            }
            return 0;
        }

        public int comparePassphraseType(String lhs, String rhs){
            return getPassphraseTypeOrderNumber(lhs) - getPassphraseTypeOrderNumber(rhs);
        }

        public int comparePasswords(String lhs, String rhs){
            if(lhs.length() != rhs.length()){
                return lhs.length() - rhs.length();
            }
            int lhsSpecial = 0, rhsSpecial = 0;
            boolean hasLowerInLhs = false, hasLowerInRhs = false, hasUpperInLhs = false, hasUpperInRhs = false;
            for(char c:lhs.toCharArray()){
                if(Character.isUpperCase(c)){
                    hasUpperInLhs = true;
                } else if(Character.isLowerCase(c)){
                    hasLowerInLhs = true;
                } else {
                    lhsSpecial++;
                }
            }
            for(char c:rhs.toCharArray()){
                if(Character.isUpperCase(c)){
                    hasUpperInRhs = true;
                } else if(Character.isLowerCase(c)){
                    hasLowerInRhs = true;
                } else {
                    rhsSpecial++;
                }
            }
            lhsSpecial += ((hasLowerInLhs?1:0) + (hasUpperInLhs?1:0));
            rhsSpecial += ((hasLowerInRhs?1:0) + (hasUpperInRhs?1:0));

            return lhsSpecial - rhsSpecial;
        }

        public int comparePin(String lhs, String rhs){
            return lhs.length() - rhs.length();
        }

        public int comparePattern(List<Integer> lhs, List<Integer> rhs){
            return lhs.size() - rhs.size();
        }

        private int getPassphraseTypeOrderNumber(String passphraseType){
            if(passphraseType.equals(Passphrase.TYPE_PASSWORD)){
                return 40;
            }
            if(passphraseType.equals(Passphrase.TYPE_PIN)){
                return 30;
            }
            if(passphraseType.equals(Passphrase.TYPE_PATTERN)){
                return 20;
            }
            if(passphraseType.equals(Passphrase.TYPE_NONE)){
                return 10;
            }
            throw new IllegalArgumentException("String must be a passphrase type");
        }
    }

    public static void setPreferredEnvironmentFirst(List<Environment> overlappingEnvironments, final Context context){
        if (overlappingEnvironments == null) {
            return;
        }
        long preferredEnvironmentId = SharedPreferencesHelper.getEnvironmentOverlapChoice(overlappingEnvironments, context);
        if(preferredEnvironmentId > 0){
            int swap1 = 0, swap2 = 0;
            for (int i = 0; i < overlappingEnvironments.size(); i++) {
                Environment e = overlappingEnvironments.get(i);
                if(e.getId() == preferredEnvironmentId){
                    swap2 = i;
                    break;
                }
            }
            Collections.swap(overlappingEnvironments, swap1, swap2);
            return;
        }
        Log.d(LOG_TAG, "Sorting environments based on their password");
        Collections.sort(overlappingEnvironments, new Comparator<Environment>() {
            @Override
            public int compare(Environment lhs, Environment rhs) {
                return -1 * new PassphraseComparator().compare(
                        User.getDefaultUser(context).getPassphraseForEnvironment(context, lhs),
                        User.getDefaultUser(context).getPassphraseForEnvironment(context, rhs));
            }
        });
        SharedPreferencesHelper.setEnvironmentOverlapChoice(overlappingEnvironments,
                overlappingEnvironments.get(0).getId(), context);
    }
}
