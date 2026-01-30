package com.example.telemedicine;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static void saveThemeMode(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }
    
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void initializeTheme(Context context) {
        // Apply theme based on saved preference or system default
        int savedThemeMode = getSavedThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);
    }

    public static void applyTheme(Context context) {
        int savedThemeMode = getSavedThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(savedThemeMode);
    }

    public static boolean isDarkMode(Context context) {
        int savedThemeMode = getSavedThemeMode(context);
        return savedThemeMode == AppCompatDelegate.MODE_NIGHT_YES ||
               (savedThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                (context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES);
    }

    // Method to check if this is a theme-change-induced recreation
    public static boolean isThemeChangeRecreation(Context context) {
        // We can use a temporary flag or check the context
        // For now, we'll rely on the saved instance state approach
        return false; // Placeholder - we'll handle this differently
    }
}