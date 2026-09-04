package com.fountainpdl.bible.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fountainpdl.bible.MainActivity;
import com.fountainpdl.bible.R;
import com.fountainpdl.bible.adapters.SearchResultAdapter;
import com.fountainpdl.bible.models.SearchResult;

import java.util.List;

public class SearchFragment extends Fragment {

    private MainActivity activity;
    private EditText searchInput;
    private TextView searchStatus, emptyState;
    private RecyclerView resultsRecycler;
    private SearchResultAdapter adapter;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        activity = (MainActivity) getActivity();
        if (activity == null) return;

        searchInput = v.findViewById(R.id.searchInput);
        searchStatus = v.findViewById(R.id.searchStatus);
        emptyState = v.findViewById(R.id.searchEmptyState);
        resultsRecycler = v.findViewById(R.id.searchResultsRecycler);

        resultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchResultAdapter(result -> {
            activity.navigateToChapter(result.book, result.chapter, result.verse);
        });
        adapter.setTheme(activity.getAppTheme());
        resultsRecycler.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { scheduleSearch(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        onThemeChanged();
    }

    private void scheduleSearch(String query) {
        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
        debounceRunnable = () -> performSearch(query);
        debounceHandler.postDelayed(debounceRunnable, 280);
    }

    private void performSearch(String query) {
        if (activity == null) return;
        if (query == null || query.trim().length() < 2) {
            adapter.setResults(new java.util.ArrayList<>());
            resultsRecycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyState.setText(getString(R.string.empty_search));
            searchStatus.setText("");
            return;
        }
        String translation = activity.getPrefsManager().getSettings().translation;
        List<SearchResult> results = activity.getBibleData().search(query.trim(), translation, 100);
        adapter.setResults(results);
        if (results.isEmpty()) {
            resultsRecycler.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyState.setText(getString(R.string.empty_search_no_results));
        } else {
            resultsRecycler.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
        searchStatus.setText(results.size() + " result" + (results.size() != 1 ? "s" : "") + " in " + translation);
    }

    public void onThemeChanged() {
        if (activity == null || getView() == null) return;
        getView().setBackgroundColor(activity.getAppTheme().surface);
        if (adapter != null) { adapter.setTheme(activity.getAppTheme()); adapter.notifyDataSetChanged(); }
        if (searchStatus != null) searchStatus.setTextColor(activity.getAppTheme().sub);
        if (emptyState != null) emptyState.setTextColor(activity.getAppTheme().sub);
    }
}
