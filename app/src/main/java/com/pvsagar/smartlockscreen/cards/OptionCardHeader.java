package com.pvsagar.smartlockscreen.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;

import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by aravind on 3/10/14.
 * Card header with a title and radio button
 * For the 3rd party card library in use.
 */
public class OptionCardHeader extends CardHeader {
    RadioButton radioButton;
    TextView titleView;
    Spinner spinner1, spinner2;

    InnerViewElementsSetUpListener<OptionCardHeader> listener;

    public OptionCardHeader(Context context, InnerViewElementsSetUpListener<OptionCardHeader> listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        radioButton = (RadioButton) parent.findViewById(R.id.radio_button_picture_card);
        titleView = (TextView) parent.findViewById(R.id.card_header_title);
        spinner1 = (Spinner) parent.findViewById(R.id.card_header_spinner1);
        spinner2 = (Spinner) parent.findViewById(R.id.card_header_spinner2);
        if(listener != null){
            listener.onInnerViewElementsSetUp(this);
        }
    }

    public RadioButton getRadioButton(){
        return radioButton;
    }
    public Spinner getSpinner1(){
        return spinner1;
    }
    public Spinner getSpinner2(){
        return spinner2;
    }

    @Override
    public String getTitle() {
        if(titleView != null) {
            return titleView.getText().toString();
        }
        return null;
    }

    @Override
    public void setTitle(String title) {
        if(titleView == null || title == null){
            return;
        }
        titleView.setText(title);
    }
}
