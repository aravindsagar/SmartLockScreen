package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 8/9/14.
 * Pin class which represents a numeric passphrase. String is used as representation to accommodate
 * potential zeroes in the beginning of the pin
 */
public class Pin extends Passphrase<String> {
    public Pin(){
        super(Passphrase.TYPE_PIN);
    }

    public Pin(String pin){
        super(Passphrase.TYPE_PIN, pin);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(String passphrase) {
        return passphrase;
    }

    @Override
    protected String getPassphraseRepresentationFromPassphraseString(String passphrase) {
        return passphrase;
    }

    @Override
    protected boolean isPassphraseRepresentationValid(String passphrase) {
        return passphrase!=null && !passphrase.isEmpty() && Utility.isInteger(passphrase);
    }
}
