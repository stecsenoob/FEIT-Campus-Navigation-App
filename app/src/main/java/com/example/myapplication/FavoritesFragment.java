package com.example.myapplication;

import android.content.Context;
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

        rvFavorites = v.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoritesAdapter(
                new ArrayList<>(),
                place -> requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, PlaceDetailsFragment.newInstance(place))
                        .addToBackStack(null)
                        .commit(),
                (place, newFavState) -> Executors.newSingleThreadExecutor().execute(() -> {
                    String user = getUsername();
                    AppDatabase.getInstance(requireContext())
                            .favoriteDao()
                            .remove(user, place.id);
                })
        );

        rvFavorites.setAdapter(adapter);
        loadFavorites();

        return v;
    }

    private void loadFavorites() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String user = getUsername();

            List<PlaceEntity> entities =
                    AppDatabase.getInstance(requireContext())
                            .favoriteDao()
                            .getFavoritesForUser(user);

            ArrayList<Place> list = new ArrayList<>();
            for (PlaceEntity e : entities) {
                list.add(new Place(
                        e.id, e.title, e.category, e.description,
                        e.location, e.phone, e.email,
                        e.lat, e.lng, true
                ));
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> adapter.setItems(list));
        });
    }

    private String getUsername() {
        return requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("username", "");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}
