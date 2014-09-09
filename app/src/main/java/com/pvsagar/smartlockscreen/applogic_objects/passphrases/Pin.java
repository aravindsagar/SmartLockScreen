package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 8/9/14.
 */
public class Pin extends Passphrase<Integer> {
    public Pin(){
        super(Passphrase.TYPE_PIN);
    }

    public Pin(int pin){
        super(Passphrase.TYPE_PIN, pin);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(Integer passphrase) {
        return String.valueOf(passphrase);
    }

    @Override
    protected Integer getPassphraseRepresentationFromPassphraseString(String passphrase) {
        return Integer.parseInt(passphrase);
    }
}
