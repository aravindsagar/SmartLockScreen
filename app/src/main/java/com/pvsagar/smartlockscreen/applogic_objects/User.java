package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.pvsagar.smartlockscreen.baseclasses.Passphrase;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.UsersEntry;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDbHelper;

/**
 * Created by aravind on 8/9/14.
 */
public class User {
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

    public static User getDefaultUser(Context context){
        Cursor userCursor = context.getContentResolver().query(UsersEntry.CONTENT_URI, null,
                UsersEntry.COLUMN_USER_NAME + " = ? ", new String[]{UsersEntry.DEFAULT_USER_NAME},
                null);
        if(userCursor.moveToFirst()){
            return getUserFromCursor(userCursor);
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

    }

    public void setPassphraseForEnvironment(Context context,
                                            Passphrase passphrase, Environment environment){
        if(id>0) {
            context.getContentResolver().insert(UsersEntry.buildUserUriWithIdEnvironmentAndPassword
                    (id, environment.getId()), passphrase.getContentValues());
        } else {
            insertIntoDatabase(context);
            setPassphraseForEnvironment(context, passphrase, environment);
        }
    }
}
