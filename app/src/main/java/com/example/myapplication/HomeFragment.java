package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView rvPlaces;
    private SearchView searchView;
    private MaterialButton btnFilter;

    private PlaceAdapter adapter;

    private String currentQuery = "";
    private final Set<String> selectedCategories = new HashSet<>();

    private final String[] filterLabels = {"Amfiteatri", "Labaratorii", "Kantina", "Sluzbi"};
    private final String[] categoryValues = {"Amphitheater", "Lab", "Kantina", "Sluzba"};
    private final boolean[] checked = {false, false, false, false};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        rvPlaces = v.findViewById(R.id.rvPlaces);
        searchView = v.findViewById(R.id.searchView);
        btnFilter = v.findViewById(R.id.btnFilter);

        rvPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadPlaces();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentQuery = (query == null) ? "" : query;
                    applyFilters();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentQuery = (newText == null) ? "" : newText;
                    applyFilters();
                    return true;
                }
            });
        }

        if (btnFilter != null) {
            btnFilter.setOnClickListener(view -> showFilterDialog());
        }

        return v;
    }

    private void loadPlaces() {
        if (!isAdded()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            String username = getUsername();
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Keep your seed behavior
            AppDatabase.seedIfEmpty(db);

            PlaceDao placeDao = db.placeDao();
            FavoriteDao favoriteDao = db.favoriteDao();

            List<PlaceEntity> entities = placeDao.getAll();

            ArrayList<Place> data = new ArrayList<>();
            for (PlaceEntity e : entities) {
                boolean isFav = favoriteDao.isFavorite(username, e.id) == 1;

                data.add(new Place(
                        e.id,
                        e.title,
                        e.category,
                        e.description,
                        e.location,
                        e.phone,
                        e.email,
                        e.lat,
                        e.lng,
                        isFav
                ));
            }

            if (!isAdded() || getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                adapter = new PlaceAdapter(
                        data,
                        place -> requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frameLayout, PlaceDetailsFragment.newInstance(place))
                                .addToBackStack(null)
                                .commit(),
                        (place, newFavState) -> {
                            // ✅ Per-user favorites in favorites table
                            Executors.newSingleThreadExecutor().execute(() -> {
                                String u = getUsername();
                                FavoriteDao favDao = AppDatabase.getInstance(requireContext()).favoriteDao();

                                if (newFavState) {
                                    favDao.add(new FavoriteEntity(u, place.id));
                                } else {
                                    favDao.remove(u, place.id);
                                }
                            });
                        }
                );

                rvPlaces.setAdapter(adapter);
                applyFilters();
            });
        });
    }

    private String getUsername() {
        return requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("username", "");
    }

    private void showFilterDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        b.setTitle("Filter");

        b.setMultiChoiceItems(filterLabels, checked, (dialog, which, isChecked) -> checked[which] = isChecked);

        b.setPositiveButton("Apply", (dialog, which) -> {
            selectedCategories.clear();
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) selectedCategories.add(categoryValues[i]);
            }
            applyFilters();
        });

        b.setNegativeButton("Cancel", null);

        b.setNeutralButton("Clear", (dialog, which) -> {
            for (int i = 0; i < checked.length; i++) checked[i] = false;
            selectedCategories.clear();
            applyFilters();
        });

        b.show();
    }

    private void applyFilters() {
        if (adapter == null) return;
        adapter.applySearchAndCategoryFilter(currentQuery, selectedCategories);
    }


}
