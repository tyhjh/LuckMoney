//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yorhp.luckmoney.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
    private static String name = "skinpeeler_config";
    private static Application sApplication;

    public SharedPreferencesUtil() {
    }

    public static void init(Application application) {
        sApplication = application;
    }

    public static SharedPreferences getSharedPreference() {
        return sApplication.getSharedPreferences(name, 0);
    }

    public static void save(String key, String value) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.putString(key, value);
        sharedData.commit();
    }

    public static void save(String key, boolean value) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.putBoolean(key, value);
        sharedData.commit();
    }

    public static void save(String key, float value) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.putFloat(key, value);
        sharedData.commit();
    }

    public static void save(String key, int value) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.putInt(key, value);
        sharedData.commit();
    }

    public static void save(String key, long value) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.putLong(key, value);
        sharedData.commit();
    }

    public static boolean getBoolean(String key, Boolean defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getBoolean(key, defValue);
    }

    public static float getFloat(String key, Float defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getFloat(key, defValue);
    }

    public static int getInt(String key, Integer defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getInt(key, defValue);
    }

    public static long getLong(String key, Long defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getLong(key, defValue);
    }

    public static String getString(String key, String defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getString(key, defValue);
    }

    public static void removeKey(String key) {
        Editor sharedData = getSharedPreference().edit();
        sharedData.remove(key);
        sharedData.commit();
    }

    public static void clearData() {
        Editor sharedData = getSharedPreference().edit();
        sharedData.clear().commit();
    }
}
