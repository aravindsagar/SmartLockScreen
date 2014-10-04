package com.pvsagar.smartlockscreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.passphrases.NoSecurity;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Password;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pattern;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.Pin;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.cards.PassphraseCardHeader;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

public class SetMasterPassword extends ActionBarActivity {
    private static final String LOG_TAG = SetMasterPassword.class.getSimpleName();

    private static final int REQUEST_CREATE_PATTERN = 32;

    private static ArrayAdapter<String> passphraseAdapter;
    private static int selectedPassphrasetype;

    private CardView passphraseCardView;
    private static List<Integer> pattern;
    private Spinner passphraseTypeSpinner;
    private EditText passphraseEditText;
    private EditText passphraseConfirmationEditText;
    private TextView passphraseEnterPatternTextView;

    int listPreferredItemHeight;
    int textViewTouchedColor, textViewNormalColor;
    LinearLayout.LayoutParams marginTopLayoutParams;

    private Button doneButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_master_password);

        passphraseCardView = (CardView) findViewById(R.id.card_passphrase);

        listPreferredItemHeight = (int) getListPreferredItemHeight();
        textViewNormalColor = Color.argb(0, 0, 0, 0);
        textViewTouchedColor = getResources().getColor(R.color.text_view_touched);
        marginTopLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginTopLayoutParams.topMargin = convertDipToPx(8);

        doneButton = (Button) findViewById(R.id.button_confirm);
        cancelButton = (Button) findViewById(R.id.button_cancel);

        ActionBar actionBar = getSupportActionBar();
        if(!Utility.checkForNullAndWarn(actionBar, LOG_TAG)) {
            actionBar.setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.action_bar_setup)));
        }
        setUpPassphraseElements();
        setUpButtons();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.action_bar_setup));
        }
    }

    public void setUpPassphraseElements(){

        passphraseEditText = new EditText(this);
        passphraseEditText.setLayoutParams(marginTopLayoutParams);
        passphraseConfirmationEditText = new EditText(this);
        passphraseConfirmationEditText.setLayoutParams(marginTopLayoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        passphraseEnterPatternTextView = new TextView(this);
        passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern));
        passphraseEnterPatternTextView.setMinHeight(listPreferredItemHeight);
        passphraseEnterPatternTextView.setGravity(Gravity.CENTER_VERTICAL);
        passphraseEnterPatternTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
        passphraseEnterPatternTextView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        passphraseEnterPatternTextView.setLayoutParams(params);
        passphraseEnterPatternTextView.setOnTouchListener(new TextViewTouchListener());
        passphraseEnterPatternTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent patternIntent = new Intent(SetMasterPassword.this, StorePattern.class);
                startActivityForResult(patternIntent, REQUEST_CREATE_PATTERN);
            }
        });

        final Card passphraseCard = new Card(this);
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
        passphraseCard.setViewToClickToExpand(viewToClickToExpand);

        PassphraseCardHeader passphraseCardHeader = new PassphraseCardHeader(SetMasterPassword.this,
                new PassphraseCardHeader.InnerViewElementsSetUpListener() {
                    @Override
                    public void onInnerViewElementsSetUp(PassphraseCardHeader header) {
                        header.setTitle(getString(R.string.text_view_passphrase));
                        passphraseTypeSpinner = header.getSpinner();
                        //Adapter for spinner
                        passphraseAdapter = new ArrayAdapter<String>(SetMasterPassword.this,
                                android.R.layout.simple_spinner_dropdown_item, Passphrase.passphraseTypes);

                        passphraseTypeSpinner.setAdapter(passphraseAdapter);
                        passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                        selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                        passphraseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[position]);
                                passphraseConfirmationEditText.setHint("Confirm " + Passphrase.passphraseTypes[position]);
                                selectedPassphrasetype = position;
                                if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD) {
                                    setPassphraseItemsEnabled(true);
                                    setPassphraseItemsVisible(true);
                                    setPatternTextViewVisible(false);
                                    passphraseEditText.setText("");
                                    passphraseConfirmationEditText.setText("");
                                    passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                    passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    pattern = null;
                                    passphraseCard.doExpand();
                                } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PIN) {
                                    setPassphraseItemsEnabled(true);
                                    setPassphraseItemsVisible(true);
                                    setPatternTextViewVisible(false);
                                    passphraseEditText.setText("");
                                    passphraseConfirmationEditText.setText("");
                                    passphraseEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                                    passphraseConfirmationEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER);
                                    passphraseEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passphraseConfirmationEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    pattern = null;
                                    passphraseCard.doExpand();
                                } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN) {
                                    setPassphraseItemsEnabled(false);
                                    setPassphraseItemsVisible(false);
                                    setPatternTextViewVisible(true);
                                    passphraseCard.doExpand();
                                } else if (position == Passphrase.INDEX_PASSPHRASE_TYPE_NONE) {
                                    setPassphraseItemsEnabled(false);
                                    setPassphraseItemsVisible(false);
                                    setPatternTextViewVisible(false);
                                    pattern = null;
                                    passphraseCard.doCollapse();
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
                                passphraseCard.doExpand();
                            }
                        });

                    }
                });
        passphraseCard.addCardHeader(passphraseCardHeader);
        passphraseCard.addCardExpand(new CardPassphraseExpand(this));

        passphraseCardView.setCard(passphraseCard);
    }

    private float getListPreferredItemHeight(){
        android.util.TypedValue value = new android.util.TypedValue();
        boolean b = this.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        String s = TypedValue.coerceToString(value.type, value.data);
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return value.getDimension(metrics);
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

    public void setPatternTextViewVisible(boolean flag){
        if(flag){
            passphraseEnterPatternTextView.setVisibility(View.VISIBLE);
        }
        else {
            passphraseEnterPatternTextView.setVisibility(View.GONE);
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

    private class CardPassphraseExpand extends CardExpand {
        public CardPassphraseExpand(Context context) {
            super(context);
        }

        @Override
        public View getInnerView(Context context, ViewGroup parent) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RelativeLayout relativeLayout = new RelativeLayout(SetMasterPassword.this);
            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(passphraseEditText);
            layout.addView(passphraseConfirmationEditText);
            relativeLayout.addView(layout);
            relativeLayout.addView(passphraseEnterPatternTextView);
            parent.addView(relativeLayout);
            return layout;
        }
    }

    private int convertDipToPx(int pixel){
        float scale = getResources().getDisplayMetrics().density;
        return (int) ((pixel * scale) + 0.5f);
    }

    public class TextViewTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(textViewTouchedColor);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundColor(textViewNormalColor);
            }
            return false;
        }
    }

}
