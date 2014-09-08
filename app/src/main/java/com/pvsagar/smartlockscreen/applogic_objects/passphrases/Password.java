package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 8/9/14.
 * Password class to deal with alphanumeric passwords.
 */
public class Password extends Passphrase<String> {

    public Password(){
        super(Passphrase.TYPE_PASSWORD);
    }

    public Password(String password){
        super(Passphrase.TYPE_PASSWORD, password);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(String passphrase) {
        return passphrase;
    }

    @Override
    protected String getPassphraseRepresentationFromPassphraseString(String passphrase) {
        return passphrase;
    }
}
