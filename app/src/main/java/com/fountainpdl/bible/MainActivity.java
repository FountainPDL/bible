package com.fountainpdl.bible;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
    private View topBar;
    private TextView titleLine1, titleLine2, positionChip, translationChip;
    private View ttsPlayBtn, noteBtn;

    private HomeFragment homeFragment;
    private ReadFragment readFragment;
    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;
    private MoreFragment moreFragment;
    private Fragment activeFragment;

    private String currentBook;
    private int currentChapter;

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

        currentBook = prefsManager.getCurrentBook();
        currentChapter = prefsManager.getCurrentChapter();

        bindViews();
        applyTheme();
        setupBottomNav();
        setupTopBar();

        View splash = findViewById(R.id.splashOverlay);
        bibleData.loadAsync(this, () -> {
            if (splash != null) splash.setVisibility(View.GONE);
            showHome();
        });

        ttsManager = new TTSManager(this, new TTSManager.Listener() {
            @Override public void onReady() { }
            @Override public void onVerseStart(int verseNum) {
                runOnUiThread(() -> { if (ttsUiListener != null) ttsUiListener.onTtsStateChanged(true, verseNum); });
            }
            @Override public void onDone() {
                runOnUiThread(() -> { if (ttsUiListener != null) ttsUiListener.onTtsStateChanged(false, -1); });
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> { if (ttsUiListener != null) ttsUiListener.onTtsStateChanged(false, -1); });
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (readFragment != null && readFragment.isAdded() && readFragment == activeFragment) {
                    if (bottomNav.getSelectedItemId() != R.id.nav_read) {
                        bottomNav.setSelectedItemId(R.id.nav_read);
                        return;
                    }
                }
                if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
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

    public void applyTheme() {
        AppSettings s = prefsManager.getSettings();
        theme = AppTheme.build(s);

        getWindow().getDecorView().setBackgroundColor(theme.bg);
        topBar.setBackgroundColor(theme.navBg);
        bottomNav.setBackgroundColor(theme.navBg);
        titleLine1.setTextColor(theme.primary);
        titleLine2.setTextColor(theme.accent);
        positionChip.setTextColor(theme.sub);

        GradientDrawable transBg = new GradientDrawable();
        transBg.setColor(ColorWithAlpha(theme.accent, 0x18));
        transBg.setStroke(dp(1.5f), ColorWithAlpha(theme.accent, 0x88));
        transBg.setCornerRadius(dp(20));
        translationChip.setBackground(transBg);
        translationChip.setTextColor(theme.accent);
        translationChip.setText(s.translation);

        bottomNav.setItemIconTintList(android.content.res.ColorStateList.valueOf(theme.sub));
        bottomNav.setItemTextColor(android.content.res.ColorStateList.valueOf(theme.sub));

        getWindow().setStatusBarColor(theme.navBg);
        getWindow().setNavigationBarColor(theme.navBg);

        if (homeFragment != null) homeFragment.onThemeChanged();
        if (readFragment != null) readFragment.onThemeChanged();
        if (searchFragment != null) searchFragment.onThemeChanged();
        if (libraryFragment != null) libraryFragment.onThemeChanged();
        if (moreFragment != null) moreFragment.onThemeChanged();
    }

    private int ColorWithAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void setupTopBar() {
        positionChip.setText(currentBook + " " + currentChapter);
        positionChip.setOnClickListener(v -> openNavigatePicker());

        translationChip.setOnClickListener(v -> switchTranslation());

        noteBtn.setOnClickListener(v -> openNoteDialog(null, null));

        ttsPlayBtn.setOnClickListener(v -> {
            if (readFragment != null) readFragment.startReadingFromTop();
        });
    }

    private void setupBottomNav() {
        homeFragment = new HomeFragment();
        readFragment = new ReadFragment();
        searchFragment = new SearchFragment();
        libraryFragment = new LibraryFragment();
        moreFragment = new MoreFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction t = fm.beginTransaction();
        t.add(R.id.fragmentContainer, moreFragment, "more").hide(moreFragment);
        t.add(R.id.fragmentContainer, libraryFragment, "library").hide(libraryFragment);
        t.add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment);
        t.add(R.id.fragmentContainer, readFragment, "read").hide(readFragment);
        t.add(R.id.fragmentContainer, homeFragment, "home");
        t.commitNow();
        activeFragment = homeFragment;

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target;
            int id = item.getItemId();
            if (id == R.id.nav_home) target = homeFragment;
            else if (id == R.id.nav_read) target = readFragment;
            else if (id == R.id.nav_search) target = searchFragment;
            else if (id == R.id.nav_library) target = libraryFragment;
            else target = moreFragment;

            if (target != activeFragment) {
                fm.beginTransaction().hide(activeFragment).show(target).commit();
                activeFragment = target;
            }
            return true;
        });
    }

    public void showHome() { bottomNav.setSelectedItemId(R.id.nav_home); }
    public void showRead() { bottomNav.setSelectedItemId(R.id.nav_read); }
    public void showLibrary() { bottomNav.setSelectedItemId(R.id.nav_library); }

    // ── Navigation ────────────────────────────────────────────────────────────
    public void navigateToChapter(String book, int chapter, Integer scrollToVerse) {
        currentBook = book;
        currentChapter = chapter;
        prefsManager.setCurrentPosition(book, chapter);
        prefsManager.addHistoryEntry(book, chapter);
        positionChip.setText(book + " " + chapter);
        showRead();
        if (readFragment != null) readFragment.loadChapter(book, chapter, scrollToVerse);
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
        translationChip.setText(s.translation);
        if (readFragment != null) readFragment.onTranslationChanged();
        if (libraryFragment != null) libraryFragment.onTranslationChanged();
    }

    /** Called when the translation was already set to a specific value elsewhere
     *  (e.g. picked directly from a settings page rather than toggled). Just
     *  refreshes the dependent UI to match what's already saved. */
    public void switchTranslationSilently() {
        AppSettings s = prefsManager.getSettings();
        translationChip.setText(s.translation);
        if (readFragment != null) readFragment.onTranslationChanged();
        if (libraryFragment != null) libraryFragment.onTranslationChanged();
    }

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
