package com.example.letsplay.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;

import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private List<String> artists;
    private LayoutInflater inflater;

    public class ViewHolder extends SwipeToAction.ViewHolder<String>{
        public TextView artistTv;

        public ViewHolder(View itemView) {
            super(itemView);
            artistTv = itemView.findViewById(R.id.tvArtistName);
        }
    }

    ArtistAdapter(Context context, ArrayList<String> data) {
        this.inflater = LayoutInflater.from(context);
        this.artists = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.artist_recycler_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String artist = artists.get(position);
        holder.artistTv.setText(artist);
        holder.data = artist;
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

}