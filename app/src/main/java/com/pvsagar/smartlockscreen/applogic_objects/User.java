package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 8/9/14.
 */
public class User {
    private static final String LOG_TAG = User.class.getSimpleName();

    public static final long UNKNOWN_ENVIRONMENT_ID = 0;

    private String userName;
    private long id;
    private Picture userPicture;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User(String userName){
        this.userName = userName;
    }

    public long getId(){
        return id;
    }

    public static User getDefaultUser(Context context){
        long defaultUserId = SharedPreferencesHelper.getDeviceOwnerUserId(context);
        if(defaultUserId == -1){
            insertDefaultUser(context);
            return getDefaultUser(context);
        } else {
            Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                    UsersEntry._ID + " = ? ", new String[]{String.valueOf(defaultUserId)},
                    null);
            if (userCursor.moveToFirst()) {
                User returnUser = getUserFromCursor(userCursor);
                userCursor.close();
                return returnUser;
            } else {
                insertDefaultUser(context);
                return getDefaultUser(context);
            }
        }
    }

    private static User getUserFromCursor(Cursor cursor){
        try{
            String userName = cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_NAME));
            User user = new User(userName);
            user.id = cursor.getLong(cursor.getColumnIndex(UsersEntry._ID));
            user.setUserPicture(new Picture(
                    cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE_TYPE)),
                    cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION)),
                    cursor.getBlob(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE)),
                    CharacterDrawable.BORDER_LIGHTER));
            return user;
        } catch (Exception e){
            throw new IllegalArgumentException("Cursor should be populated with values from " +
                    "Users table");
        }
    }

    private ContentValues getContentValues(){
        ContentValues userValues = new ContentValues();
        userValues.put(UsersEntry.COLUMN_USER_NAME, getUserName());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_TYPE, getUserPicture().getPictureType());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION, getUserPicture().getPictureDescription());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE, getUserPicture().getImage());
        return userValues;
    }

    public long insertIntoDatabase(Context context){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                UsersEntry.COLUMN_USER_NAME + " = ? ", new String[]{getUserName()}, null);
        if(userCursor.getCount() == 0) {
            if(userPicture == null){
                userPicture = new Picture(Picture.PICTURE_TYPE_COLOR,
                        String.valueOf(Utility.getRandomColor(context)), null, CharacterDrawable.BORDER_LIGHTER);
            }
            Uri uri = context.getContentResolver().insert(UsersEntry.CONTENT_URI, getContentValues());
            id = UsersEntry.getUserIdFromUri(uri);
        } else {
            userCursor.moveToFirst();
            User existingUser = getUserFromCursor(userCursor);
            this.id = existingUser.id;
        }
        userCursor.close();
        return id;
    }

    public void setPassphraseForEnvironment(Context context,
                                            Passphrase passphrase, Environment environment){
        if(id>0) {
            if(environment.getId() <= 0){
                Log.w(LOG_TAG, "Environment id was invalid; getting from database. Name: " +
                        environment.getName());
                environment = Environment.getBareboneEnvironment(context, environment.getName());
            }
            if(environment.getId() <= 0){
                throw new IllegalArgumentException("Environment " + environment.getName() +
                        " not present in database. Please insert the environment first.");
            }
            context.getContentResolver().update(UsersEntry.buildUserUriWithIdEnvironmentAndPassword
                    (id, environment.getId()), passphrase.getContentValues(), null, null);
        } else {
            insertIntoDatabase(context);
            setPassphraseForEnvironment(context, passphrase, environment);
        }
    }

    public Passphrase getPassphraseForEnvironment(Context context, Environment environment){
        if(id>0) {
            if(environment.getId() <= 0){
                environment = Environment.getBareboneEnvironment(context, environment.getName());
            }
            Cursor passwordCursor = context.getContentResolver().query(UsersEntry.
                    buildUserUriWithIdEnvironmentAndPassword(id, environment.getId()),
                    null, null, null, null);
            if(passwordCursor != null && passwordCursor.moveToFirst()){
                Passphrase returnPassphrase = Passphrase.getPassphraseFromCursor(passwordCursor);
                passwordCursor.close();
                return returnPassphrase;
            }
            Log.w(LOG_TAG, "Empty cursor returned for password query.");
            return null;
        }
        Log.w(LOG_TAG, "User has id 0.");
        return null;
    }

    public void setPassphraseForUnknownEnvironment(Context context, Passphrase passphrase){
        if(id>0) {
            context.getContentResolver().update(UsersEntry.buildUserUriWithIdEnvironmentAndPassword
                    (id, UNKNOWN_ENVIRONMENT_ID), passphrase.getContentValues(), null, null);
        } else {
            insertIntoDatabase(context);
            setPassphraseForUnknownEnvironment(context, passphrase);
        }
    }

    public void removePassphraseForUnknownEnvironment(Context context){
        if(id>0) {
            context.getContentResolver().delete(UsersEntry.buildUserUriWithIdEnvironmentAndPassword(
                    id, UNKNOWN_ENVIRONMENT_ID), null, null);
        }
    }

    public Passphrase getPassphraseForUnknownEnvironment(Context context){
        if(id>0) {
            Cursor passwordCursor = context.getContentResolver().query(UsersEntry.
                            buildUserUriWithIdEnvironmentAndPassword(id, UNKNOWN_ENVIRONMENT_ID),
                    null, null, null, null);
            if(passwordCursor != null && passwordCursor.moveToFirst()){
                Passphrase returnPassphrase = Passphrase.getPassphraseFromCursor(passwordCursor);
                passwordCursor.close();
                return returnPassphrase;
            }
            return null;
        }
        Log.w(LOG_TAG, "User has id 0.");
        return null;
    }

    private static User currentUser;

    public static User getCurrentUser(Context context) {
        if(currentUser == null){
            currentUser = getDefaultUser(context);
        }
        return currentUser;
    }

    public static void setCurrentUser(User user){
        currentUser = user;
    }

    public static List<User> getAllUsers(Context context){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null, null, null, null);
        ArrayList<User> allUsers = new ArrayList<User>();
        if(userCursor.moveToFirst()) {
            for(;!userCursor.isAfterLast(); userCursor.moveToNext()){
                allUsers.add(getUserFromCursor(userCursor));
            }
        }
        userCursor.close();
        return allUsers;
    }

    public Picture getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(Picture userPicture) {
        this.userPicture = userPicture;
    }

    public Drawable getUserPictureDrawable(Context context){
        return userPicture.getDrawable(Character.toUpperCase(getUserName().charAt(0)), context);
    }

    public static void insertDefaultUser(Context context){
        String userName = null;
        Bitmap userImage = null;
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Cursor c = resolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        if(c.moveToFirst()) {
            userName = (c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream
                    (resolver, ContactsContract.Profile.CONTENT_URI, true);
            if(photoDataStream != null) {
                userImage = BitmapFactory.decodeStream(photoDataStream);
            } else {
                String photoUriString = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                if(photoUriString != null && !photoUriString.isEmpty()) {
                    Uri photoUri = Uri.parse(photoUriString);
                    Cursor pictureCursor = resolver.query(photoUri, new String[]
                            {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
                    try{
                        byte[] photoBytes = pictureCursor.getBlob(0);
                        userImage = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    String photoThumbUriString = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    if(photoThumbUriString != null && !photoThumbUriString.isEmpty()) {
                        Uri photoThumbUri = Uri.parse(photoThumbUriString);
                        Cursor pictureCursor = resolver.query(photoThumbUri, new String[]
                                {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
                        try{
                            byte[] photoBytes = pictureCursor.getBlob(0);
                            userImage = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            c.close();
        }
        if(userName == null || userName.isEmpty()){
            userName = UsersEntry.DEFAULT_USER_NAME;
        }
        User defaultUser = new User(userName);
        if(userImage == null){
            defaultUser.setUserPicture(new Picture(Picture.PICTURE_TYPE_COLOR,
                    String.valueOf(Utility.getRandomColor(context)), null));
        } else {
            userImage = Utility.getCroppedBitmap(userImage, Color.rgb(200, 200, 200));
            defaultUser.setUserPicture(new Picture(Picture.PICTURE_TYPE_CUSTOM,
                    null, Picture.drawableToByteArray(new BitmapDrawable(context.getResources(), userImage))));
        }
        long id = defaultUser.insertIntoDatabase(context);

        if(id >= 0){
            SharedPreferencesHelper.setDeviceOwnerUserId(context, id);
        } else {
            throw new SQLiteException("Cannot insert default user into database");
        }
    }
}
