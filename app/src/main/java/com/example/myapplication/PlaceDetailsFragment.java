package com.example.myapplication;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executors;

public class PlaceDetailsFragment extends Fragment {

    private static final String ARG_PLACE = "arg_place";

    public PlaceDetailsFragment() {
        super(R.layout.fragment_place_details);
    }

    public static PlaceDetailsFragment newInstance(Place place) {
        PlaceDetailsFragment f = new PlaceDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_PLACE, place);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView titleTop = view.findViewById(R.id.txtTitleTop);
        TextView category = view.findViewById(R.id.txtCategory);
        TextView description = view.findViewById(R.id.txtDescription);
        TextView location = view.findViewById(R.id.txtLocation);
        TextView phone = view.findViewById(R.id.txtPhone);
        TextView email = view.findViewById(R.id.txtEmail);
        TextView btnFavorite = view.findViewById(R.id.btnFavorite);

        // ✅ make it effectively final (no reassign)
        final Place place = (getArguments() != null)
                ? (Place) getArguments().getSerializable(ARG_PLACE)
                : null;

        if (place == null) return;

        // ✅ use final title in lambdas
        final String placeTitle = place.title;

        if (place.title != null) titleTop.setText(place.title);
        if (place.category != null) category.setText(place.category);

        if (place.description != null && !place.description.trim().isEmpty()) {
            description.setText(place.description);
        } else {
            description.setText("");
        }

        if (place.location != null) location.setText(place.location);
        if (place.phone != null) phone.setText("Телефон: " + place.phone);
        if (place.email != null) email.setText("e-mail: " + place.email);

        setupClickableLocation(location, place);

        // ✅ Favorites toggle
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Load current favorite state
        Executors.newSingleThreadExecutor().execute(() -> {
            PlaceEntity e = db.placeDao().getByTitle(placeTitle);
            final boolean isFav = (e != null && e.isFavorite);

            requireActivity().runOnUiThread(() -> setFavoriteText(btnFavorite, isFav));
        });

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    PlaceEntity e = db.placeDao().getByTitle(placeTitle);
                    boolean cur = (e != null && e.isFavorite);
                    boolean next = !cur;

                    db.placeDao().setFavorite(placeTitle, next);

                    requireActivity().runOnUiThread(() -> {
                        setFavoriteText(btnFavorite, next);
                        Toast.makeText(requireContext(),
                                next ? "Додадено во Favorites" : "Тргнато од Favorites",
                                Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void setFavoriteText(TextView btnFavorite, boolean fav) {
        if (btnFavorite == null) return;
        btnFavorite.setText(fav ? "Remove from Favorites" : "Add to Favorites");
    }

    private void setupClickableLocation(TextView locationView, Place place) {
        locationView.setClickable(true);
        locationView.setFocusable(true);
        locationView.setPaintFlags(locationView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        locationView.setOnClickListener(v -> {
            Fragment map = MapFragment.newInstanceFocus(place.lat, place.lng, place.title);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, map)
                    .addToBackStack(null)
                    .commit();
        });
    }
}


