package com.pvsagar.smartlockscreen.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.ChoosePicture;
import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.cards.CardAnimatorListener;
import com.pvsagar.smartlockscreen.cards.CardTouchListener;

import java.util.List;
import java.util.Vector;

/**
 * Created by PV on 9/2/2014.
 * A list adapter which populates items of the list shown in ManageEnvironmentFragment
 */
public class EnvironmentListAdapter extends ArrayAdapter<String> {
    private static final int ANIMATOR_DURATION = 150;

    private Context context;
    private List<String> environmentNames;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Boolean> enabledValues;
    private List<String> environmentHints;
    private List<Drawable> environmentPictures;
    private Vector<Float> elevations;

    private ColorDrawable switchOn, switchOff;

    private static ImageView clickedImageView;

    public EnvironmentListAdapter(Context context, List<String> environmentNames,
            List<Boolean> enabledValues, List<String> environmentHints, List<Drawable> environmentPictures){
        super(context, R.layout.list_view_environments,environmentNames);
        this.context = context;
        this.environmentNames = environmentNames;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.enabledValues = enabledValues;
        this.environmentHints = environmentHints;
        this.environmentPictures = environmentPictures;
        this.elevations = new Vector<Float>();

        switchOn = new ColorDrawable(context.getResources().getColor(R.color.switch_color));
        switchOff = new ColorDrawable(Color.LTGRAY);

        for (int i = 0; i < environmentNames.size(); i++) {
            elevations.add(CardTouchListener.CARD_NORMAL_ELEVATION);
        }
    }

    public static ImageView getClickedImageView() {
        return clickedImageView;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_environments, parent, false);
        CardView cardView = (CardView) rootView.findViewById(R.id.manage_environment_card_view);
        final Switch mSwitch = (Switch) rootView.findViewById(R.id.switch_environment_list);
        TextView textView = (TextView)rootView.findViewById(R.id.text_view_environment_list);
        String environmentName = environmentNames.get(position);
        textView.setText(environmentName);
        TextView hintTextView = (TextView)rootView.findViewById(R.id.text_view_environment_hint);
        hintTextView.setText(environmentHints.get(position));
        cardView.setPreventCornerOverlap(false);
        cardView.setMaxCardElevation(elevations.get(position));
        cardView.setCardElevation(elevations.get(position));
        if(mSelectedItemsIds.get(position)){
            ObjectAnimator maxAnimator = ObjectAnimator.ofFloat(cardView, "maxCardElevation", CardTouchListener.CARD_SELECTED_ELEVATION);
            maxAnimator.setDuration(ANIMATOR_DURATION);
            maxAnimator.setInterpolator(new AccelerateInterpolator());
            maxAnimator.addListener(new CardAnimatorListener(position, elevations, cardView));
            maxAnimator.start();

            ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "cardElevation", CardTouchListener.CARD_SELECTED_ELEVATION);
            animator.setDuration(ANIMATOR_DURATION);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addListener(new CardAnimatorListener(position, elevations, cardView));
            animator.start();
        } else {
            ObjectAnimator maxAnimator = ObjectAnimator.ofFloat(cardView, "maxCardElevation", CardTouchListener.CARD_NORMAL_ELEVATION);
            maxAnimator.setDuration(ANIMATOR_DURATION);
            maxAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            maxAnimator.addListener(new CardAnimatorListener(position, elevations, cardView));
            maxAnimator.start();

            ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "cardElevation", CardTouchListener.CARD_NORMAL_ELEVATION);
            animator.setDuration(ANIMATOR_DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addListener(new CardAnimatorListener(position, elevations, cardView));
            animator.start();
        }

        mSwitch.setChecked(enabledValues.get(position));
        if(!mSwitch.isChecked()){
            mSwitch.setThumbDrawable(switchOff);
        }
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Environment.setEnabledInDatabase(context,environmentNames.get(position),
                        isChecked);
                if(isChecked){
                    mSwitch.setThumbDrawable(switchOn);
                } else {
                    mSwitch.setThumbDrawable(switchOff);
                }
            }
        });
        final ImageView environmentPicture = (ImageView) rootView.findViewById(R.id.image_view_environment_picture);
        environmentPicture.setImageDrawable(environmentPictures.get(position));
        environmentPicture.setOnTouchListener(new Picture.PictureTouchListener());
        environmentPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChoosePicture.class);
                clickedImageView = environmentPicture;
                getContext().startActivity(intent);
                ((Activity)getContext()).overridePendingTransition(0, 0);
            }
        });
        return rootView;
    }

    @Override
    public void remove(String object) {
        environmentNames.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int position) {
        return environmentNames.get(position);
    }

    public void toggleSelection(int position){
        selectView(position,!mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value){
        if(value){
            mSelectedItemsIds.put(position,true);
        }
        else{
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount(){
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds(){
        return mSelectedItemsIds;
    }
}
