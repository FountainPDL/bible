package com.fountainpdl.bible;

import android.content.res.ColorStateList;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.fountainpdl.bible.utils.ThemeHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fountainpdl.bible.dialogs.NoteDialog;
import com.fountainpdl.bible.dialogs.SermonDialog;
import com.fountainpdl.bible.dialogs.MarathonDialog;
import com.fountainpdl.bible.dialogs.NavigatePickerDialog;
import com.fountainpdl.bible.fragments.HomeFragment;
import com.fountainpdl.bible.fragments.LibraryFragment;
import com.fountainpdl.bible.fragments.MoreFragment;
import com.fountainpdl.bible.fragments.ReadFragment;
import com.fountainpdl.bible.fragments.SearchFragment;
import com.fountainpdl.bible.models.AppSettings;
import com.fountainpdl.bible.models.Sermon;
import com.fountainpdl.bible.utils.AppTheme;
import com.fountainpdl.bible.utils.BibleDataManager;
import com.fountainpdl.bible.utils.PrefsManager;
import com.fountainpdl.bible.utils.TTSManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private BibleDataManager bibleData;
    private TTSManager ttsManager;
    private AppTheme theme;

    private BottomNavigationView bottomNav;
    private FrameLayout fragmentContainer;
    private View topBar, splash;
    private TextView titleLine1, titleLine2, positionChip, translationChip, ttsPlayBtn;
    private android.widget.ImageView noteBtn;

    private HomeFragment homeFragment;
    private ReadFragment readFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;
    private MoreFragment moreFragment;
    private Fragment activeFragment;

    private String currentBook;
    private int currentChapter;
    private String lastTranslation;
    private String lastThemeMode;

    public interface TtsUiListener { void onTtsStateChanged(boolean playing, int verse); }
    private TtsUiListener ttsUiListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);
        bibleData = BibleDataManager.getInstance();

        AppSettings settings = prefsManager.getSettings();
        theme = AppTheme.build(settings);
        lastTranslation = settings.translation;
        lastThemeMode = AppTheme.effectiveMode(settings);

        currentBook = prefsManager.getCurrentBook();
        currentChapter = prefsManager.getCurrentChapter();

        bindViews();
        setupBottomNav();
        setupTopBar();
        applyChrome(settings);
        applyKeepScreenOn(settings);

        splash = findViewById(R.id.splashOverlay);
        bibleData.loadAsync(this, () -> {
            if (splash != null) splash.setVisibility(View.GONE);
        });

        ttsManager = new TTSManager(this, new TTSManager.Listener() {
            @Override public void onReady() {
                AppSettings s = prefsManager.getSettings();
                ttsManager.setRate(s.ttsRate);
                ttsManager.setPitch(s.ttsPitch);
            }
            @Override public void onVerseStart(int verseNum) { dispatchTts(true, verseNum); }
            @Override public void onDone() { dispatchTts(false, -1); }
            @Override public void onError(String message) { dispatchTts(false, -1); }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (activeFragment == moreFragment && moreFragment != null && moreFragment.handleBack()) return;
                if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void dispatchTts(boolean playing, int verse) {
        runOnUiThread(() -> {
            if (ttsPlayBtn != null) ttsPlayBtn.setText(playing ? "\u25A0" : "\u25B6");
            if (ttsUiListener != null) ttsUiListener.onTtsStateChanged(playing, verse);
        });
    }

    private void bindViews() {
        bottomNav = findViewById(R.id.bottomNav);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        topBar = findViewById(R.id.topBar);
        titleLine1 = findViewById(R.id.titleLine1);
        titleLine2 = findViewById(R.id.titleLine2);
        positionChip = findViewById(R.id.positionChip);
        translationChip = findViewById(R.id.translationChip);
        ttsPlayBtn = findViewById(R.id.ttsPlayBtn);
        noteBtn = findViewById(R.id.noteBtn);
    }

    // ── Theme / settings ─────────────────────────────────────────────────────

    /** Rebuild the theme from saved settings and repaint the whole app. */
    public void applyTheme() {
        AppSettings s = prefsManager.getSettings();
        theme = AppTheme.build(s);
        lastThemeMode = AppTheme.effectiveMode(s);
        applyChrome(s);
        if (homeFragment != null) homeFragment.onThemeChanged();
        if (readFragment != null) readFragment.onThemeChanged();
        if (searchFragment != null) searchFragment.onThemeChanged();
        if (libraryFragment != null) libraryFragment.onThemeChanged();
        if (moreFragment != null) moreFragment.onThemeChanged();
    }

    /**
     * Single entry point for "a setting changed". Re-reads the saved settings and
     * pushes them to every screen immediately, so nothing waits for a restart or
     * a tab switch to take effect.
     */
    public void onSettingsChanged() {
        AppSettings s = prefsManager.getSettings();
        theme = AppTheme.build(s);
        lastThemeMode = AppTheme.effectiveMode(s);
        applyChrome(s);
        applyKeepScreenOn(s);

        boolean translationChanged = !s.translation.equals(lastTranslation);
        lastTranslation = s.translation;

        if (homeFragment != null) { homeFragment.onThemeChanged(); homeFragment.onSettingsChanged(); }
        if (readFragment != null) {
            readFragment.onSettingsChanged();
            if (translationChanged) readFragment.onTranslationChanged();
        }
        if (searchFragment != null) {
            searchFragment.onThemeChanged();
            if (translationChanged) searchFragment.onTranslationChanged();
        }
        if (libraryFragment != null) {
            libraryFragment.onThemeChanged();
            if (translationChanged) libraryFragment.onTranslationChanged();
        }
        if (moreFragment != null) moreFragment.onThemeChanged();
    }

    /** Paints the activity-level views (top bar, bottom nav, system bars, splash). */
    private void applyChrome(AppSettings s) {
        AppTheme t = theme;

        // Keep Material widgets, dialogs and system defaults in the same light/dark
        // family as the in-app theme. Only touch it when it actually differs so we
        // never trigger a needless restart.
        int wantNight = t.isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != wantNight) AppCompatDelegate.setDefaultNightMode(wantNight);

        getWindow().getDecorView().setBackgroundColor(t.bg);
        if (splash != null) splash.setBackgroundColor(t.bg);
        topBar.setBackgroundColor(t.navBg);
        bottomNav.setBackgroundColor(t.navBg);
        fragmentContainer.setBackgroundColor(t.bg);
        titleLine1.setTextColor(t.primaryFg);
        titleLine2.setTextColor(t.accentFg);
        positionChip.setTextColor(t.text);
        positionChip.setBackground(ThemeHelper.shape(this, android.graphics.Color.TRANSPARENT, t.border, 20));

        translationChip.setBackground(ThemeHelper.shape(this, ColorUtilsAlpha(t.accent, 0x22), ColorUtilsAlpha(t.accent, 0x99), 20));
        translationChip.setTextColor(t.accentFg);
        translationChip.setText(s.translation);

        ttsPlayBtn.setTextColor(t.primaryFg);
        noteBtn.setColorFilter(t.primaryFg);

        ColorStateList navColors = ThemeHelper.stateList(t.primaryFg, t.sub);
        bottomNav.setItemIconTintList(navColors);
        bottomNav.setItemTextColor(navColors);

        getWindow().setStatusBarColor(t.navBg);
        getWindow().setNavigationBarColor(t.navBg);
        // Dark icons on a light bar, light icons on a dark one. Without this the
        // clock and battery vanish whenever the in-app theme differs from the phone's.
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(!t.isDark);
            c.setAppearanceLightNavigationBars(!t.isDark);
        }
    }

    private void applyKeepScreenOn(AppSettings s) {
        if (s.keepScreenOn) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private int ColorUtilsAlpha(int color, int alpha) {
        return android.graphics.Color.argb(alpha, android.graphics.Color.red(color),
            android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto Night Mode: the hour may have rolled over while the app was in the background.
        AppSettings s = prefsManager.getSettings();
        if (!AppTheme.effectiveMode(s).equals(lastThemeMode)) onSettingsChanged();
    }

    private void setupTopBar() {
        positionChip.setText(currentBook + " " + currentChapter);
        positionChip.setOnClickListener(v -> openNavigatePicker());

        translationChip.setOnClickListener(v -> switchTranslation());

        noteBtn.setOnClickListener(v -> openNoteDialog(null, null));

        ttsPlayBtn.setOnClickListener(v -> {
            if (ttsManager != null && ttsManager.isPlaying()) { ttsManager.stop(); return; }
            showRead();
            if (readFragment != null) readFragment.startReadingFromTop();
        });
    }

    private void setupBottomNav() {
        FragmentManager fm = getSupportFragmentManager();

        // After a restart or recreate the FragmentManager already holds the old
        // fragments. Reuse them: creating new ones on top is what produced
        // overlapping, duplicated screens (and blank ones).
        homeFragment = (HomeFragment) fm.findFragmentByTag("home");
        readFragment = (ReadFragment) fm.findFragmentByTag("read");
        searchFragment = (SearchFragment) fm.findFragmentByTag("search");
        libraryFragment = (LibraryFragment) fm.findFragmentByTag("library");
        moreFragment = (MoreFragment) fm.findFragmentByTag("more");

        if (homeFragment == null || readFragment == null || searchFragment == null
                || libraryFragment == null || moreFragment == null) {
            FragmentTransaction clear = fm.beginTransaction();
            for (Fragment f : fm.getFragments()) clear.remove(f);
            clear.commitNowAllowingStateLoss();

            homeFragment = new HomeFragment();
            readFragment = new ReadFragment();
            searchFragment = new SearchFragment();
            libraryFragment = new LibraryFragment();
            moreFragment = new MoreFragment();

            FragmentTransaction t = fm.beginTransaction();
            t.add(R.id.fragmentContainer, moreFragment, "more").hide(moreFragment);
            t.add(R.id.fragmentContainer, libraryFragment, "library").hide(libraryFragment);
            t.add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment);
            t.add(R.id.fragmentContainer, readFragment, "read").hide(readFragment);
            t.add(R.id.fragmentContainer, homeFragment, "home");
            t.commitNow();
            activeFragment = homeFragment;
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            Fragment[] all = {homeFragment, readFragment, searchFragment, libraryFragment, moreFragment};
            activeFragment = homeFragment;
            for (Fragment f : all) if (!f.isHidden()) { activeFragment = f; break; }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target;
            if (id == R.id.nav_home) target = homeFragment;
            else if (id == R.id.nav_read) target = readFragment;
            else if (id == R.id.nav_search) target = searchFragment;
            else if (id == R.id.nav_library) target = libraryFragment;
            else target = moreFragment;

            if (target != activeFragment) {
                fm.beginTransaction().setReorderingAllowed(true).hide(activeFragment).show(target).commit();
                activeFragment = target;
            }
            return true;
        });
    }

    public void showHome() { bottomNav.setSelectedItemId(R.id.nav_home); }
    public void showRead() { bottomNav.setSelectedItemId(R.id.nav_read); }
    public void showLibrary() { bottomNav.setSelectedItemId(R.id.nav_library); }
    public void showLibrary(String tab) {
        if (libraryFragment != null && tab != null) libraryFragment.openTab(tab);
        showLibrary();
    }
    public void showSearch() { bottomNav.setSelectedItemId(R.id.nav_search); }

    // ── Navigation ────────────────────────────────────────────────────────────
    public void navigateToChapter(String book, int chapter, Integer scrollToVerse) {
        // Don't keep reading the old chapter aloud over the new one.
        if (ttsManager != null && ttsManager.isPlaying()) ttsManager.stop();
        currentBook = book;
        currentChapter = chapter;
        prefsManager.setCurrentPosition(book, chapter);
        prefsManager.addHistoryEntry(book, chapter);
        positionChip.setText(book + " " + chapter);
        if (homeFragment != null) homeFragment.refreshData();
        // Load first, then reveal the tab, so the Read page never flashes stale content.
        if (readFragment != null) readFragment.loadChapter(book, chapter, scrollToVerse);
        showRead();
    }

    public void openNavigatePicker() {
        NavigatePickerDialog dialog = new NavigatePickerDialog(this, theme, prefsManager.getSettings(),
            (book, chapter, verse) -> navigateToChapter(book, chapter, verse));
        dialog.show();
    }

    public void switchTranslation() {
        AppSettings s = prefsManager.getSettings();
        s.translation = "KJV".equals(s.translation) ? "NIV" : "KJV";
        prefsManager.saveSettings(s);
        onSettingsChanged();
    }

    /** Kept for older callers: settings were saved elsewhere, just refresh everything. */
    public void switchTranslationSilently() { onSettingsChanged(); }

    // ── Dialog launchers ──────────────────────────────────────────────────────
    public void openNoteDialog(String linkedRef, com.fountainpdl.bible.models.Note editNote) {
        NoteDialog dialog = new NoteDialog(this, theme, linkedRef, editNote, () -> {
            if (libraryFragment != null) libraryFragment.refreshNotes();
        });
        dialog.show();
    }

    public void openSermonDialog(Sermon editSermon) {
        SermonDialog dialog = new SermonDialog(this, theme, bibleData, prefsManager.getSettings().translation, editSermon, () -> {
            if (libraryFragment != null) libraryFragment.refreshSermons();
        });
        dialog.show();
    }

    public void openMarathonDialog() {
        MarathonDialog dialog = new MarathonDialog(this, theme, () -> {
            if (libraryFragment != null) libraryFragment.refreshMarathons();
        });
        dialog.show();
    }

    // ── Getters for fragments ────────────────────────────────────────────────
    public PrefsManager getPrefsManager() { return prefsManager; }
    public BibleDataManager getBibleData() { return bibleData; }
    public TTSManager getTtsManager() { return ttsManager; }
    public AppTheme getAppTheme() { return theme; }
    public String getCurrentBook() { return currentBook; }
    public int getCurrentChapter() { return currentChapter; }
    public void setTtsUiListener(TtsUiListener l) { this.ttsUiListener = l; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) ttsManager.shutdown();
    }
}
