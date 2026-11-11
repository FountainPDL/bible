package com.fountainpdl.bible;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.PrefsManager;

/**
 * Applies the saved light/dark choice before any Activity exists.
 *
 * The app paints its own colors from AppTheme, but Material widgets, dialogs,
 * switches and the system text defaults still follow the DayNight theme. If
 * that follows the phone's dark-mode setting instead of the in-app theme, the
 * two disagree (dark text on a dark card, white dialog on a light page, ...).
 * Forcing the DayNight mode to match the in-app choice keeps them in sync.
 * Doing it here, in Application.onCreate, avoids a recreate on every launch.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings s = new PrefsManager(this).getSettings();
        AppCompatDelegate.setDefaultNightMode(
            AppTheme.isDarkMode(s) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
