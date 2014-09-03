package com.pvsagar.smartlockscreen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;

/**
 * Created by PV on 9/2/2014.
 */
public class EnvironmentListAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] environmentNames;

    public EnvironmentListAdapter(Context context, String[] values){
        super(context, R.layout.list_view_environments,values);
        this.context = context;
        this.environmentNames = values;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.list_view_environments, parent, false);
        Switch mSwitch = (Switch) rootView.findViewById(R.id.switch_environment_list);
        TextView textView = (TextView)rootView.findViewById(R.id.text_view_environment_list);
        textView.setText(environmentNames[position]);
        mSwitch.setChecked(Environment.getEnvironmentEnabled(context,environmentNames[position]));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(context,"Checked changed",Toast.LENGTH_SHORT).show();
                //Todo: Update the database, if failed set the check back
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: Start AddEnvironment Activity in edit mode
            }
        });
        return rootView;
    }
}
