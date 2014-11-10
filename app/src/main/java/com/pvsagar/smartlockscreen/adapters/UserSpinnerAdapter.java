package com.pvsagar.smartlockscreen.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;

/**
 * Created by aravind on 9/11/14.
 */
public class UserSpinnerAdapter extends ResourceCursorAdapter {
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final User user = User.getUserFromCursor(cursor);

        UserViewHolder viewHolder = (UserViewHolder) view.getTag();
        if(viewHolder == null){
            viewHolder = new UserViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.user_name_text_view);
            viewHolder.type = (TextView) view.findViewById(R.id.user_type_text_view);
            viewHolder.userImage = (ImageView) view.findViewById(R.id.user_image_view);
            viewHolder.deleteImage = (ImageView) view.findViewById(R.id.user_selected_tick_image_view);

            view.setTag(viewHolder);
        }

        viewHolder.name.setText(user.getUserName());

        if(user.getId() == SharedPreferencesHelper.getDeviceOwnerUserId(mContext)){
            viewHolder.type.setText(mContext.getString(R.string.user_type_device_owner));
            viewHolder.deleteImage.setVisibility(View.GONE);
        } else {
            viewHolder.type.setText(mContext.getString(R.string.user_type_restricted_profile));
            viewHolder.deleteImage.setVisibility(View.VISIBLE);
            viewHolder.deleteImage.setOnTouchListener(new Picture.PictureTouchListener());
            viewHolder.deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.deleteFromDatabase(mContext);
                    if(mListener != null){
                        mListener.onUsersModified();
                    }
                }
            });
        }

        viewHolder.userImage.setImageDrawable(user.getUserPictureDrawable(mContext));

    }

    Context mContext;
    OnUsersModifiedListener mListener;

    public UserSpinnerAdapter(Context context, int layout, Cursor c, int flags, OnUsersModifiedListener listener) {
        super(context, layout, c, flags);
        mContext = context;
        mListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);

        UserViewHolder viewHolder = new UserViewHolder();
        viewHolder.name = (TextView) view.findViewById(R.id.user_name_text_view);
        viewHolder.type = (TextView) view.findViewById(R.id.user_type_text_view);
        viewHolder.userImage = (ImageView) view.findViewById(R.id.user_image_view);
        viewHolder.deleteImage = (ImageView) view.findViewById(R.id.user_selected_tick_image_view);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount()+1;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View res;
        if (position == getCount()-1) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //better not try to reuse our view as it can be requested for the spinner or by the list with different kind of layouts.
            View addUserView = inflater.inflate(R.layout.nav_drawer_new_profile, parent, false);
            addUserView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.title_add_new_profile);
                    View newProfileDialogView = inflater.inflate(R.layout.dialog_add_profile, null, false);
                    final EditText profileNameEditText = (EditText) newProfileDialogView.
                            findViewById(R.id.edit_text_new_profile_name);
                    builder.setView(newProfileDialogView);
                    builder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(profileNameEditText.getText().toString().isEmpty()){
                                        return;
                                    }
                                    User newUser = new User(profileNameEditText.getText().toString());
                                    newUser.insertIntoDatabase(mContext);
                                    if(mListener != null){
                                        mListener.onUsersModified();
                                    }
                                }
                            });
                    builder.create().show();
                }
            });
            res= addUserView;
        } else if (convertView != null && R.id.linear_layout_new_profile_button == convertView.getId() ){
            // If the adapter is trying to recycle our footer view, we force the generation of a new view (check on the id of our custom view)
            res= super.getDropDownView(position, null, parent);
        } else {
            res= super.getDropDownView(position, convertView, parent);
        }
        return res;
    }

    private static class UserViewHolder{
        TextView name, type;
        ImageView userImage, deleteImage;
    }

    public static interface OnUsersModifiedListener {
        public void onUsersModified();
    }
}
