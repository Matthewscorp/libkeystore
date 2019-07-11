package com.matthewscorp.android.keystore.demoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.matthewscorp.android.keystore.LibKeyStore;

import kotlin.Pair;

/**
 * Created by jmatthews on 4/16/2019.
 */

public class Credentials {

    private static final String USER = "com.matthewscorp.android.username";
    private static final String PWD = "com.matthewscorp.android.password";
    private static final String ENCRYPIV = "com.matthewscorp.android.encryptionIv";
    private static final String USER_TYPE = "com.matthewscorp.android.type";
    public static final String ALIAS_KEY = "appCredAlias";
    public static final String ACCESS_TYPE_FACEBOOK = "facebook";
    public static final String ACCESS_TYPE_EMAIL = "email";

    public static void saveCredentials(Context context, String usernameString, String pwd, String type) {
        Pair pair = LibKeyStore.INSTANCE.encryptString(ALIAS_KEY, pwd);
        SharedPreferences.Editor editor = getGlobalSharedPreferences(context).edit();
        editor.putString(USER, usernameString);
        editor.putString(USER_TYPE, Base64.encodeToString(type.getBytes(), Base64.DEFAULT));
        String encrypto = null;
        editor.putString(PWD, (String) pair.getFirst());
        try {
            encrypto = Base64.encodeToString((byte[]) pair.getSecond(), Base64.DEFAULT);
            editor.putString(ENCRYPIV, encrypto);
        } catch (NullPointerException npe) {
            editor.remove(ENCRYPIV);
        }
        editor.apply();
    }

    public static void removeCredentials(Context context) {
        SharedPreferences.Editor editor = getGlobalSharedPreferences(context).edit();
        editor.remove(USER);
        editor.remove(USER_TYPE);
        editor.remove(PWD);
        editor.remove(ENCRYPIV);
        editor.apply();
    }

    public static Pair<String, String> retrieveCredentials(Context context) {
        // load login data from shared preferences (
        // only the password is encrypted, IV used for the encryption is loaded from shared preferences
        SharedPreferences sharedPreferences = getGlobalSharedPreferences(context);
        String username = sharedPreferences.getString(USER, null);
        if (username == null) {
            System.out.println("Credentials - You must first store credentials.");
            return null;
        }

        String base64EncryptedPassword = sharedPreferences.getString(PWD, null);
        String base64EncryptionIv = sharedPreferences.getString(ENCRYPIV, null);
        byte[] encryptionIv = null;
        if (base64EncryptionIv != null) {
            encryptionIv = Base64.decode(base64EncryptionIv, Base64.DEFAULT);
        }
//        byte[] encryptedPassword = Base64.decode(base64EncryptedPassword, Base64.DEFAULT);

        String pwd = LibKeyStore.INSTANCE.decryptString(ALIAS_KEY, base64EncryptedPassword, encryptionIv);

        return new Pair(username, pwd);
    }

    public static SharedPreferences getGlobalSharedPreferences(Context context) {
        return context.getSharedPreferences("com.matthewscorp.android.GLOBAL.PREF.KEY", Context.MODE_PRIVATE);
    }

}
