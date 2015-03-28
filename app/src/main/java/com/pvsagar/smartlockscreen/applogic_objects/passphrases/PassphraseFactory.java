package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

import java.util.List;

/**
 * Created by aravind on 5/10/14.
 * Factory Class for getting passphrases of different types
 */
public class PassphraseFactory {
    public static Passphrase getPassphraseInstance(final int PASSPHRASE_INDEX){
        switch (PASSPHRASE_INDEX){
            case Passphrase.INDEX_PASSPHRASE_TYPE_NONE:
                return new NoSecurity();
            case Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD:
                return new Password();
            case Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN:
                return new Pattern();
            case Passphrase.INDEX_PASSPHRASE_TYPE_PIN:
                return new Pin();
        }
        return null;
    }

    public static Passphrase getPassphraseInstance(final int PASSPHRASE_INDEX, String password,
                                                   String pin, List<Integer> pattern){
        switch (PASSPHRASE_INDEX){
            case Passphrase.INDEX_PASSPHRASE_TYPE_NONE:
                return new NoSecurity();
            case Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD:
                return new Password(password);
            case Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN:
                return new Pattern(pattern);
            case Passphrase.INDEX_PASSPHRASE_TYPE_PIN:
                return new Pin(pin);
        }
        return null;
    }

    public static Passphrase getPassphraseInstance(final String type){
        Passphrase returnPassphrase;
        switch (type) {
            case Passphrase.TYPE_PASSWORD:
                returnPassphrase = new Password();
                break;
            case Passphrase.TYPE_PIN:
                returnPassphrase = new Pin();
                break;
            case Passphrase.TYPE_NONE:
                returnPassphrase = new NoSecurity();
                break;
            case Passphrase.TYPE_PATTERN:
                returnPassphrase = new Pattern();
                break;
            default:
                throw new TypeNotPresentException("The type read from database is not a valid type."
                        , new Exception());
        }
        return returnPassphrase;
    }
}
