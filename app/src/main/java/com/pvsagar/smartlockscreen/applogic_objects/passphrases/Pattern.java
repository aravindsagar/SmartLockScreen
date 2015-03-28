package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import android.content.Context;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.RootHelper;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 17/9/14.
 */
public class Pattern extends Passphrase<List<Integer>> {
    private static final String LOG_TAG = Pattern.class.getSimpleName();
    private boolean isRootPattern = false;

    public Pattern(){
        super(TYPE_PATTERN);
    }

    public Pattern(List<Integer> pattern){
        super(TYPE_PATTERN, pattern);
        if(pattern.size() < 2) {
            isRootPattern = true;
        }
    }

    public Pattern(boolean isRootPattern){
        this();
        this.isRootPattern = isRootPattern;
        if(isRootPattern){
            List<Integer> pattern = new ArrayList<>();
            pattern.add(0);
            this.setPasswordRepresentation(pattern);
        }
    }
    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(List<Integer> passphrase) {
        String passphraseString = "";
        for (Integer aPassphrase : passphrase) {
            passphraseString += aPassphrase + ":";
        }
        return passphraseString;
    }

    @Override
    protected List<Integer> getPassphraseRepresentationFromPassphraseString(String passphrase) {
        List<Integer> pattern  = new ArrayList<Integer>();
        String[] patternNumbers = passphrase.split(":");
        for (String patternNumber : patternNumbers) {
            pattern.add(Integer.parseInt(patternNumber));
        }
        return pattern;
    }

    @Override
    protected boolean isPassphraseRepresentationValid(List<Integer> passphrase) {
        return passphrase!=null && passphrase.size() > 0;
    }

    @Override
    public boolean setAsCurrentPassword(Context context) {
        Log.d(LOG_TAG, "Setting current password");
        if(isRootPattern){
            Passphrase.getMasterPassword(context).setAsCurrentPassword(context);
            return RootHelper.setCurrentPattern(context);
        }
        return super.setAsCurrentPassword(context);
    }

    public void setRootPattern(boolean isRootPattern){
        this.isRootPattern = isRootPattern;
    }

    @Override
    public void setPasswordRepresentation(List<Integer> passphrase) {
        super.setPasswordRepresentation(passphrase);
        if(passphrase.size() < 2) setRootPattern(true);
    }
}
