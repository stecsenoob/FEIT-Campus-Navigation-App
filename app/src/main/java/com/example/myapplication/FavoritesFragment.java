package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private FavoritesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        // ✅ Matches fragment_favorites.xml
        rvFavorites = v.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoritesAdapter(
                new ArrayList<>(),
                place -> requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, PlaceDetailsFragment.newInstance(place))
                        .addToBackStack(null)
                        .commit(),
                (place, newFavState) -> Executors.newSingleThreadExecutor().execute(() ->
                        AppDatabase.getInstance(requireContext())
                                .placeDao()
                                .setFavorite(place.title, newFavState)
                )
        );

        rvFavorites.setAdapter(adapter);

        loadFavorites();

        return v;
    }

    private void loadFavorites() {
        if (!isAdded()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            PlaceDao dao = db.placeDao();

            List<PlaceEntity> entities = dao.getFavorites();

            ArrayList<Place> favorites = new ArrayList<>();
            for (PlaceEntity e : entities) {
                favorites.add(new Place(
                        e.title,
                        e.category,
                        e.description,
                        e.location,
                        e.phone,
                        e.email,
                        e.lat,
                        e.lng,
                        e.isFavorite
                ));
            }

            if (!isAdded() || getActivity() == null) return;

            getActivity().runOnUiThread(() -> adapter.setItems(favorites));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // refresh when returning
    }
}
