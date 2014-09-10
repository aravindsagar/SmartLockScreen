package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 8/9/14.
 */
public class Pin extends Passphrase<String> {
    public Pin(){
        super(Passphrase.TYPE_PIN);
    }

    public Pin(String pin){
        super(Passphrase.TYPE_PIN, pin);
        Integer.parseInt(pin); //Throws numberFormatException if its not a valid number
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
    public void setPasswordRepresentation(String passphrase) {
        Integer.parseInt(passphrase); //Throws numberFormatException if its not a valid number
        super.setPasswordRepresentation(passphrase);
    }
}
