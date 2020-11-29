package com.e.android_launcher.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_APPEND;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class SharedPreferencesUtil {
    public static final String APP_LOCK_PASSWORD = "app_lock_password";

    public SharedPreferencesUtil() {
    }

    @SuppressLint("WrongConstant")
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("share_data", MODE_APPEND);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getSharedPreferences(context).getString(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getSharedPreferences(context).getInt(key, defaultValue);
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        return getSharedPreferences(context).getFloat(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getSharedPreferences(context).getLong(key, defaultValue);
    }

    public static void put(Context context, String key, Object value) {
        if (value instanceof String) {
            putString(context, key, (String) value);
        } else if (value instanceof Integer) {
            putInt(context, key, (Integer) value);
        } else if (value instanceof Boolean) {
            putBoolean(context, key, (Boolean) value);
        } else if (value instanceof Float) {
            putFloat(context, key, (Float) value);
        } else if (value instanceof Long) {
            putLong(context, key, (Long) value);
        }

    }

    public static Object get(Context context, String key, Object defaultValue) {
        if (defaultValue instanceof String) {
            return getString(context, key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return getInt(context, key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return getBoolean(context, key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return getFloat(context, key, (Float) defaultValue);
        } else {
            return defaultValue instanceof Long ? getLong(context, key, (Long) defaultValue) : null;
        }
    }

    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }

    public static boolean contains(Context context, String key) {
        return getSharedPreferences(context).contains(key);
    }
}
