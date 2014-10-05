package com.pvsagar.smartlockscreen;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.services.BaseService;

import java.util.List;

/**
 * Created by PV on 9/2/2014.
 */
public class EnvironmentListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> environmentNames;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Boolean> enabledValues;
    private List<String> environmentHints;

    public EnvironmentListAdapter(Context context, List<String> environmentNames,
                                  List<Boolean> enabledValues, List<String> environmentHints){
        super(context, R.layout.list_view_environments,environmentNames);
        this.context = context;
        this.environmentNames = environmentNames;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.enabledValues = enabledValues;
        this.environmentHints = environmentHints;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_environments, parent, false);
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.linear_layout_list_items);
        Switch mSwitch = (Switch) rootView.findViewById(R.id.switch_environment_list);
        TextView textView = (TextView)rootView.findViewById(R.id.text_view_environment_list);
        textView.setText(environmentNames.get(position));
        TextView hintTextView = (TextView)rootView.findViewById(R.id.text_view_environment_hint);
        hintTextView.setText(environmentHints.get(position));
        if(mSelectedItemsIds.get(position)){
            linearLayout.setBackgroundColor(context.getResources().getColor(R.color.wallet_holo_blue_light));
        }
        mSwitch.setChecked(enabledValues.get(position));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Environment.setEnabledInDatabase(context,environmentNames.get(position),
                        isChecked);
                context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_ENVIRONMENT));
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
            mSelectedItemsIds.put(position,value);
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
