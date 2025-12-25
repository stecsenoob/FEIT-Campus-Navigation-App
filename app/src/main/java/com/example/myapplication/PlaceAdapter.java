package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.VH> {

    public interface OnPlaceClick {
        void onPlaceClick(Place place);
    }

    // Fragment will handle SQLite (FavoriteDao)
    public interface OnFavoriteClick {
        void onHeartClick(Place place, boolean newFavState);
    }

    private final ArrayList<Place> allItems = new ArrayList<>();
    private final ArrayList<Place> shownItems = new ArrayList<>();

    private final OnPlaceClick clickListener;
    private final OnFavoriteClick heartListener;

    public PlaceAdapter(List<Place> data,
                        OnPlaceClick clickListener,
                        OnFavoriteClick heartListener) {

        if (data != null) {
            allItems.addAll(data);
            shownItems.addAll(data);
        }
        this.clickListener = clickListener;
        this.heartListener = heartListener;
    }

    // Used by HomeFragment search + filter
    public void applySearchAndCategoryFilter(String query, Set<String> categories) {
        String q = (query == null) ? "" : query.trim().toLowerCase(Locale.ROOT);
        Set<String> cats = (categories == null) ? new HashSet<>() : categories;

        shownItems.clear();

        for (Place p : allItems) {
            if (p == null) continue;

            if (!cats.isEmpty() && (p.category == null || !cats.contains(p.category))) {
                continue;
            }

            if (!q.isEmpty()) {
                String title = (p.title == null) ? "" : p.title.toLowerCase(Locale.ROOT);
                String category = (p.category == null) ? "" : p.category.toLowerCase(Locale.ROOT);
                String desc = (p.description == null) ? "" : p.description.toLowerCase(Locale.ROOT);

                if (!(title.contains(q) || category.contains(q) || desc.contains(q))) continue;
            }

            shownItems.add(p);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place p = shownItems.get(position);

        if (holder.title != null) {
            holder.title.setText(p.title);
        }

        if (holder.subtitle != null) {
            holder.subtitle.setText(p.description);
        }

        // Open details
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPlaceClick(p);
            }
        });

        // ❤️ Favorite handling (UI only)
        if (holder.heart != null) {

            holder.heart.setImageResource(
                    p.isFavorite
                            ? R.drawable.favorite_24
                            : R.drawable.outline_favorite_24
            );

            holder.heart.setOnClickListener(v -> {
                boolean newState = !p.isFavorite;

                // update UI state immediately
                p.isFavorite = newState;

                holder.heart.setImageResource(
                        newState
                                ? R.drawable.favorite_24
                                : R.drawable.outline_favorite_24
                );

                // delegate DB update to Fragment
                if (heartListener != null) {
                    heartListener.onHeartClick(p, newState);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return shownItems.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        ImageView heart;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            subtitle = itemView.findViewById(R.id.txtSubtitle); // optional
            heart = itemView.findViewById(R.id.imgHeart);       // optional
        }
    }
}
