package com.pvsagar.smartlockscreen.applogic_objects.passphrases;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 17/9/14.
 */
public class Pattern extends Passphrase<List<Integer>> {

    public Pattern(){
        super(TYPE_PATTERN);
    }

    public Pattern(List<Integer> pattern){
        super(TYPE_PATTERN, pattern);
    }

    @Override
    protected String getPassphraseStringFromPassphraseRepresentation(List<Integer> passphrase) {
        String passphraseString = "";
        for (Integer aPassphrase : passphrase) {
            passphraseString += aPassphrase + ":";
        }
        return passphraseString;
    }

    @Override
    protected List<Integer> getPassphraseRepresentationFromPassphraseString(String passphrase) {
        List<Integer> pattern  = new ArrayList<Integer>();
        String[] patternNumbers = passphrase.split(":");
        for (int i = 0; i < patternNumbers.length; i++) {
            pattern.add(Integer.parseInt(patternNumbers[i]));
        }
        return pattern;
    }

    @Override
    protected boolean isPassphraseRepresentationValid(List<Integer> passphrase) {
        return passphrase!=null && passphrase.size() > 0;
    }

}
