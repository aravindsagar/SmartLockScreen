package com.pvsagar.smartlockscreen.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;

import java.util.List;

/**
 * Created by aravind on 6/10/14.
 * List adapter to populate the Navigation Drawer used in SmartLockScreen Settings activity.
 * The list is heterogeneous; main items which replace the fragment in the settings activity are shown,
 * and secondary items which launch separate activities are also shown.
 */
public class NavigationDrawerListAdapter extends BaseAdapter{
    private static final String LOG_TAG = NavigationDrawerListAdapter.class.getSimpleName();

    /**
     * Constant integer values denoting item types used the list
     */
    public static final int ITEM_TYPE_MAIN = 0;
    public static final int ITEM_TYPE_SECONDARY = 1;
    public static final int ITEM_TYPE_SEPARATOR = 2;

    /**
     * Data required to populate the list
     */
    private List<String> mMainItems;
    private List<Integer> mMainItemImageRIds;
    private List<String> mSecondaryItems;
    private List<Integer> mSecondaryItemImageRIds;

    private Context mContext;

    /**
     * Stores the currently selected items in various sections of the list
     */
    private int selectedMainItemIndex = -1;

    private Typeface itemTypeface;
    private int selectedItemBackgroundColor, selectedItemTextColor;

    /**
     * Constructor which takes in the required data to create list items
     * @param context Activity context
     * @param mainItems The names of main items. They are intended to replace the fragment of the settings activity
     * @param mainItemImageRIds Resource ids of icons for main list items. Should be in the same order as that of the names list.
     * @param secondaryItems Secondary item names.
     * @param secondaryItemImageRIds Resource ids of icons for secondary list items. Should be in the same order as that of the names list.
     */
    public NavigationDrawerListAdapter(Context context, List<String> mainItems, List<Integer> mainItemImageRIds,
                                       List<String> secondaryItems, List<Integer> secondaryItemImageRIds) {
        mContext = context;
        mMainItems = mainItems;
        mMainItemImageRIds = mainItemImageRIds;
        mSecondaryItems = secondaryItems;
        mSecondaryItemImageRIds = secondaryItemImageRIds;

        itemTypeface = Typeface.createFromAsset(context.getAssets(),
                "fonts/roboto-medium.ttf");
        selectedItemBackgroundColor = context.getResources().getColor(R.color.text_view_touched_darker);
        selectedItemTextColor = context.getResources().getColor(R.color.nav_bar_main_selected_item_color);
    }

    private Context getContext(){
        return mContext;
    }

    /**
     * The main item which is selected. This will cause that particular item's text to be shown in bold
     * @param selectedMainItemIndex
     */
    public void setSelectedMainItemIndex(int selectedMainItemIndex) {
        this.selectedMainItemIndex = selectedMainItemIndex;
    }

    public void setMainItems(List<String> mainItems, List<Integer>mainItemImageRIds){
        mMainItems = mainItems;
        mMainItemImageRIds = mainItemImageRIds;
        notifyDataSetInvalidated();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int arrayIndex;
        ListItemViewHolder viewHolder;

        switch (getItemViewType(position)){
            case ITEM_TYPE_SEPARATOR:
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.shadow_separator, parent, false);
                }
                break;

            case ITEM_TYPE_MAIN:
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.nav_drawer_list_item_main, null, false);
                    viewHolder = new ListItemViewHolder();
                    viewHolder.iconView = (ImageView) convertView.findViewById(R.id.list_item_icon);
                    viewHolder.itemName = (TextView) convertView.findViewById(R.id.list_item_text_view);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ListItemViewHolder) convertView.getTag();
                }
                arrayIndex = position;
                if(mMainItemImageRIds != null && arrayIndex < mMainItemImageRIds.size() && mMainItemImageRIds.get(arrayIndex) != null){
                    viewHolder.iconView.setImageResource(mMainItemImageRIds.get(arrayIndex));
                }
                viewHolder.itemName.setTypeface(itemTypeface);
                viewHolder.itemName.setText(mMainItems.get(arrayIndex));
                if(arrayIndex == selectedMainItemIndex){
                    convertView.setBackgroundColor(selectedItemBackgroundColor);
                    viewHolder.itemName.setTextColor(selectedItemTextColor);
                    //TODO find a better mode if available
                    viewHolder.iconView.setColorFilter(selectedItemTextColor, PorterDuff.Mode.SRC_ATOP);
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                    viewHolder.itemName.setTextColor(Color.BLACK);
                    viewHolder.iconView.clearColorFilter();
                }
                break;

            case ITEM_TYPE_SECONDARY:
                if(convertView == null) {
                    convertView = inflater.inflate(R.layout.nav_drawer_list_item_secondary, parent, false);
                    viewHolder = new ListItemViewHolder();
                    viewHolder.iconView = (ImageView) convertView.findViewById(R.id.list_item_icon);
                    viewHolder.itemName = (TextView) convertView.findViewById(R.id.list_item_text_view);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ListItemViewHolder) convertView.getTag();
                }

                arrayIndex = position - mMainItems.size() - 1;
                if(mSecondaryItemImageRIds != null && arrayIndex < mSecondaryItemImageRIds.size() && mSecondaryItemImageRIds.get(arrayIndex) != null){
                    viewHolder.iconView.setImageResource(mSecondaryItemImageRIds.get(arrayIndex));
                }

                viewHolder.itemName.setTypeface(itemTypeface);
                viewHolder.itemName.setText(mSecondaryItems.get(arrayIndex));
                break;
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < mMainItems.size()){
            return ITEM_TYPE_MAIN;
        } else if(position == mMainItems.size()){
            return ITEM_TYPE_SEPARATOR;
        } else if(position < mMainItems.size() + mSecondaryItems.size() + 1){
            return ITEM_TYPE_SECONDARY;
        } else if(position == mMainItems.size() + mSecondaryItems.size() + 1){
            return ITEM_TYPE_SEPARATOR;
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mMainItems.size() + mSecondaryItems.size() + 2;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        int arrayIndex;
        switch (getItemViewType(position)){
            case ITEM_TYPE_MAIN:
                arrayIndex = position;
                return mMainItems.get(arrayIndex);
            case ITEM_TYPE_SECONDARY:
                arrayIndex = position - mMainItems.size() - 1;
                return mSecondaryItems.get(arrayIndex);
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Takes the position of an item, and converts it into the array index in the corresponding array(or position in list)
     * Use getItemViewType to determine which array or list to use
     * @param position position of the item in the list whose array index is to be calculated
     * @return the array index in the corresponding array.
     */
    public int getItemArrayIndex(int position) {
        switch (getItemViewType(position)){
            case ITEM_TYPE_MAIN:
                return position;
            case ITEM_TYPE_SECONDARY:
                return position - mMainItems.size() - 1;
            default:
                return -1;
        }
    }

    private static class ListItemViewHolder {
        ImageView iconView;
        TextView itemName;
    }
}
