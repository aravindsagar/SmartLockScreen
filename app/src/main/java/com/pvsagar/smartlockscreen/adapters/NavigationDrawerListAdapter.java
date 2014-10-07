package com.pvsagar.smartlockscreen.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;

import java.util.List;

/**
 * Created by aravind on 6/10/14.
 */
public class NavigationDrawerListAdapter extends BaseAdapter{
    private static final String LOG_TAG = NavigationDrawerListAdapter.class.getSimpleName();

    public static final int ITEM_TYPE_PROFILE = 0;
    public static final int ITEM_TYPE_NEW_PROFILE = 1;
    public static final int ITEM_TYPE_MAIN = 2;
    public static final int ITEM_TYPE_SECONDARY = 3;
    public static final int ITEM_TYPE_SEPARATOR = 4;

    private List<User> mUsers;
    private List<String> mMainItems;
    private List<Integer> mMainItemImageRIds;
    private List<String> mSecondaryItems;
    private List<Integer> mSecondaryItemImageRIds;
    private Context mContext;

    private int selectedProfileIndex = -1, selectedMainItemIndex = -1;

    private Typeface selectedItemTypeface;

    public NavigationDrawerListAdapter(Context context, List<User> users, List<String> mainItems,
            List<Integer> mainItemImageRIds, List<String> secondaryItems, List<Integer> secondaryItemImageRIds) {
        mContext = context;
        mUsers = users;
        mMainItems = mainItems;
        mMainItemImageRIds = mainItemImageRIds;
        mSecondaryItems = secondaryItems;
        mSecondaryItemImageRIds = secondaryItemImageRIds;

        selectedItemTypeface = Typeface.DEFAULT_BOLD;
    }

    private Context getContext(){
        return mContext;
    }

    public void setSelectedProfileIndex(int selectedProfileIndex) {
        this.selectedProfileIndex = selectedProfileIndex;
    }

    public void setSelectedMainItemIndex(int selectedMainItemIndex) {
        this.selectedMainItemIndex = selectedMainItemIndex;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int arrayIndex;
        //TODO use convertView to recycle views. See http://stackoverflow.com/questions/3514548/creating-viewholders-for-listviews-with-different-item-layouts
        switch (getItemViewType(position)){
            case ITEM_TYPE_PROFILE:
                convertView = inflater.inflate(R.layout.nav_drawer_list_item_profile, null);
                arrayIndex = position;
                User user = mUsers.get(arrayIndex);

                TextView name = (TextView) convertView.findViewById(R.id.user_name_text_view);
                name.setText(user.getUserName());

                TextView type = (TextView) convertView.findViewById(R.id.user_type_text_view);
                if(user.getId() == SharedPreferencesHelper.getDeviceOwnerUserId(getContext())){
                    type.setText(getContext().getString(R.string.user_type_device_owner));
                } else {
                    type.setText(getContext().getString(R.string.user_type_restricted_profile));
                }

                ImageView userImage = (ImageView) convertView.findViewById(R.id.user_image_view);
                Bitmap userPictureBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.ic_contact_picture);
                userImage.setImageBitmap(Utility.getCroppedBitmap(userPictureBitmap));

                ImageView tickImage = (ImageView) convertView.findViewById(R.id.user_selected_tick_image_view);
                if(arrayIndex == selectedProfileIndex){
                    tickImage.setVisibility(View.VISIBLE);
                } else {
                    tickImage.setVisibility(View.INVISIBLE);
                }
                break;

            case ITEM_TYPE_SEPARATOR:
                /*convertView = new View(getContext());
                convertView.setBackgroundColor(Color.LTGRAY);
                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        convertDipToPx(2)));*/
                convertView = inflater.inflate(R.layout.shadow_separator, null);
                break;

            case ITEM_TYPE_NEW_PROFILE:
                convertView = inflater.inflate(R.layout.nav_drawer_new_profile, null);
                break;

            case ITEM_TYPE_MAIN:
                convertView = inflater.inflate(R.layout.nav_drawer_list_item_main, null);
                arrayIndex = position - mUsers.size() - 2;
                if(mMainItemImageRIds != null && arrayIndex < mMainItemImageRIds.size() && mMainItemImageRIds.get(arrayIndex) != null){
                    ImageView iconView = (ImageView) convertView.findViewById(R.id.list_item_icon);
                    iconView.setImageResource(mMainItemImageRIds.get(arrayIndex));
                }
                TextView itemName = (TextView) convertView.findViewById(R.id.list_item_text_view);
                itemName.setText(mMainItems.get(arrayIndex));
                if(arrayIndex == selectedMainItemIndex){
                    itemName.setTypeface(selectedItemTypeface);
                }
                break;

            case ITEM_TYPE_SECONDARY:
                convertView = inflater.inflate(R.layout.nav_drawer_list_item_secondary, null);
                arrayIndex = position - mUsers.size() - mMainItems.size() - 3;
                if(mSecondaryItemImageRIds != null && arrayIndex < mSecondaryItemImageRIds.size() && mSecondaryItemImageRIds.get(arrayIndex) != null){
                    ImageView iconView = (ImageView) convertView.findViewById(R.id.list_item_icon);
                    iconView.setImageResource(mSecondaryItemImageRIds.get(arrayIndex));
                }
                itemName = (TextView) convertView.findViewById(R.id.list_item_text_view);
                itemName.setText(mSecondaryItems.get(arrayIndex));
                break;
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < mUsers.size()){
            return ITEM_TYPE_PROFILE;
        } else if(position == mUsers.size()) {
            return ITEM_TYPE_NEW_PROFILE;
        } else if(position == mUsers.size() + 1){
            return ITEM_TYPE_SEPARATOR;
        } else if(position < mUsers.size() + mMainItems.size() + 2){
            return ITEM_TYPE_MAIN;
        } else if(position == mUsers.size() + mMainItems.size() + 2){
            return ITEM_TYPE_SEPARATOR;
        } else if(position < mUsers.size() + mMainItems.size() + mSecondaryItems.size() + 3){
            return ITEM_TYPE_SECONDARY;
        }
        return -1;
    }

    private int convertDipToPx(int pixel){
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((pixel * scale) + 0.5f);
    }

    @Override
    public int getCount() {
        return mMainItems.size() + mSecondaryItems.size() + mUsers.size() + 3;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        int arrayIndex;
        switch (getItemViewType(position)){
            case ITEM_TYPE_PROFILE:
                arrayIndex = position;
                return mUsers.get(arrayIndex);
            case ITEM_TYPE_MAIN:
                arrayIndex = position - mUsers.size() - 2;
                return mMainItems.get(arrayIndex);
            case ITEM_TYPE_SECONDARY:
                arrayIndex = position - mUsers.size() - mMainItems.size() - 3;
                return mSecondaryItems.get(arrayIndex);
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemArrayIndex(int position) {
        switch (getItemViewType(position)){
            case ITEM_TYPE_PROFILE:
                return position;
            case ITEM_TYPE_MAIN:
                return position - mUsers.size() - 2;
            case ITEM_TYPE_SECONDARY:
                return position - mUsers.size() - mMainItems.size() - 3;
            default:
                return -1;
        }
    }
}
