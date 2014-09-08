package com.pvsagar.smartlockscreen.baseclasses;

import android.content.ContentValues;

import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.PasswordEntry;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

    private static final String KEY = "OJNBWer8074OUHWEF9u";

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

    protected String getPassphraseStringFromPassphraseRepresentation(PassphraseRepresentation passphrase){
        return passphrase.toString();
    }

    protected String getPassphraseRepresentationFromPassphraseString(String passphrase){
        return passphrase.toString();
    }

    public void setPasswordRepresentation(PassphraseRepresentation passphrase){
        passphraseRepresentation = passphrase;
        this.passwordString = getPassphraseStringFromPassphraseRepresentation(passphrase);
        encryptedPasswordString = encryptPassword(passwordString);
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

    private String encryptPassword(String passwordString){
        try {
            Cipher passwordCipher = Cipher.getInstance("AES");
            passwordCipher.init(Cipher.ENCRYPT_MODE, getKey());
            return toHex(passwordCipher.doFinal(passwordString.getBytes()));
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (NoSuchPaddingException e){
            e.printStackTrace();
        } catch (InvalidKeyException e){
            e.printStackTrace();
        } catch (IllegalBlockSizeException e){
            e.printStackTrace();
        } catch (BadPaddingException e){
            e.printStackTrace();
        }
        return null;
    }

    private String decryptPassword(String encryptedPasswordString){
        try{
            Cipher passwordCipher = Cipher.getInstance("AES");
            passwordCipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(passwordCipher.doFinal(toByte(encryptedPasswordString)));
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (NoSuchPaddingException e){
            e.printStackTrace();
        } catch (InvalidKeyException e){
            e.printStackTrace();
        } catch (IllegalBlockSizeException e){
            e.printStackTrace();
        } catch (BadPaddingException e){
            e.printStackTrace();
        }
        return null;
    }

    private Key getKey() throws NoSuchAlgorithmException{
        SecretKeySpec keySpec = new SecretKeySpec(getRawKey(KEY.getBytes()), "AES");
        return keySpec;
        //TODO should change KEY
    }

    private static byte[] getRawKey(byte[] seed) throws NoSuchAlgorithmException{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        generator.init(128, sr);
        SecretKey skey = generator.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private final static String HEX = "0123456789ABCDEF";
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }

    private ContentValues getContentValues(){
        ContentValues passwordValues = new ContentValues();
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_TYPE, passphraseType);
        passwordValues.put(PasswordEntry.COLUMN_PASSWORD_STRING, encryptedPasswordString);
        return passwordValues;
    }
}
