package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDbHelper;

/**
 * Created by aravind on 8/9/14.
 */
public class User {
    private static final String LOG_TAG = User.class.getSimpleName();

    private String userName;
    private long id;

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
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                UsersEntry.COLUMN_USER_NAME + " = ? ", new String[]{UsersEntry.DEFAULT_USER_NAME},
                null);
        if(userCursor.moveToFirst()){
            User returnUser = getUserFromCursor(userCursor);
            userCursor.close();
            return returnUser;
        } else {
            EnvironmentDbHelper.insertDefaultUser(context);
            return getDefaultUser(context);
        }
    }

    private static User getUserFromCursor(Cursor cursor){
        try{
            String userName = cursor.getString(cursor.getColumnIndex(UsersEntry.COLUMN_USER_NAME));
            User user = new User(userName);
            user.id = cursor.getLong(cursor.getColumnIndex(UsersEntry._ID));
            return user;
        } catch (Exception e){
            throw new IllegalArgumentException("Cursor should be populated with values from " +
                    "Users table");
        }
    }

    private ContentValues getContentValues(){
        ContentValues userValues = new ContentValues();
        userValues.put(UsersEntry.COLUMN_USER_NAME, getUserName());
        return userValues;
    }

    public void insertIntoDatabase(Context context){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                UsersEntry.COLUMN_USER_NAME + " = ? ", new String[]{getUserName()}, null);
        if(userCursor.getCount() == 0) {
            Uri uri = context.getContentResolver().insert(UsersEntry.CONTENT_URI, getContentValues());
            id = UsersEntry.getUserIdFromUri(uri);
        } else {
            userCursor.moveToFirst();
            User existingUser = getUserFromCursor(userCursor);
            this.id = existingUser.id;
        }
        userCursor.close();
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

    private static User currentUser;

    public static User getCurrentUser(Context context) {
        if(currentUser == null){
            return getDefaultUser(context);
        }
        return currentUser;
    }
}
