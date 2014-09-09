package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 9/9/14.
 * A passphrase type which represents no password
 */
public class NoSecurity extends Passphrase<Void> {
    public NoSecurity(){
        super(Passphrase.TYPE_NONE);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(Void passphrase) {
        return "";
    }

    @Override
    protected Void getPassphraseRepresentationFromPassphraseString(String passphrase) {
        return null;
    }
}
