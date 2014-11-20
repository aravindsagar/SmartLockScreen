package com.pvsagar.smartlockscreen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.SmartLockScreenSettings;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;

import java.util.List;

/**
 * Created by aravind on 12/11/14.
 */
public class UserListAdapter extends BaseAdapter {
    private static final int ITEM_VIEW_TYPE_COUNT = 2;
    private static final int ITEM_VIEW_TYPE_USER = 0;
    private static final int ITEM_VIEW_TYPE_ADD_USER = 1;


    private int mDeviceOwnerIndex;
    private int mResource;
    private int mNewUserResource;
    private OnUsersModifiedListener mListener;
    private Context mContext;
    private List<User> mUsers;

    public UserListAdapter(Context context, int resource, int newUserResource, List<User> objects, int deviceOwnerIndex, OnUsersModifiedListener listener) {
        mDeviceOwnerIndex = deviceOwnerIndex;
        mResource = resource;
        mListener = listener;
        mContext = context;
        mUsers = objects;
        mNewUserResource = newUserResource;
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mUsers.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(getItemViewType(position) == ITEM_VIEW_TYPE_USER) {
            final User user = (User) getItem(position);
            UserViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(mResource, parent, false);
                viewHolder = new UserViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.user_name_text_view);
                viewHolder.type = (TextView) convertView.findViewById(R.id.user_type_text_view);
                viewHolder.userImage = (ImageView) convertView.findViewById(R.id.user_image_view);
                viewHolder.deleteImage = (ImageView) convertView.findViewById(R.id.user_selected_tick_image_view);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (UserViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(user.getUserName());

            if (position == mDeviceOwnerIndex) {
                viewHolder.type.setText(getContext().getString(R.string.user_type_device_owner));
                if(viewHolder.deleteImage != null) {
                    viewHolder.deleteImage.setVisibility(View.GONE);
                }
            } else {
                viewHolder.type.setText(getContext().getString(R.string.user_type_restricted_profile));
                if(viewHolder.deleteImage != null) {
                    viewHolder.deleteImage.setVisibility(View.VISIBLE);
                    viewHolder.deleteImage.setOnTouchListener(new Picture.PictureTouchListener());
                    viewHolder.deleteImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user.deleteFromDatabase(getContext());
                            if (mListener != null) {
                                mListener.onUsersModified();
                            }
                        }
                    });
                }
            }

            viewHolder.userImage.setImageDrawable(user.getUserPictureDrawable(getContext()));
        } else {
            if(convertView == null){
                convertView = inflater.inflate(mNewUserResource, parent, false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mNewUserResource == R.layout.nav_drawer_new_profile) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.title_add_new_profile);
                        View newProfileDialogView = inflater.inflate(R.layout.dialog_add_profile, null, false);
                        final EditText profileNameEditText = (EditText) newProfileDialogView.
                                findViewById(R.id.edit_text_new_profile_name);
                        builder.setView(newProfileDialogView);
                        builder.setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (profileNameEditText.getText().toString().isEmpty()) {
                                            return;
                                        }
                                        User newUser = new User(profileNameEditText.getText().toString());
                                        newUser.insertIntoDatabase(getContext());
                                        if (mListener != null) {
                                            mListener.onUsersModified();
                                        }
                                    }
                                });
                        builder.create().show();
                    } else {
                        Intent intent = new Intent(getContext().getApplicationContext(), SmartLockScreenSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        if(mListener != null){
                            mListener.onSettingsClicked();
                        }
                    }
                }
            });
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mUsers.size() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getCount() - 1){
            return ITEM_VIEW_TYPE_ADD_USER;
        } else {
            return ITEM_VIEW_TYPE_USER;
        }
    }

    private Context getContext() {
        return mContext;
    }

    private static class UserViewHolder{
        TextView name, type;
        ImageView userImage, deleteImage;
    }

    public static interface OnUsersModifiedListener {
        public void onUsersModified();

        public void onSettingsClicked();
    }
}
