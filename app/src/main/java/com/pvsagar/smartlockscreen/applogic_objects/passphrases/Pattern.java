package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

/**
 * Created by aravind on 17/9/14.
 */
public class Pattern extends Passphrase<char[]> {

    public Pattern(){
        super(TYPE_PATTERN);
    }

    public Pattern(char[] pattern){
        super(TYPE_PATTERN, pattern);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(char[] passphrase) {
        return new String(passphrase);
    }

    @Override
    protected char[] getPassphraseRepresentationFromPassphraseString(String passphrase) {
        return passphrase.toCharArray();
    }

    @Override
    protected boolean isPassphraseRepresentationValid(char[] passphrase) {
        return passphrase!=null && passphrase.length > 0;
    }

}
