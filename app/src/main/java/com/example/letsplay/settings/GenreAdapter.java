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

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {
    private ArrayList<String> genres;
    private String[] genresNames;
    private LayoutInflater inflater;
    private Context context;

    public class ViewHolder extends SwipeToAction.ViewHolder<String> {
        public TextView genreTv;

        public ViewHolder(View itemView) {
            super(itemView);
            genreTv = itemView.findViewById(R.id.tvArtistName);
        }

    }

    GenreAdapter(Context context, ArrayList<String> data) {
        this.inflater = LayoutInflater.from(context);
        this.genres = data;
        this.context = context;
    }

    @NonNull
    @Override
    public GenreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.artist_recycler_cell, parent, false);
        //genresNames = context.getResources().getStringArray(R.array.genres);
        return new GenreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String genre = genres.get(position);
        holder.genreTv.setText(genre);
        holder.data = genre;
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

}

