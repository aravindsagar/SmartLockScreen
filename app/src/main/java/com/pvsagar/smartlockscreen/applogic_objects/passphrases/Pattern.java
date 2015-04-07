package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import android.app.NotificationManager;
import android.content.Context;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.backend_helpers.RootHelper;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.frontend_helpers.NotificationHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 17/9/14.
 */
public class Pattern extends Passphrase<List<Integer>> {

    public Pattern(){
        super(TYPE_PATTERN);
    }

    public Pattern(List<Integer> pattern){
        super(TYPE_PATTERN, pattern);
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
        if(SharedPreferencesHelper.isRootPattern(context)){
            Passphrase.getMasterPassword(context).setAsCurrentPassword(context);
            if(getPassphraseRepresentation().size() <= 2) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NotificationHelper.ENTER_PATTERN_NOTIFICATION_ID,
                        NotificationHelper.getAppNotification(context, context.getString(R.string.enter_pattern_again)));
                return true;
            }
            if(RootHelper.setCurrentPattern(context, this)) return true;
        }
        return super.setAsCurrentPassword(context);
    }

    private byte[] getPatternBytes(){
        List<Integer> pattern = getPassphraseRepresentation();
        byte[] patternBytes = new byte[pattern.size()];
        for (int i = 0; i < pattern.size(); i++) {
            patternBytes[i] = (byte) ((int)pattern.get(i));
        }
        return patternBytes;
    }

    public String toSystemString(){
        return new String(getPatternBytes());
    }

    public byte[] toSystemHash(){
        byte[] patternBytes = getPatternBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(patternBytes);
        } catch (NoSuchAlgorithmException nsa) {
            return patternBytes;
        }
    }
}
