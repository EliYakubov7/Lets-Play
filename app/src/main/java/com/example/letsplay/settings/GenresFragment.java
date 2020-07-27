package com.example.letsplay.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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


public class GenresFragment extends Fragment {
    private String[] genresNames;
    private int[] genresArr;
    private ArrayList<Integer> userGenres;
    private ArrayList<String> userGenresNames = new ArrayList<>();
    private GenreAdapter adapter;
    private Spinner genreSpinner;


    public GenresFragment() {
    }

    public static GenresFragment newInstance() {
        GenresFragment genresFragment = new GenresFragment();
        Bundle bundle = new Bundle();
        genresFragment.setArguments(bundle);
        return genresFragment;
    }

    private String findGenre(int genreInt) {
        for (int i = 0; i < genresNames.length; i++) {
            if (genresArr[i] == (genreInt))
                return genresNames[i];
        }
        return null;
    }

    private int findGenreInt(String genreString) {
        for (int i = 0; i < genresArr.length; i++) {
            if (genresNames[i].equals(genreString))
                return genresArr[i];
        }
        return 1;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_remove_genres_fragment, container, false);

        genresNames = getContext().getResources().getStringArray(R.array.genres);
        genresArr = getContext().getResources().getIntArray(R.array.genresInts);

        if (ProfileActivity.currentSignedInUser.getMusicGenres() == null) {
            ProfileActivity.currentSignedInUser.setMusicGenres(new ArrayList<Integer>());
        }
        userGenres = ProfileActivity.currentSignedInUser.getMusicGenres();

        for (Integer genreInt : userGenres) {
            userGenresNames.add(findGenre(genreInt));
        }


        Button addBtn = rootView.findViewById(R.id.add_genre_btn);
        genreSpinner = rootView.findViewById(R.id.add_genres_spinner);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addGenre(genreSpinner.getSelectedItemPosition()))
                    displaySnackbar(genresNames[genreSpinner.getSelectedItemPosition()] + " Genre was added", null, null);
                else
                    displaySnackbar("Genre already exist", null, null);
            }
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.genres_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new GenreAdapter(getContext(), userGenresNames);
        recyclerView.setAdapter(adapter);

        new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<String>() {
            @Override
            public boolean swipeLeft(String itemData) {
                final int position = ProfileActivity.currentSignedInUser.getMusicGenres().indexOf(findGenreInt(itemData));
                removeGenre(position);
                displaySnackbar(itemData + " genre removed.", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addGenre(position);
                    }
                });
                return true;
            }

            @Override
            public boolean swipeRight(String itemData) {
                final int position = ProfileActivity.currentSignedInUser.getMusicGenres().indexOf(findGenreInt(itemData));
                removeGenre(position);
                displaySnackbar(itemData + " genre removed.", "Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addGenre(position);
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

    private boolean addGenre(int position) {
        //if genre is already on list
        if (userGenresNames.contains(genresNames[position])) {
            return false;
        } else { //Add item to list
            int genre = genresArr[position];
            ProfileActivity.currentSignedInUser.getMusicGenres().add(genre);
            userGenresNames.add(genresNames[position]);
            FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
            adapter.notifyItemInserted(userGenresNames.size());
            return true;
        }
    }

    private void removeGenre(int position) {
        ProfileActivity.currentSignedInUser.getMusicGenres().remove(position);
        userGenresNames.remove(position);
        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
        adapter.notifyItemRemoved(position);
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);
        snack.show();
    }
}
