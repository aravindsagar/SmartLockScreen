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
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 8/9/14.
 * Class which represents a User.
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

    /**
     * Gets an instance of this class which represents the default user (device owner)
     * @param context Activity/Service context
     * @return instance of device owner
     */
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

    /**
     * Takes in a cursor and creates instances of user populated with data from the cursor
     * @param cursor should contain data from users table
     * @return list of users read from the cursor
     */
    public static User getUserFromCursor(Cursor cursor){
        try{
            String userName = cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_NAME));
            User user = new User(userName);
            user.id = cursor.getLong(cursor.getColumnIndex(UsersEntry._ID));
            user.setUserPicture(new Picture(
                    cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE_TYPE)),
                    cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE_DRAWABLE)),
                    cursor.getBlob(cursor.getColumnIndex(UsersEntry.COLUMN_USER_PICTURE)),
                    CharacterDrawable.BORDER_LIGHTER));
            return user;
        } catch (Exception e){
            throw new IllegalArgumentException("Cursor should be populated with values from " +
                    "Users table");
        }
    }

    /**
     * Gets content values for this user instance
     * @return instance of ContentValues with values from this instance of User
     */
    private ContentValues getContentValues(){
        ContentValues userValues = new ContentValues();
        userValues.put(UsersEntry.COLUMN_USER_NAME, getUserName());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_TYPE, getUserPicture().getPictureType());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_DESCRIPTION, getUserPicture().getBackgroundColor());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE_DRAWABLE, getUserPicture().getDrawableName());
        userValues.put(UsersEntry.COLUMN_USER_PICTURE, getUserPicture().getImage());
        return userValues;
    }

    /**
     * Inserts this user into database
     * @param context Activity/Service context
     * @return id of the user
     */
    public long insertIntoDatabase(Context context){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                UsersEntry.COLUMN_USER_NAME + " = ? ", new String[]{getUserName()}, null);
        if(userCursor.getCount() == 0) {
            if(userPicture == null){
                userPicture = new Picture(Picture.PICTURE_TYPE_COLOR,
                        String.valueOf(Utility.getRandomColor(context)), null, null, CharacterDrawable.BORDER_LIGHTER);
            }
            Uri uri = context.getContentResolver().insert(UsersEntry.CONTENT_URI, getContentValues());
            id = UsersEntry.getUserIdFromUri(uri);
//            Log.d(LOG_TAG, "New user id: " + id);
        } else {
            userCursor.moveToFirst();
            User existingUser = getUserFromCursor(userCursor);
            this.id = existingUser.id;
        }
        userCursor.close();
        return id;
    }

    /**
     * Sets passphrase for this user for specific environment
     * @param context Activity/Service context
     * @param passphrase Passphrase to be set
     * @param environment Environment for which the passphrase is to be set
     */
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

    /**
     * Gets passphrase for this user for specific environment
     * @param context Activity/Service context
     * @param environment Environment for which the passphrase is to be retrieved
     * @return required Passphrase
     */
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

    /**
     * Sets passphrase for current user, when environment is unknown. For restricted profiles, this passphrase will be used throughout
     * @param context Activity/Service context
     * @param passphrase passphrase to be set
     */
    public void setPassphraseForUnknownEnvironment(Context context, Passphrase passphrase){
        if(id>0) {
            context.getContentResolver().update(UsersEntry.buildUserUriWithIdEnvironmentAndPassword
                    (id, UNKNOWN_ENVIRONMENT_ID), passphrase.getContentValues(), null, null);
        } else {
            insertIntoDatabase(context);
            setPassphraseForUnknownEnvironment(context, passphrase);
        }
    }

    /**
     * Removes the passphrase for current user, for unknown environment.
     * @param context Activity/Service context
     */
    public void removePassphraseForUnknownEnvironment(Context context){
        if(id>0) {
            context.getContentResolver().delete(UsersEntry.buildUserUriWithIdEnvironmentAndPassword(
                    id, UNKNOWN_ENVIRONMENT_ID), null, null);
        }
    }

    /**
     * Gets passphrase for current user, when environment is unknown. For restricted profiles, this passphrase is to be used throughout
     * @param context Activity/Service context
     * @return required passphrase
     */
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

    /**
     * Stores the active user
     */
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

    /**
     * Get all users from the database
     * @param context Activity/Service context
     * @return list of all users
     */
    public static List<User> getAllUsers(Context context){
        Cursor userCursor = getAllUserCursor(context);
        ArrayList<User> allUsers = new ArrayList<User>();
        if(userCursor.moveToFirst()) {
            for(;!userCursor.isAfterLast(); userCursor.moveToNext()){
                allUsers.add(getUserFromCursor(userCursor));
            }
        }
        userCursor.close();
        return allUsers;
    }

    public static Cursor getAllUserCursor(Context context){
        return context.getContentResolver().query(UsersEntry.CONTENT_URI, null, null, null, null);
    }

    public static User getUserWithId(Context context, long id){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.buildUserUriWithId(id),
                null, null, null, null);
        if(userCursor.moveToFirst()) {
            return getUserFromCursor(userCursor);
        } else {
            return null;
        }
    }

    public Picture getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(Picture userPicture) {
        this.userPicture = userPicture;
    }

    /**
     * Gets a drawable corresponding to user's picture
     * @param context Activity/Service context
     * @return a drawable of user picture
     */
    public Drawable getUserPictureDrawable(Context context){
        return userPicture.getDrawable(Character.toUpperCase(getUserName().charAt(0)), context);
    }

    /**
     * Inserts the default user (Device owner) into the database. This reads the 'me' contact card available
     * in the system, and uses a generic string if that is null
     * @param context Activity/Service context
     */
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
                    String.valueOf(Utility.getRandomColor(context)), null, null));
        } else {
            userImage = Picture.getCroppedBitmap(userImage, Color.rgb(200, 200, 200));
            defaultUser.setUserPicture(new Picture(Picture.PICTURE_TYPE_CUSTOM,
                    null, null, Picture.drawableToByteArray(new BitmapDrawable(context.getResources(), userImage))));
        }
        long id = defaultUser.insertIntoDatabase(context);

        if(id >= 0){
            SharedPreferencesHelper.setDeviceOwnerUserId(context, id);
        } else {
            throw new SQLiteException("Cannot insert default user into database");
        }
    }

    public void deleteFromDatabase(Context context){
        if(getDefaultUser(context).getId() == getId()){
            return;
        }
        context.getContentResolver().delete(UsersEntry.buildUserUriWithId(getId()), null, null);
    }

    public List<App> getAllowedApps(Context context){
        Cursor appsCursor = context.getContentResolver().query(UsersEntry.buildUserUriWithAppWhitelist(getId()), null, null, null, null);
        List<App> allowedApps = new ArrayList<App>();
        if(appsCursor.moveToFirst()){
            for(;!appsCursor.isAfterLast();appsCursor.moveToNext()){
                allowedApps.add(App.getAppFromCursor(appsCursor, context));
            }
        }
        return allowedApps;
    }

    public boolean isAppAllowed(Context context, String packageName){
        Cursor appCursor = context.getContentResolver().query(UsersEntry.
                buildUserUriWithAppWhitelistWithPackageName(getId(), packageName), null, null, null, null);
        return appCursor.moveToFirst();
    }

    public void addToAllowedApps(Context context, String packageName){
        ContentValues values = new ContentValues();
        values.put(EnvironmentDatabaseContract.AppWhitelistEntry.COLUMN_PACKAGE_NAME, packageName);
        context.getContentResolver().insert(UsersEntry.buildUserUriWithAppWhitelist(getId()), values);
    }

    public void removeFromAllowedApps(Context context, String packageName){
        context.getContentResolver().delete(UsersEntry.buildUserUriWithAppWhitelistWithPackageName(getId(), packageName), null, null);
    }
    //TODO function to change device owner name
}
