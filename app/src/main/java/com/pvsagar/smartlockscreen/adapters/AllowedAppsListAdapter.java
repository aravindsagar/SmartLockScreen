package com.pvsagar.smartlockscreen.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.App;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.frontend_helpers.CustomSwitchHelper;

import java.util.List;

/**
 * Created by aravind on 11/11/14.
 */
public class AllowedAppsListAdapter extends ArrayAdapter<App> {

    private int mResource;
    private User currentUser;
    private List<App> mAllowedApps;

    public AllowedAppsListAdapter(Context context, int resource, List<App> objects, User currentUser) {
        super(context, resource, objects);
        this.currentUser = currentUser;
        mResource = resource;
        mAllowedApps = currentUser.getAllowedApps(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final App app = getItem(position);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AllowedAppViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(mResource, parent, false);
            viewHolder = new AllowedAppViewHolder();
            viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.image_view_app_icon);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.text_view_app_name);
            viewHolder.packageName = (TextView) convertView.findViewById(R.id.text_view_package_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AllowedAppViewHolder) convertView.getTag();
        }
        viewHolder.aSwitch = (Switch) convertView.findViewById(R.id.switch_allowed_app_list);

        viewHolder.appIcon.setImageDrawable(app.getAppIcon());
        viewHolder.appName.setText(app.getAppName());
        viewHolder.packageName.setText(app.getPackageName());

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (viewHolder.aSwitch.isChecked()) {
                viewHolder.aSwitch.setThumbDrawable(CustomSwitchHelper.getSwitchOnDrawable(getContext()));
            } else {
                viewHolder.aSwitch.setThumbDrawable(CustomSwitchHelper.getSwitchOffDrawable());
            }
        }
        viewHolder.aSwitch.setOnCheckedChangeListener(new CustomSwitchHelper.CustomSwitchCheckedChangeListener(getContext()) {
            @Override
            public void onCustomCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(mAllowedApps.contains(app)){
                        return;
                    }
                    mAllowedApps.add(app);
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            currentUser.addToAllowedApps(getContext(), app.getPackageName());
                            return null;
                        }
                    }.execute();
                } else {
                    mAllowedApps.remove(app);
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            currentUser.removeFromAllowedApps(getContext(), app.getPackageName());
                            return null;
                        }
                    }.execute();
                }
            }
        });

        viewHolder.aSwitch.setChecked(mAllowedApps.contains(app));
        return convertView;
    }

    private static class AllowedAppViewHolder {
        ImageView appIcon;
        TextView appName, packageName;
        Switch aSwitch;
    }
}
