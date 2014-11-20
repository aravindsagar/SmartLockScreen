package com.pvsagar.smartlockscreen.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.StorePattern;
import com.pvsagar.smartlockscreen.applogic_objects.passphrases.PassphraseFactory;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.cards.PassphraseCardHeader;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by aravind on 7/10/14.
 * Fragment which gives user a UI to set/change the master password
 */
public class SetPasswordFragment extends Fragment {
    private static final String LOG_TAG = SetPasswordFragment.class.getSimpleName();
    private static final int REQUEST_CREATE_PATTERN = 32;

    private static ArrayAdapter<String> passphraseAdapter;
    private static int selectedPassphrasetype;

    private static List<Integer> pattern;

    private CardView passphraseCardView;
    private Spinner passphraseTypeSpinner;
    private EditText passphraseEditText;
    private EditText passphraseConfirmationEditText;
    private TextView passphraseEnterPatternTextView;

    private static int currentPassphraseTypeIndex;

    int listPreferredItemHeight;
    int textViewTouchedColor, textViewNormalColor;
    LinearLayout.LayoutParams marginTopLayoutParams;

    private Button doneButton, resetButton;

    SetPassphraseInterface mSetPassphraseInterface;

    int mPaddingTop, mPaddingBottom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_set_master_password, container, false);

        passphraseCardView = (CardView) rootView.findViewById(R.id.card_passphrase);

        listPreferredItemHeight = (int) getListPreferredItemHeight();
        textViewNormalColor = Color.argb(0, 0, 0, 0);
        textViewTouchedColor = getResources().getColor(R.color.text_view_touched);
        marginTopLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginTopLayoutParams.topMargin = convertDipToPx(8);

        doneButton = (Button) rootView.findViewById(R.id.button_confirm);
        resetButton = (Button) rootView.findViewById(R.id.button_reset);

        setUpActionBar();
        setUpPassphraseElements();
        initPassphraseElements();
        setUpButtons();

        switch (getActivity().getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getTop() + mPaddingTop,
                        rootView.getPaddingRight(), rootView.getBottom() + mPaddingBottom);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getTop() + mPaddingTop,
                        rootView.getPaddingRight() + mPaddingBottom, rootView.getBottom());
                break;
        }
        return rootView;
    }

    private void setUpActionBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                mPaddingTop += 16;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSetPassphraseInterface = (SetPassphraseInterface) activity;
        } catch (ClassCastException e){
            throw new InstantiationException("Activity using " + LOG_TAG + " should implement "
                    + SetPassphraseInterface.class.getSimpleName(), e);
        }
    }

    private void setUpPassphraseElements(){

        passphraseEditText = new EditText(getActivity());
        passphraseEditText.setLayoutParams(marginTopLayoutParams);
        passphraseConfirmationEditText = new EditText(getActivity());
        passphraseConfirmationEditText.setLayoutParams(marginTopLayoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        passphraseEnterPatternTextView = new TextView(getActivity());
        passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern));
        passphraseEnterPatternTextView.setMinHeight(listPreferredItemHeight);
        passphraseEnterPatternTextView.setGravity(Gravity.CENTER_VERTICAL);
        passphraseEnterPatternTextView.setPadding((int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, 0, 0);
        passphraseEnterPatternTextView.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
        passphraseEnterPatternTextView.setLayoutParams(params);
        passphraseEnterPatternTextView.setOnTouchListener(new TextViewTouchListener());
        passphraseEnterPatternTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent patternIntent = new Intent(getActivity(), StorePattern.class);
                startActivityForResult(patternIntent, REQUEST_CREATE_PATTERN);
            }
        });

        final Card passphraseCard = new Card(getActivity());
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
        passphraseCard.setViewToClickToExpand(viewToClickToExpand);

        PassphraseCardHeader passphraseCardHeader = new PassphraseCardHeader(getActivity(),
                new PassphraseCardHeader.InnerViewElementsSetUpListener() {
                    @Override
                    public void onInnerViewElementsSetUp(PassphraseCardHeader header) {
                        header.setTitle(getString(R.string.text_view_passphrase));
                        passphraseTypeSpinner = header.getSpinner();
                        //Adapter for spinner
                        passphraseAdapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_spinner_dropdown_item, Passphrase.passphraseTypes);

                        passphraseTypeSpinner.setAdapter(passphraseAdapter);
                        passphraseTypeSpinner.setSelection(Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD);
                        selectedPassphrasetype = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
                        passphraseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                passphraseConfirmationEditText.setHint("Confirm " + Passphrase.passphraseTypes[position]);
                                selectedPassphrasetype = position;
                                if (position == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD) {
                                    if(currentPassphraseTypeIndex == Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD){
                                        passphraseEditText.setHint("(Unchanged)");
                                    } else {
                                        passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[position]);
                                    }
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
                                    if(currentPassphraseTypeIndex == Passphrase.INDEX_PASSPHRASE_TYPE_PIN){
                                        passphraseEditText.setHint("(Unchanged)");
                                    } else {
                                        passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[position]);
                                    }
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
                                    if(currentPassphraseTypeIndex == Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN){
                                        passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern_after_entry));
                                    } else {
                                        passphraseEnterPatternTextView.setText("Set " + Passphrase.passphraseTypes[position]);
                                    }
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
                                passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD]);
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
        passphraseCard.addCardExpand(new CardPassphraseExpand(getActivity()));
        passphraseCardView.setCard(passphraseCard);
    }

    public void setPatternTextViewVisible(boolean flag){
        if(flag){
            passphraseEnterPatternTextView.setVisibility(View.VISIBLE);
        }
        else {
            passphraseEnterPatternTextView.setVisibility(View.GONE);
        }
    }

    private void initPassphraseElements(){
        Passphrase passphrase = mSetPassphraseInterface.getCurrentPassphrase();
        if(passphrase == null){
            currentPassphraseTypeIndex = -1;
            return;
        }
        if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PASSWORD)) {
            currentPassphraseTypeIndex = Passphrase.INDEX_PASSPHRASE_TYPE_PASSWORD;
        } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PATTERN)){
            currentPassphraseTypeIndex = Passphrase.INDEX_PASSPHRASE_TYPE_PATTERN;
            passphraseEnterPatternTextView.setText(getString(R.string.text_view_enter_pattern_after_entry));
        } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_PIN)){
            currentPassphraseTypeIndex = Passphrase.INDEX_PASSPHRASE_TYPE_PIN;
        } else if(passphrase.getPassphraseType().equals(Passphrase.TYPE_NONE)){
            currentPassphraseTypeIndex = Passphrase.INDEX_PASSPHRASE_TYPE_NONE;
        }
        passphraseTypeSpinner.setSelection(currentPassphraseTypeIndex);
        selectedPassphrasetype = currentPassphraseTypeIndex;
    }
    private float getListPreferredItemHeight(){
        TypedValue value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        TypedValue.coerceToString(value.type, value.data);
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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

    public void setUpButtons(){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPassphrasetype == Passphrase.INDEX_PASSPHRASE_TYPE_NONE || (!passphraseEditText.getText().toString().equals("") &&
                        passphraseConfirmationEditText.getText().toString().equals(passphraseEditText.getText().toString())) ||
                        pattern != null) {
                    Passphrase passphrase = PassphraseFactory.getPassphraseInstance(
                            selectedPassphrasetype, passphraseEditText.getText().toString(),
                            passphraseEditText.getText().toString(), null);

                    getActivity().startService(BaseService.getServiceIntent(getActivity(),
                            null, BaseService.ACTION_DETECT_ENVIRONMENT));
                    mSetPassphraseInterface.onPassphraseEntered(passphrase);
                    return;
                } else if (!passphraseEditText.getText().toString().equals(passphraseConfirmationEditText.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_no_passphrase_match_title).setMessage(R.string.alert_no_passphrase_match_message);
                    builder.setPositiveButton(R.string.ok,null);
                    builder.create().show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_no_passphrase_title).setMessage(R.string.alert_no_passphrase_message);
                builder.setPositiveButton(R.string.ok,null);
                builder.create().show();

            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passphraseEditText.setText("");
                passphraseEditText.setHint("Set " + Passphrase.passphraseTypes[passphraseTypeSpinner.getSelectedItemPosition()]);
                passphraseConfirmationEditText.setText("");
                pattern = null;
            }
        });
    }

    private class CardPassphraseExpand extends CardExpand {
        public CardPassphraseExpand(Context context) {
            super(context);
        }

        @Override
        public View getInnerView(Context context, ViewGroup parent) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RelativeLayout relativeLayout = new RelativeLayout(context);
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

    public static interface SetPassphraseInterface {
        public void onPassphraseEntered(Passphrase passphrase);

        public Passphrase getCurrentPassphrase();
    }
}
