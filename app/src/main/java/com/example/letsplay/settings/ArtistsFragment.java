package com.example.letsplay.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;

public class ArtistsFragment extends Fragment {
    private ArrayList<String> artists = new ArrayList<>();
    private ArtistAdapter adapter;


    public ArtistsFragment() {
    }

    public static ArtistsFragment newInstance() {
        ArtistsFragment artistsFragment = new ArtistsFragment();
        Bundle bundle = new Bundle();
        artistsFragment.setArguments(bundle);
        return artistsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_remove_artists_fragment, container, false);
        artists = ProfileActivity.currentSignedInUser.getArtists();

        Button addBtn = rootView.findViewById(R.id.add_artist_btn);
        final EditText artistEt = rootView.findViewById(R.id.add_artist_et);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String artist = artistEt.getText().toString();
                if (!artist.isEmpty()) {
                    artists.add(artist);
                    FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                    adapter.notifyItemInserted(artist.length());
                    artistEt.setText("");
                } else {
                    displaySnackbar("Can't add empty text", null, null);
                }
            }
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.artist_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new ArtistAdapter(getContext(), artists);
        recyclerView.setAdapter(adapter);

        new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<String>() {
            @Override
            public boolean swipeLeft(final String itemData) {
                final int position = artists.indexOf(itemData);
                artists.remove(itemData);
                FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                adapter.notifyItemRemoved(position);
                displaySnackbar(itemData + " have been removed.", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        artists.add(itemData);
                        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                        adapter.notifyItemInserted(position);
                    }
                });
                return true;
            }

            @Override
            public boolean swipeRight(final String itemData) {
                final int position = artists.indexOf(itemData);
                artists.remove(itemData);
                FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                adapter.notifyItemRemoved(position);
                displaySnackbar(itemData + " have been removed.", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        artists.add(itemData);
                        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                        adapter.notifyItemInserted(position);
                    }
                });
                return true;
            }

            @Override
            public void onClick(String itemData) {
                Toast.makeText(getContext(), "Swipe to delete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(String itemData) {

            }

        });

        return rootView;
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        // View v = snack.getView();
        // v.setBackgroundColor(getResources().getColor(R.color.secondary));
        //((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        //((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snack.show();
    }
}
