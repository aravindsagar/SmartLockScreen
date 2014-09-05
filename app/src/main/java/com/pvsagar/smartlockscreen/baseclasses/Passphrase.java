package com.pvsagar.smartlockscreen.baseclasses;

/**
 * Created by aravind on 10/8/14.
 */
public abstract class Passphrase {
    private String passwordString, encryptedPasswordString;

    public void setPasswordString(String passwordString){
        this.passwordString = passwordString;
        //TODO add encryption
        encryptedPasswordString = passwordString;
    }
}
