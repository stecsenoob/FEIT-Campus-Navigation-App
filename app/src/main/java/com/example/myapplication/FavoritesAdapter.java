package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

    public interface OnFavoriteClick {
        void onHeartClick(Place place, boolean newFavState);
    }

    public interface OnPlaceClick {
        void onPlaceClick(Place place);
    }

    private final ArrayList<Place> items = new ArrayList<>();
    private final OnPlaceClick clickListener;
    private final OnFavoriteClick heartListener;

    public FavoritesAdapter(ArrayList<Place> data,
                            OnPlaceClick clickListener,
                            OnFavoriteClick heartListener) {
        if (data != null) items.addAll(data);
        this.clickListener = clickListener;
        this.heartListener = heartListener;
    }

    public void setItems(ArrayList<Place> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place p = items.get(position);

        holder.title.setText(p.title);

        // Whole card opens details
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onPlaceClick(p);
        });

        // Always red heart in favorites
        holder.heart.setImageResource(R.drawable.favorite_24);

        holder.heart.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            p.isFavorite = false;

            if (heartListener != null) {
                heartListener.onHeartClick(p, false);
            }

            // Remove from favorites list immediately
            items.remove(pos);
            notifyItemRemoved(pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageView heart;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            heart = itemView.findViewById(R.id.imgHeart);
        }
    }
}
