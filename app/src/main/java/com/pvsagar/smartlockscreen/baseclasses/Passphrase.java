package com.pvsagar.smartlockscreen.baseclasses;

import android.content.ContentValues;

import com.pvsagar.smartlockscreen.backend_helpers.EncryptorDecryptor;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.PasswordEntry;

/**
 * Created by aravind on 10/8/14.
 * Abstract class to represent a generic passphrase. Any class can be used for PassphraseRepresentation,
 * but the subclasses should provide a one-one mapping from any object of PassphraseRepresentation
 * to a set of unique Strings.
 */
public abstract class Passphrase<PassphraseRepresentation> {
    private String passwordString, encryptedPasswordString;
    private PassphraseRepresentation passphraseRepresentation;
    private String passphraseType;

    private static final String KEY = "000102030405060708090A0B0C0D0E0F";

    private static final String PACKAGE_PREFIX = "com.pvsagar.smartlockscreen.applogic_objects" +
            ".passphrases";
    public static final String TYPE_PASSWORD = PACKAGE_PREFIX + ".TYPE_PASSWORD";
    public static final String TYPE_PIN = PACKAGE_PREFIX + ".TYPE_PIN";
    public static final String TYPE_NONE = PACKAGE_PREFIX + ".TYPE_NONE";

    public Passphrase(String type){
        if(!checkTypeValidity(type)){
            throw new IllegalArgumentException("Cannot initialize passphrase with type " +
                    type);
        }
    }

    public Passphrase(String type, PassphraseRepresentation passphrase){
        this(type);
        setPasswordRepresentation(passphrase);
    }

    protected abstract String getPassphraseStringFromPassphraseRepresentation(PassphraseRepresentation passphrase);

    protected abstract PassphraseRepresentation getPassphraseRepresentationFromPassphraseString(String passphrase);

    public void setPasswordRepresentation(PassphraseRepresentation passphrase){
        passphraseRepresentation = passphrase;
        this.passwordString = getPassphraseStringFromPassphraseRepresentation(passphrase);
        encryptPassword();
    }

    public PassphraseRepresentation getPassphraseRepresentation(){
        return passphraseRepresentation;
    }

    private boolean checkTypeValidity(String variableType){
        if(variableType.equals(TYPE_NONE) || variableType.equals(TYPE_PASSWORD)
                || variableType.equals(TYPE_PIN)){
            return true;
        }
        return false;
    }

    private void encryptPassword(){
        encryptedPasswordString = EncryptorDecryptor.encrypt(passwordString, KEY);
    }

    private void decryptPassword(){
        passwordString = EncryptorDecryptor.decrypt(encryptedPasswordString, KEY);
    }

    private ContentValues getContentValues(){
        ContentValues passwordValues = new ContentValues();
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_TYPE, passphraseType);
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_STRING, encryptedPasswordString);
        return passwordValues;
    }
}
