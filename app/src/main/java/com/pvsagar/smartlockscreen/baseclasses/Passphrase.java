package com.pvsagar.smartlockscreen.baseclasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.pvsagar.smartlockscreen.applogic_objects.passphrases.PassphraseFactory;
import com.pvsagar.smartlockscreen.backend_helpers.EncryptorDecryptor;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.PasswordEntry;
import com.pvsagar.smartlockscreen.receivers.AdminActions;

/**
 * Created by aravind on 10/8/14.
 * Abstract class to represent a generic passphrase. Any class can be used for PassphraseRepresentation,
 * but the subclasses should provide a one-one mapping from any object of PassphraseRepresentation
 * to a set of unique Strings.
 */
public abstract class Passphrase<PassphraseRepresentation> {
    private static final String LOG_TAG = Passphrase.class.getSimpleName();

    private String passwordString, encryptedPasswordString;
    private PassphraseRepresentation passphraseRepresentation;
    private String passphraseType;

    //User friendly password type strings
    public static final String[] passphraseTypes = {"Password","Pin","Pattern","None"};
    public static final String[] masterPassphraseTypes = {"Password","Pin"};
    public static final int INDEX_PASSPHRASE_TYPE_PASSWORD = 0;
    public static final int INDEX_PASSPHRASE_TYPE_PIN = 1;
    public static final int INDEX_PASSPHRASE_TYPE_PATTERN = 2;
    public static final int INDEX_PASSPHRASE_TYPE_NONE = 3;

    private static final String KEY = "000102030405060708090A0B0C0D0E0F";

    private static final String PACKAGE_PREFIX = "com.pvsagar.smartlockscreen.applogic_objects" +
            ".passphrases";
    public static final String TYPE_PASSWORD = PACKAGE_PREFIX + ".TYPE_PASSWORD";
    public static final String TYPE_PIN = PACKAGE_PREFIX + ".TYPE_PIN";
    public static final String TYPE_NONE = PACKAGE_PREFIX + ".TYPE_NONE";
    public static final String TYPE_PATTERN = PACKAGE_PREFIX + ".TYPE_PATTERN";

    public Passphrase(String type){
        Utility.checkForNullAndThrowException(type);
        if(!checkTypeValidity(type)){
            throw new IllegalArgumentException("Cannot initialize passphrase with type " +
                    type);
        }
        passphraseType = type;
    }

    public Passphrase(String type, PassphraseRepresentation passphrase){
        this(type);
        setPasswordRepresentation(passphrase);
    }

    /**
     * Each passphrase type should provide a unique string corresponding to each of the passphrase representation
     * instance used. This method does that.
     * @param passphrase to be converted to a string
     * @return string representation of the passphrase
     */
    protected abstract String getPassphraseStringFromPassphraseRepresentation(PassphraseRepresentation passphrase);

    /**
     * Converts a given string back to the original passphrase representation object instance
     * @param passphrase string representation of the passphrase
     * @return original representation of the passphrase
     */
    protected abstract PassphraseRepresentation getPassphraseRepresentationFromPassphraseString(String passphrase);

    /**
     * Takes a given passphrase representation instance and checks for its validity
     * @param passphrase whose validity should be checked
     * @return true if valid, false otherwise
     */
    protected abstract boolean isPassphraseRepresentationValid(PassphraseRepresentation passphrase);

    public void setPasswordRepresentation(PassphraseRepresentation passphrase) {
        if (isPassphraseRepresentationValid(passphrase)) {
            passphraseRepresentation = passphrase;
            this.passwordString = getPassphraseStringFromPassphraseRepresentation(passphrase);
            encryptPassword();
        } else {
            throw new IllegalArgumentException("Invalid passphrase passed.");
        }
    }

    public PassphraseRepresentation getPassphraseRepresentation(){
        return passphraseRepresentation;
    }

    private boolean checkTypeValidity(String variableType){
        return variableType.equals(TYPE_NONE) || variableType.equals(TYPE_PASSWORD)
                || variableType.equals(TYPE_PIN) || variableType.equals(TYPE_PATTERN);
    }

    public String getPassphraseType(){
        return passphraseType;
    }

    private void encryptPassword(){
        encryptedPasswordString = EncryptorDecryptor.encrypt(passwordString, KEY);
    }

    private void decryptPassword(){
        passwordString = EncryptorDecryptor.decrypt(encryptedPasswordString, KEY);
    }

    public ContentValues getContentValues(){
        ContentValues passwordValues = new ContentValues();
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_TYPE, passphraseType);
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_STRING, encryptedPasswordString);
        return passwordValues;
    }

    /**
     * Reads passphrase information from a cursor
     * @param cursor should be populated with values from passwords table
     * @return passphrase instances corresponding to the values from the cursor
     */
    public static Passphrase getPassphraseFromCursor(Cursor cursor){
        try{
            String type = cursor.getString(cursor.getColumnIndex(PasswordEntry.COLUMN_PASSWORD_TYPE));
            Passphrase returnPassphrase = PassphraseFactory.getPassphraseInstance(type);
            returnPassphrase.encryptedPasswordString = cursor.getString(cursor.getColumnIndex
                    (PasswordEntry.COLUMN_PASSWORD_STRING));
            if(returnPassphrase.passphraseType.equals(TYPE_NONE)){
                returnPassphrase.encryptedPasswordString = "";
            }
            returnPassphrase.decryptPassword();
            returnPassphrase.setPasswordRepresentation(returnPassphrase.
                    getPassphraseRepresentationFromPassphraseString(returnPassphrase.passwordString));
            return returnPassphrase;
        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException("Cursor should have values from passwords table");
        }
    }

    /**
     * Sets this passphrase as current device password
     * @return
     */
    public boolean setAsCurrentPassword(Context context){
        Log.d(LOG_TAG, "Setting current password");
        return AdminActions.changePassword(passwordString, this.passphraseType, getMasterPassword(context));
    }

    /**
     * Sets the master passphrase
     * @param masterPassword master passphrase to be set
     * @param context Activity/service context
     */
    public static void setMasterPassword(Passphrase masterPassword, Context context){
        SharedPreferencesHelper.setMasterPassword(context, masterPassword.encryptedPasswordString,
                masterPassword.passphraseType);
    }

    /**
     * Returns the current master passphrase
     * @param context Activity/service context
     * @return current master passphrase
     */
    public static Passphrase getMasterPassword(Context context){
        String masterPasswordType = SharedPreferencesHelper.getMasterPasswordType(context),
                masterPasswordString = SharedPreferencesHelper.getMasterPasswordString(context);
        if(masterPasswordType == null || masterPasswordString == null){
            masterPasswordType = TYPE_NONE;
            masterPasswordString = "";
        }
        Passphrase masterPassphrase = PassphraseFactory.getPassphraseInstance(masterPasswordType);
        masterPassphrase.encryptedPasswordString = masterPasswordString;
        if(masterPassphrase.passphraseType.equals(TYPE_NONE)){
            masterPassphrase.encryptedPasswordString = "";
        }
        masterPassphrase.decryptPassword();
        masterPassphrase.setPasswordRepresentation(masterPassphrase.
                getPassphraseRepresentationFromPassphraseString(masterPassphrase.passwordString));
        return masterPassphrase;
    }

    public boolean compareString(String passphrase){
        return passwordString != null && passphrase != null && passwordString.equals(passphrase);
    }
}
