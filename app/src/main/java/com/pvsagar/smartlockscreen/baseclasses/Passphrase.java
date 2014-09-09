package com.pvsagar.smartlockscreen.baseclasses;

/**
 * Created by aravind on 10/8/14.
 */
public abstract class Passphrase {
    private String passwordString, encryptedPasswordString;

    //User friendly password type strings
    public static final String[] passphraseTypes = {"Password","Pin"};
    public static final int INDEX_PASSPHRASE_TYPE_PASSWORD = 0;
    public static final int INDEX_PASSPHRASE_TYPE_PIN = 1;

    public void setPasswordString(String passwordString){
        this.passwordString = passwordString;
        //TODO add encryption
        encryptedPasswordString = passwordString;
    }
}
