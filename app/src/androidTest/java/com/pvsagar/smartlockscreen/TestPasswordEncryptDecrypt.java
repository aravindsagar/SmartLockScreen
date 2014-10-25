package com.pvsagar.smartlockscreen;

import android.test.AndroidTestCase;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.EncryptorDecryptor;

/**
 * Created by aravind on 8/9/14.
 * Tests the password encryption and decryption
 */
public class TestPasswordEncryptDecrypt extends AndroidTestCase {
    private static final String LOG_TAG = TestPasswordEncryptDecrypt.class.getSimpleName();

    public void testEncryptDecrypt(){
        final String passwordString = "password";
        final String KEY = "000102030405060708090A0B0C0D0E0F";

        String encryptedString = EncryptorDecryptor.encrypt(passwordString, KEY);
        Log.d(LOG_TAG, "Encrypted: " + encryptedString);

        String decryptedPassword = EncryptorDecryptor.decrypt(encryptedString, KEY);
        Log.d(LOG_TAG, "Decrypted: " + decryptedPassword);
        assertTrue(passwordString.equals(decryptedPassword));
    }
}
