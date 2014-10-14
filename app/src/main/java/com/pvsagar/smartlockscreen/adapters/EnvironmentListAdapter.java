package com.pvsagar.smartlockscreen.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;

import java.util.List;

/**
 * Created by PV on 9/2/2014.
 * A list adapter which populates items of the list shown in ManageEnvironmentFragment
 */
public class EnvironmentListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> environmentNames;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Boolean> enabledValues;
    private List<String> environmentHints;
    private List<Drawable> environmentPictures;

    private ColorDrawable switchOn, switchOff;

    public EnvironmentListAdapter(Context context, List<String> environmentNames,
            List<Boolean> enabledValues, List<String> environmentHints, List<Drawable> environmentPictures){
        super(context, R.layout.list_view_environments,environmentNames);
        this.context = context;
        this.environmentNames = environmentNames;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.enabledValues = enabledValues;
        this.environmentHints = environmentHints;
        this.environmentPictures = environmentPictures;

        switchOn = new ColorDrawable(context.getResources().getColor(R.color.switch_color));
        switchOff = new ColorDrawable(Color.LTGRAY);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_environments, parent, false);
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.linear_layout_list_items);
        final Switch mSwitch = (Switch) rootView.findViewById(R.id.switch_environment_list);
        TextView textView = (TextView)rootView.findViewById(R.id.text_view_environment_list);
        String environmentName = environmentNames.get(position);
        textView.setText(environmentName);
        TextView hintTextView = (TextView)rootView.findViewById(R.id.text_view_environment_hint);
        hintTextView.setText(environmentHints.get(position));
        if(mSelectedItemsIds.get(position)){
            linearLayout.setBackgroundColor(context.getResources().getColor(R.color.wallet_holo_blue_light));
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
        ImageView environmentPicture = (ImageView) rootView.findViewById(R.id.image_view_environment_picture);
        environmentPicture.setImageDrawable(environmentPictures.get(position));
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
