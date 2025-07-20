package com.deathcoutndown.app;

import android.content.Context;

public class SharedPrefsManager {
    private static final String PREFS_NAME = "WallpaperPrefs";

    public static void saveBirthDate(Context context, long birthDateMillis) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong("birthDateMillis", birthDateMillis)
                .apply();
    }

    public static long getBirthDateMillis(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong("birthDateMillis", -1);
    }

    public static void saveExpectedLifespan(Context context, int expectedLifespanYears) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt("expectedLifespan", expectedLifespanYears)
                .apply();
    }

    public static int getExpectedLifespan(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt("expectedLifespan", 80);
    }

    public static boolean hasValidConfiguration(Context context) {
        return getBirthDateMillis(context) != -1;
    }
}