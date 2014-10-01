package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.passphrases.NoSecurity;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Password;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pin;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;

import java.util.List;


public class SetMasterPassword extends Activity {
    private static final String LOG_TAG = SetMasterPassword.class.getSimpleName();

    private static final int REQUEST_CREATE_PATTERN = 32;

    private static ArrayAdapter<String> passphraseAdapter;
    private static int selectedPassphrasetype;

    private static List<Integer> pattern;
    private Spinner passphraseTypeSpinner;
    private EditText passphraseEditText;
    private EditText passphraseConfirmationEditText;

    private Button doneButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_master_password);

        passphraseTypeSpinner = (Spinner) findViewById(R.id.spinner_passphrase_type);
        passphraseEditText = (EditText) findViewById(R.id.edit_text_passphrase);
        passphraseConfirmationEditText = (EditText) findViewById(R.id.edit_text_passphrase_confirmation);

        doneButton = (Button) findViewById(R.id.button_confirm);
        cancelButton = (Button) findViewById(R.id.button_cancel);

        setUpPassphraseElements();
        setUpButtons();
    }

    public void setUpPassphraseElements(){
        //Adapter for spinner
        passphraseAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Passphrase.passphraseTypes);
        passphraseTypeSpinner.setAdapter(passphraseAdapter);
        passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
        selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
        passphraseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                passphraseEditText.setHint("Set "+Passphrase.passphraseTypes[position]);
                passphraseConfirmationEditText.setHint("Confirm "+Passphrase.passphraseTypes[position]);
                selectedPassphrasetype = position;
                if(position == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                    setPassphraseItemsEnabled(true);
                    setPassphraseItemsVisible(true);
                    passphraseEditText.setText("");
                    passphraseConfirmationEditText.setText("");
                    passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pattern = null;
                }
                else if(position == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                    setPassphraseItemsEnabled(true);
                    setPassphraseItemsVisible(true);
                    passphraseEditText.setText("");
                    passphraseConfirmationEditText.setText("");
                    passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                    passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                    passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pattern = null;
                } else if(position == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN){
                    setPassphraseItemsEnabled(false);
                    setPassphraseItemsVisible(false);

                    Intent patternIntent = new Intent(getBaseContext(), StorePattern.class);
                    startActivityForResult(patternIntent, REQUEST_CREATE_PATTERN);
                } else if(position == Passphrase.INDEX_PASSPHRASE_TYPE_NONE){
                    setPassphraseItemsEnabled(false);
                    setPassphraseItemsVisible(false);
                    pattern = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                setPassphraseItemsEnabled(true);
                setPassphraseItemsVisible(true);
                passphraseEditText.setText("");
                passphraseConfirmationEditText.setText("");
                passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    public void setPassphraseItemsEnabled(boolean flag){
        passphraseEditText.setEnabled(flag);
        passphraseConfirmationEditText.setEnabled(flag);
    }

    public void setPassphraseItemsVisible(boolean flag){
        if(flag){
            passphraseEditText.setVisibility(View.VISIBLE);
            passphraseConfirmationEditText.setVisibility(View.VISIBLE);
        }
        else {
            passphraseEditText.setVisibility(View.INVISIBLE);
            passphraseConfirmationEditText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CREATE_PATTERN){
            if (resultCode == RESULT_OK) {
                pattern = data.getIntegerArrayListExtra(StorePattern.EXTRA_PATTERN);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setUpButtons(){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_NONE &&
                        selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                        passphraseEditText.getText().toString().equals("")) ||
                        selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                                pattern == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SetMasterPassword.this);
                    builder.setTitle(R.string.alert_no_passphrase_title).setMessage(R.string.alert_no_passphrase_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                } else{
                    if (selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_NONE &&
                            selectedPassphrasetype != Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN &&
                            !passphraseEditText.getText().toString().equals(passphraseConfirmationEditText.getText().toString())){
                        AlertDialog.Builder builder = new AlertDialog.Builder(SetMasterPassword.this);
                        builder.setTitle(R.string.alert_no_passphrase_match_title).setMessage(R.string.alert_no_passphrase_match_message);
                        builder.setPositiveButton(R.string.ok,null);
                        builder.create().show();
                        return;
                    }
                }
                Passphrase masterPassphrase;
                if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                    masterPassphrase = new Password(passphraseEditText.getText().toString());
                } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                    masterPassphrase = new Pin(passphraseEditText.getText().toString());
                } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_NONE){
                    masterPassphrase = new NoSecurity();
                } else if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN) {
                    masterPassphrase = new Pattern(pattern);
                } else {
                    throw new InternalError("Internal error in " + LOG_TAG);
                }
                Passphrase.setMasterPassword(masterPassphrase, getBaseContext());
                Toast.makeText(getBaseContext(), getString(R.string.master_password_set), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SetMasterPassword.this);
                builder.setTitle(R.string.alert_cancel_add_environment_title).setMessage(R.string.alert_cancel_master_password_message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel,null);
                builder.create().show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelButton.callOnClick();
    }
}
