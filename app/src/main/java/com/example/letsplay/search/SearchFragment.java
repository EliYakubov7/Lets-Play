package com.example.letsplay.search;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.activities.SignUpActivity;
import com.example.letsplay.homefeeds.FeedFilterFragment;
import com.example.letsplay.objects.LastSearchPreferencesSaved;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.example.letsplay.profile.UserProfileFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment implements Serializable, SearchFilterFragment.FilterSearchListener {
    private Context context;
    private FriendsAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private TextView noResultsTv;
    private ImageView noResultsIv;
    public ProgressBar progressBar;
    private Handler handler = new Handler();
    private SearchView searchView;

    /*Firebase references*/
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference("Users-Locations");
    private DatabaseReference users = database.getReference("Users");
    private GeoFire geoFire;

    /*Search attributes*/
    private List<Integer> genresArr = new ArrayList<>();
    private List<Integer> instrumentsArr = new ArrayList<>();
    private String[] firstLastName = null;
    private String lastName;
    private boolean isByGenre = false, isByInstrument = false, isByDistance = false, isBySearchName = false, hasLastName = false;
    private int radius = 0, instrumentPosition, genrePosition;


    public final String TAG = "tests";


    public SearchFragment() {
    }

    public static SearchFragment newInstance() {
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        searchFragment.setArguments(bundle);
        return searchFragment;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        int[] arr1 = context.getResources().getIntArray(R.array.instrumentsInts);
        for (int element : arr1)
            instrumentsArr.add(element);

        instrumentPosition = 0;

        int[] arr2 = context.getResources().getIntArray(R.array.genresInts);
        for (int element : arr2)
            genresArr.add(element);

        genrePosition = 0;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        geoFire = new GeoFire(ref);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_fragment, container, false);

        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.action_nav_search).setChecked(true);

        //Get the toolbar
        Toolbar toolbar = rootView.findViewById(R.id.search_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        progressBar = rootView.findViewById(R.id.search_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        noResultsTv = rootView.findViewById(R.id.no_results_tv);
        noResultsIv = rootView.findViewById(R.id.search_bottom_frag_no_result_iv);
        noResultsIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ProfileActivity.isAGuest){
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    return;
                }

                LastSearchPreferencesSaved preferencesSaved = new LastSearchPreferencesSaved(isByDistance,
                        isBySearchName, isByGenre, isByInstrument, radius, genrePosition, instrumentPosition);
                BottomSheetDialogFragment filterSearchFragment = SearchFilterFragment.newInstance(SearchFragment.this, preferencesSaved);
                filterSearchFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), SearchFilterFragment.TAG);
            }
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.search_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        //Search view
        searchView = rootView.findViewById(R.id.search_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });


        adapter = new FriendsAdapter(userList, context);
        adapter.setListener(new FriendsAdapter.FriendsListener() {
            @Override
            public void onFriendClick(int position, View v) {
                loadProfileFragment(position);
            }
        });



        if (userList.size() > 0) {
            progressBar.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
            noResultsTv.setVisibility(View.GONE);
            noResultsIv.setVisibility(View.GONE);
        }
        else{
            if(!ProfileActivity.isAGuest) {
                //Initial the first list that is shown by distance of 50 Km from the user
                radius = 50;
                searchPeopleByDistance();
            }
            else
                getAllUsersFromFirebase();
        }

        recyclerView.setAdapter(adapter);


        return rootView;
    }


    /*Inflating the menu*/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        // Make menu text to white
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(ProfileActivity.isAGuest){
            displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), SignUpActivity.class));
                    getActivity().finish();
                }
            });
            return super.onOptionsItemSelected(item);
        }

        /*Sends the last preferences of the search back to the filter screen*/
        LastSearchPreferencesSaved preferencesSaved = new LastSearchPreferencesSaved(isByDistance,
                isBySearchName, isByGenre, isByInstrument, radius, genrePosition, instrumentPosition);
        BottomSheetDialogFragment filterSearchFragment = SearchFilterFragment.newInstance(this, preferencesSaved);
        filterSearchFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), SearchFilterFragment.TAG);
        return super.onOptionsItemSelected(item);
    }


    /*Callbacks from the listener for the search filters and searching methods*/

    /*Get the distance for the search radius*/
    @Override
    public void onSeek(int distance) {
        this.radius = distance;
    }

    /*Search by name*/
    @Override
    public void onSearchClick(String name) {
        progressBar.setVisibility(View.VISIBLE);

        userList.clear();
        adapter.notifyDataSetChanged();
        isBySearchName = true;
        /*Check if the search input contains last name too*/
        if (name.contains(" ")) {
            hasLastName = true;
            firstLastName = name.split(" ");
            name = firstLastName[0].substring(0, 1).toUpperCase(Locale.getDefault()).concat(firstLastName[0].substring(1));
            lastName = firstLastName[1].substring(0, 1).toUpperCase(Locale.getDefault()).concat(firstLastName[1].substring(1));
        } else
            lastName = name = name.substring(0, 1).toUpperCase(Locale.getDefault()).concat(name.substring(1));

        //Search for users which their first name starts at/equals to "name"
        users.orderByChild("firstName").startAt(name).endAt(name + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        userList.add(user);

                        if (hasLastName)
                            if (!user.getLastName().toLowerCase(Locale.getDefault()).contains(lastName.toLowerCase(Locale.getDefault())))
                                userList.remove(user);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (userList.size() > 0) {
                                noResultsTv.setVisibility(View.GONE);
                                noResultsIv.setVisibility(View.GONE);
                                searchView.setVisibility(View.VISIBLE);
                            } else {
                                noResultsTv.setVisibility(View.VISIBLE);
                                noResultsIv.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    progressBar.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();
                } else
                    searchByLastName(lastName.substring(0, 1).toUpperCase(Locale.getDefault()).concat(lastName.substring(1)));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /*Search for users which their last name starts at/equals to "lastName"*/
    private void searchByLastName(final String lastName) {
        users.orderByChild("lastName").startAt(lastName).endAt(lastName + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        if (!userList.contains(user))
                            userList.add(user);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (userList.size() > 0) {
                                noResultsTv.setVisibility(View.GONE);
                                noResultsIv.setVisibility(View.GONE);
                            } else {
                                noResultsTv.setVisibility(View.VISIBLE);
                                noResultsIv.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    progressBar.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();
                } else
                    progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*Check which spinner is selected and which item in it to search*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.search_filter_genres_spinner:
                this.genrePosition = position;
                break;
            case R.id.search_filter_instrument_spinner:
                this.instrumentPosition = position;
                break;
        }
    }

    /*Check how many checkboxes are checked for the search */
    @Override
    public void onItemChecked(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.search_filter_genres_cb:
                isByGenre = isChecked;
                break;
            case R.id.search_filter_instrument_cb:
                isByInstrument = isChecked;
                break;
            case R.id.search_filter_distance_cb:
                isByDistance = isChecked;
                break;
        }
    }


    /*Handle the search by the parameters defined above after the dialog dismisses*/
    @Override
    public void onDismiss() {
        userList.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);

        if (isBySearchName)
            isBySearchName = false;
        else if (isByDistance) {
            searchPeopleByDistance();
            return;
        } else if (isByInstrument || isByGenre) {
            getAllUsersFromFirebase();
            return;
        }

        progressBar.setVisibility(View.GONE);
    }


    /*Retrieve people by distance*/
    private void searchPeopleByDistance() {
        /*Search by distance query which returns the users keys*/
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(ProfileActivity.currentSignedInUser.getLatitude(),
                ProfileActivity.currentSignedInUser.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                /*Retrieve user object For each user key*/
                users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user1 = snapshot.getValue(User.class);
                            //Check if the user matches to all parameters parameters
                            if (isMatchToSearchParameters(user1)) {
                                userList.add(user1);
                                searchView.setVisibility(View.VISIBLE);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        noResultsTv.setVisibility(View.GONE);
                                        noResultsIv.setVisibility(View.GONE);
                                    }
                                });

                                adapter.addToFriendListFull(user1);
                                adapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);

                            } else
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(userList.size()==0){
                                            searchView.setVisibility(View.INVISIBLE);
                                            noResultsTv.setVisibility(View.VISIBLE);
                                            noResultsIv.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                geoQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    /*Retrieve all users*/
    private void getAllUsersFromFirebase() {
        users.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        //Check if the user matches to all parameters parameters
                        if (isMatchToSearchParameters(user))
                            userList.add(user);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (userList.size() > 0) {
                                progressBar.setVisibility(View.GONE);
                                searchView.setVisibility(View.VISIBLE);
                                noResultsTv.setVisibility(View.GONE);
                                noResultsIv.setVisibility(View.GONE);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                noResultsTv.setVisibility(View.VISIBLE);
                                searchView.setVisibility(View.INVISIBLE);
                                noResultsIv.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    adapter.setFriendListFull(userList);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*Check other parameters to search*/
    private boolean isMatchToSearchParameters(User user) {

        //Check if one of the user's instruments (main/secondary) not fits the search
        if (isByInstrument) {
            if (user.getMainInstrument() != null) {
                if (user.getMainInstrument().getInstrumentResId() != instrumentsArr.get(instrumentPosition)) {
                    if (user.getSecondaryInstrument() != null) {
                        if (user.getSecondaryInstrument().getInstrumentResId() != instrumentsArr.get(instrumentPosition))
                            return false;
                    } else
                        return false;
                }
            } else if (user.getSecondaryInstrument() != null) {
                if (user.getSecondaryInstrument().getInstrumentResId() != instrumentsArr.get(instrumentPosition))
                    return false;
            } else
                return false;
        }
        //Check if one of the user's genres does not fit the search
        if (isByGenre) {
            if (user.getMusicGenres() == null)
                return false;
            else if (!user.getMusicGenres().contains(genresArr.get(genrePosition)))
                return false;
        }
        return true;
    }

    /*Loads user profile fragment*/
    private void loadProfileFragment(int position) {
        searchView.setQuery("", false);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        if(!ProfileActivity.isAGuest) {
            //Open my user profile
            if (userList.get(position).getUid().equals(ProfileActivity.currentSignedInUser.getUid())) {
                transaction.replace(R.id.fragment_container, UserProfileFragment.newInstance()).addToBackStack(null).commit();
            }
            //Open other user profile
            else {
                transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                        .newInstance(UsersManager.getUserByKey(userList.get(position).getUid()))).addToBackStack(null).commit();
            }
        }
        //Open other user profile
        else {
            transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                    .newInstance(UsersManager.getUserByKey(userList.get(position).getUid()))).addToBackStack(null).commit();
        }
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                .setAction(actionName, action);
        snack.show();
    }

}
