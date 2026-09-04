package com.fountainpdl.bible.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.R;
import com.fountainpdl.bible.utils.AppTheme;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private static final String[] VOTD_REFS = {
        "John 3:16", "Jeremiah 29:11", "Philippians 4:13", "Romans 8:28",
        "Psalms 23:1", "Proverbs 3:5", "Isaiah 40:31", "Joshua 1:9",
        "Psalms 46:1", "Romans 12:2", "Matthew 6:33", "Psalms 119:105",
        "2 Corinthians 5:17", "Galatians 5:22", "Ephesians 2:8", "1 Corinthians 13:4",
        "Psalms 27:1", "Isaiah 41:10", "Matthew 11:28", "John 14:6",
        "Romans 5:8", "Psalms 91:1", "Philippians 4:6", "Hebrews 11:1",
        "Psalms 34:8", "Matthew 5:14", "John 1:1", "Romans 10:9",
        "Psalms 139:14", "Colossians 3:23"
    };

    private MainActivity activity;
    private TextView continuePosition, votdText, votdRef;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        activity = (MainActivity) getActivity();
        if (activity == null) return;

        continuePosition = v.findViewById(R.id.continuePosition);
        votdText = v.findViewById(R.id.votdText);
        votdRef = v.findViewById(R.id.votdRef);

        v.findViewById(R.id.continueBtn).setOnClickListener(x -> goToCurrentChapter());
        v.findViewById(R.id.continueCard).setOnClickListener(x -> goToCurrentChapter());
        v.findViewById(R.id.navigateBtn).setOnClickListener(x -> activity.openNavigatePicker());
        v.findViewById(R.id.quickBookmarks).setOnClickListener(x -> openLibraryTab("bookmarks"));
        v.findViewById(R.id.quickNotes).setOnClickListener(x -> openLibraryTab("notes"));
        v.findViewById(R.id.quickSermons).setOnClickListener(x -> openLibraryTab("sermons"));
        v.findViewById(R.id.quickMarathon).setOnClickListener(x -> openLibraryTab("marathon"));
        v.findViewById(R.id.quickSearch).setOnClickListener(x -> {
            // Bottom nav item id for search
            View root = getActivity().findViewById(R.id.bottomNav);
            if (root instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) root).setSelectedItemId(R.id.nav_search);
            }
        });

        refreshData();
        loadVerseOfDay();
        onThemeChanged();
    }

    private void goToCurrentChapter() {
        activity.navigateToChapter(activity.getCurrentBook(), activity.getCurrentChapter(), null);
    }

    private void openLibraryTab(String tab) {
        activity.showLibrary();
    }

    public void refreshData() {
        if (activity == null || continuePosition == null) return;
        continuePosition.setText(activity.getCurrentBook() + " " + activity.getCurrentChapter());
    }

    private void loadVerseOfDay() {
        if (activity == null) return;
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String ref = VOTD_REFS[dayOfYear % VOTD_REFS.length];
        String translation = activity.getPrefsManager().getSettings().translation;
        String text = activity.getBibleData().resolveRefText(ref, translation);
        // Strip the leading verse number for a cleaner display
        text = text.replaceFirst("^\\d+\\s+", "");
        votdText.setText("\u201C" + text + "\u201D");
        votdRef.setText(ref);
    }

    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        AppTheme t = activity.getAppTheme();
        getView().setBackgroundColor(t.surface);

        applyCardColors(getView());
    }

    private void applyCardColors(View root) {
        AppTheme t = activity.getAppTheme();
        View continueCard = root.findViewById(R.id.continueCard);
        if (continueCard != null) continueCard.getBackground().setTint(t.card);
        if (continuePosition != null) continuePosition.setTextColor(t.primary);
        if (votdText != null) votdText.setTextColor(t.text);
        if (votdRef != null) votdRef.setTextColor(t.accent);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}
