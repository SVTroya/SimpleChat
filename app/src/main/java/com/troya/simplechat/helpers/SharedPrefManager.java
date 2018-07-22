package com.troya.simplechat.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.troya.simplechat.model.User;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefManager {

    private static final String PREF_FILE = "Preferences";
    private static final String KEY_USER_NAME = "UserName";
    private static final String KEY_USER_ID = "UserID";

    public static void saveUserInfo(Context context, User user) {
        final SharedPreferences preferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.apply();
    }

    public static User getUserInfo(Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        if (preferences != null) {
            User user = new User(preferences.getString(KEY_USER_ID, null), preferences.getString(KEY_USER_NAME, null));
            return (TextUtils.isEmpty(user.getUserId()) || TextUtils.isEmpty(user.getUserName())) ? null : user;
        }

        return null;
    }
}
