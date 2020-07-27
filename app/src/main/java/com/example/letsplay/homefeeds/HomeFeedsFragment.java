package com.example.letsplay.homefeeds;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.activities.SignUpActivity;
import com.example.letsplay.chats.ChatWindowFragment;
import com.example.letsplay.objects.Feed;
import com.example.letsplay.objects.LastHomeFeedsPreferencesSaved;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.example.letsplay.profile.UserProfileFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class HomeFeedsFragment extends Fragment implements FeedFilterFragment.OnSeekAndCheckListener, Serializable {

    private Context context;
    /*Feed list*/
    private List<Feed> feedList = new ArrayList<>();
    private List<Feed> feedListFull = new ArrayList<>();
    private List<Feed> myFeeds = new ArrayList<>();

    /*Feed key lists*/
    private List<String> feedKeyList = new ArrayList<>();
    private List<String> feedKeyListFull = new ArrayList<>();
    private List<String> myFeedKeyList = new ArrayList<>();

    /*Friend list and all users list*/
    private List<User> friendsList = new ArrayList<>();
    private List<User> allUsersList = new ArrayList<>();


    private FeedAdapter adapter;
    private int radius = 0;
    private String TAG = "tests";
    private boolean isByDistance = false, isByMostRecent = true, isByFriends = false, isByMyFeeds = false;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noFeedsTv;
    private View rootView;
    private Menu menu;
    private ImageButton likeBtn;


    /*Firebase references*/
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference("Users-Locations");
    private GeoFire geoFire;
    //Reference to Table "Feeds"
    private DatabaseReference feeds = database.getReference("Feeds");


    public HomeFeedsFragment() {
    }

    public static HomeFeedsFragment newInstance() {
        HomeFeedsFragment homeFeedsFragment = new HomeFeedsFragment();
        Bundle bundle = new Bundle();
        homeFeedsFragment.setArguments(bundle);
        return homeFeedsFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.home_feeds_fragment, container, false);
        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.action_nav_feed).setChecked(true);

        progressBar = rootView.findViewById(R.id.home_feed_progress_bar);
        likeBtn = rootView.findViewById(R.id.feed_cell_like_ib);
        noFeedsTv = rootView.findViewById(R.id.no_feeds_tv);

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.home_feeds_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        recyclerView = rootView.findViewById(R.id.home_feeds_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        //Loads the feed list from firebase
        loadInitialFeedList();

        //Initialize the geo fire object
        geoFire = new GeoFire(ref);

        //Swipe to refresh the feeds
        final SwipeRefreshLayout pullToRefresh = rootView.findViewById(R.id.swipe_to_refresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*Clear all before reloading*/
                feedList.clear();
                feedKeyList.clear();
                feedListFull.clear();
                feedKeyListFull.clear();
                myFeeds.clear();
                myFeedKeyList.clear();

                loadInitialFeedList();

                pullToRefresh.setRefreshing(false);
            }
        });

        return rootView;
    }


    private void updateFeed(int position) {
        feeds.child(feedKeyList.get(position)).setValue(feedList.get(position));
    }

    private void loadInitialFeedList() {
        /*Loads all users from firebase, and filter them to the current user's friends only by key inside "friendsList"
         * and loads all of them into "allUsers" */

        /*Clear all before reloading*/
        feedList.clear();
        feedKeyList.clear();
        feedListFull.clear();
        feedKeyListFull.clear();
        myFeeds.clear();
        myFeedKeyList.clear();


        UsersManager.loadUsers();
        UsersManager.setListener(new UsersManager.LoadListener() {
            @Override
            public void onFinishLoad() {
                if (!ProfileActivity.isAGuest) {
                    if (ProfileActivity.currentSignedInUser.getFriends() != null) {
                        for (User user : UsersManager.getUsers()) {
                            if (ProfileActivity.currentSignedInUser.getFriends().contains(user.getUid()))
                                friendsList.add(user);
                        }
                    }
                }

                allUsersList.addAll(UsersManager.getUsers());
            }
        });




        /*Loads all the feeds from firebase for later filtering*/
        feeds.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    noFeedsTv.setVisibility(View.GONE);
                    for (DataSnapshot feed : snapshot.getChildren()) {
                        if (!ProfileActivity.isAGuest) {
                            if (feed.getValue(Feed.class).getAuthor().equals(ProfileActivity.currentSignedInUser.getUid())) {
                                myFeeds.add(feed.getValue(Feed.class));
                                myFeedKeyList.add(feed.getKey());
                            } else {
                                feedList.add(feed.getValue(Feed.class));
                                feedKeyList.add(feed.getKey());
                            }
                        } else {
                            feedList.add(feed.getValue(Feed.class));
                            feedKeyList.add(feed.getKey());
                        }
                    }


                    feedListFull.addAll(feedList);
                    feedListFull.addAll(myFeeds);
                    feedKeyListFull.addAll(feedKeyList);
                    feedKeyListFull.addAll(myFeedKeyList);

                    removeDuplicates();

                    loadAdapterAndRecycler();
                } else {
                    noFeedsTv.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("tests", error.getDetails());
            }
        });


    }

    private void loadAdapterAndRecycler() {
        adapter = new FeedAdapter(feedList, context);
        adapter.setFeedListener(new FeedAdapter.FeedListener() {
            @Override
            public void onCommentClick(int position, View v) {
                if (ProfileActivity.isAGuest) {
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    return;
                }
                CommentsDialogFragment commentsFragment = CommentsDialogFragment.newInstance(feedList.get(position), feedKeyList.get(position), position);
                commentsFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), CommentsDialogFragment.TAG);
                commentsFragment.setListener(new CommentsDialogFragment.CommentListener() {
                    @Override
                    public void onComment(int position) {
                        adapter.notifyItemChanged(position);
                    }
                });
            }

            @Override
            public void onLikeClick(int position, View v) {
                if (ProfileActivity.isAGuest) {
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    return;
                }
                Feed feed = feedList.get(position);
                ImageButton likeBtn = v.findViewById(R.id.feed_cell_like_ib);

                if (feed.getLikesControl() == null)
                    feed.setLikesControl(new HashMap<String, Boolean>());
                /*If the user didn't like the post yet*/
                if (!feed.getLikesControl().containsKey(ProfileActivity.currentSignedInUser.getUid())) {
                    feed.getLikesControl().put(ProfileActivity.currentSignedInUser.getUid(), true);
                    feed.setLikes(feed.getLikes() + 1);
                    //ImageViewCompat.setImageTintList(likeBtn, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));

                } else {//if the user liked the post before
                    if (feed.getLikesControl().get(ProfileActivity.currentSignedInUser.getUid())) {
                        feed.getLikesControl().put(ProfileActivity.currentSignedInUser.getUid(), false);
                        feed.setLikes(feed.getLikes() - 1);
                        //ImageViewCompat.setImageTintList(likeBtn, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGray)));

                    } else {
                        feed.getLikesControl().put(ProfileActivity.currentSignedInUser.getUid(), true);
                        feed.setLikes(feed.getLikes() + 1);
                        //ImageViewCompat.setImageTintList(likeBtn, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
                    }
                }

                //Updates the feed in firebase
                updateFeed(position);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onProfileImageClick(int position, View v) {
                loadProfileFragment(position);
            }
        });

        recyclerView.setAdapter(adapter);
        loadFeedsByMostRecent();
    }

    /*Remove duplicates if exist*/
    private void removeDuplicates(){
        for(int i=0;i<feedList.size();i++){
            for(int j=0;j<feedList.size();j++){
                if(i!=j&&feedList.get(i).equals(feedList.get(j))){
                    feedList.remove(j);
                    feedKeyList.remove(j);
                }
            }
        }
        for(int i=0;i<myFeeds.size();i++){
            for(int j=0;j<myFeeds.size();j++){
                if(i!=j&&myFeeds.get(i).equals(myFeeds.get(j))){
                    myFeeds.remove(j);
                    myFeedKeyList.remove(j);
                }
            }
        }
        for(int i=0;i<feedListFull.size();i++){
            for(int j=0;j<feedListFull.size();j++){
                if(i!=j&&feedListFull.get(i).equals(feedListFull.get(j))){
                    feedListFull.remove(j);
                    feedKeyListFull.remove(j);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.home_feeds_menu, menu);

        // Make menu text to white
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*Call the filter by fragment and add post fragment*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home_feeds_filter:

                if (ProfileActivity.isAGuest) {
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    break;
                }

                /*Restoring the last preferences defined back to the filter screen*/
                LastHomeFeedsPreferencesSaved preferencesSaved = new LastHomeFeedsPreferencesSaved(isByFriends, isByMostRecent,
                        isByMyFeeds, isByDistance, radius);
                FeedFilterFragment.newInstance(this, preferencesSaved).show(((AppCompatActivity) context)
                        .getSupportFragmentManager(), FeedFilterFragment.TAG);
                break;
            case R.id.action_home_feeds_add:

                if (ProfileActivity.isAGuest) {
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    break;
                }

                PostFeedFragment postFeedFragment = PostFeedFragment.newInstance();
                postFeedFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), PostFeedFragment.TAG);
                postFeedFragment.setListener(new PostFeedFragment.PostListener() {
                    @Override
                    public void onPost(final String post) {

                        /*Clear all before reloading*/
                        feedList.clear();
                        feedKeyList.clear();
                        feedListFull.clear();
                        feedKeyListFull.clear();
                        myFeeds.clear();
                        myFeedKeyList.clear();

                        /*Loads all the feeds from firebase before uploading the post*/
                        feeds.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    noFeedsTv.setVisibility(View.GONE);
                                    for (DataSnapshot feed : snapshot.getChildren()) {
                                        if (!ProfileActivity.isAGuest) {
                                            if (feed.getValue(Feed.class).getAuthor().equals(ProfileActivity.currentSignedInUser.getUid())) {
                                                myFeeds.add(feed.getValue(Feed.class));
                                                myFeedKeyList.add(feed.getKey());
                                            } else {
                                                feedList.add(feed.getValue(Feed.class));
                                                feedKeyList.add(feed.getKey());
                                            }
                                        } else {
                                            feedList.add(feed.getValue(Feed.class));
                                            feedKeyList.add(feed.getKey());
                                        }
                                    }

                                    feedListFull.addAll(feedList);
                                    feedListFull.addAll(myFeeds);
                                    feedKeyListFull.addAll(feedKeyList);
                                    feedKeyListFull.addAll(myFeedKeyList);

                                    removeDuplicates();

                                    loadAdapterAndRecycler();
                                } else {
                                    noFeedsTv.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                                //Upload the post to the feed wall
                                post(post);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("tests", error.getDetails());
                            }
                        });

                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Posts the post to the feed wall*/
    private void post(String post){
        Feed feed = new Feed(ProfileActivity.currentSignedInUser.getUid(), post);
        /*Update feed list and its keys*/
        feedList.add(0,feed);
        feedKeyList.add(0,String.valueOf(feedListFull.size()-1));
        //Collections.swap(feedList,feedList.size()-1,0);
        /*Update my feed list and its keys*/
        myFeeds.add(0,feed);
        myFeedKeyList.add(0,String.valueOf(feedListFull.size()-1));
       // Collections.swap(myFeeds,myFeeds.size()-1,0);
        /*Update feed full list and its keys*/
        feedListFull.add(0,feed);
        feedKeyListFull.add(0,String.valueOf(feedListFull.size()-1));
        //Collections.swap(feedListFull,feedListFull.size()-1,0);

        /*Update firebase*/
        feeds.child(String.valueOf(feedListFull.size()-1)).setValue(feed);


        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
    }


    /*Fragment interface methods to implement*/

    /*Indicate the distance in radius in case of filter by distance*/
    @Override
    public void onSeeking(int distance) {
        radius = distance;
    }

    /*Indicate which radio button is checked*/
    @Override
    public void onItemChecked(int checkedId) {
        switch (checkedId) {
            //show feeds by the given distance
            case R.id.distance_rb:
                if (radius == 0)
                    return;
                isByDistance = true;
                isByFriends = false;
                isByMostRecent = false;
                isByMyFeeds = false;
                Log.d(TAG, "feeds in radius of " + radius + " km");
                break;
            //show feeds of friends only
            case R.id.friends_rb:
                isByDistance = false;
                isByFriends = true;
                isByMostRecent = false;
                isByMyFeeds = false;
                Log.d(TAG, "My friends' feeds");
                break;
            //show most recent feeds
            case R.id.most_recent_rb:
                isByDistance = false;
                isByFriends = false;
                isByMostRecent = true;
                isByMyFeeds = false;
                Log.d(TAG, "Most recent feeds");
                break;
            //show my feeds only
            case R.id.my_posts_rb:
                isByDistance = false;
                isByFriends = false;
                isByMostRecent = false;
                isByMyFeeds = true;
                Log.d(TAG, "My feeds");
                break;
        }
    }

    /*When the user cancels the dialog fragment*/
    @Override
    public void onDismiss() {
        if (isByDistance)
            loadFeedsByDistance();
        else if (isByMostRecent)
            loadFeedsByMostRecent();
        else if (isByFriends)
            loadFeedsOfMyFriends();
        else if (isByMyFeeds)
            loadFeedsByMyFeeds();
    }

    /*Load feeds by distance*/
    private void loadFeedsByDistance() {
        feedList.clear();
        feedKeyList.clear();

        adapter.notifyDataSetChanged();

        /*Search by distance query which returns the users keys*/
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(ProfileActivity.currentSignedInUser.getLatitude(),
                ProfileActivity.currentSignedInUser.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                /*Adds the nearby feeds by "radius"*/
                for (int i = 0; i < feedListFull.size(); i++) {
                    if (feedListFull.get(i).getAuthor().equals(key) && !key.equals(ProfileActivity.currentSignedInUser.getUid())) {
                        feedList.add(feedListFull.get(i));
                        feedKeyList.add(feedKeyListFull.get(i));
                    }
                }
                sortByMostRecent(feedList, feedKeyList);
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

    /*Load feeds by most recent*/
    private void loadFeedsByMostRecent() {
        feedList.clear();
        feedKeyList.clear();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            for (int i = 0; i < feedListFull.size(); i++) {
                for (int j = 0; j < feedListFull.size(); j++) {
                    if (i != j) {
                        String fullDate_i = feedListFull.get(i).getDate() + " " + feedListFull.get(i).getTime();
                        String fullDate_j = feedListFull.get(j).getDate() + " " + feedListFull.get(j).getTime();
                        if (!sdf.parse(fullDate_i).before(sdf.parse(fullDate_j))) {
                            Collections.swap(feedListFull, i, j);
                            Collections.swap(feedKeyListFull, i, j);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            Log.d("tests", e.getMessage());
        }
        feedList.addAll(feedListFull);
        feedKeyList.addAll(feedKeyListFull);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    /*Load feeds by my friends*/
    private void loadFeedsOfMyFriends() {
        feedList.clear();
        feedKeyList.clear();

        for (int i = 0; i < feedListFull.size(); i++) {
            for (User user : friendsList) {
                if (feedListFull.get(i).getAuthor().equals(user.getUid())) {
                    feedList.add(feedListFull.get(i));
                    feedKeyList.add(feedKeyListFull.get(i));
                }
            }
        }

        sortByMostRecent(feedList, feedKeyList);
    }

    /*Load my feeds */
    private void loadFeedsByMyFeeds() {
        feedList.clear();
        feedKeyList.clear();

        feedList.addAll(myFeeds);
        feedKeyList.addAll(myFeedKeyList);

        sortByMostRecent(feedList, feedKeyList);
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                .setAction(actionName, action);
        snack.show();
    }

    /*Sorting the filtered results by most recent*/
    private void sortByMostRecent(List<Feed> feeds, List<String> keys) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            for (int i = 0; i < feeds.size(); i++) {
                for (int j = 0; j < feeds.size(); j++) {
                    if (i != j) {
                        String fullDate_i = feeds.get(i).getDate() + " " + feeds.get(i).getTime();
                        String fullDate_j = feeds.get(j).getDate() + " " + feeds.get(j).getTime();
                        if (!sdf.parse(fullDate_i).before(sdf.parse(fullDate_j))) {
                            Collections.swap(feeds, i, j);
                            Collections.swap(keys, i, j);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            Log.d("tests", e.getMessage());
        }

        adapter.notifyDataSetChanged();
    }

    /*Loads user profile fragment*/
    private void loadProfileFragment(int position) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        if (!ProfileActivity.isAGuest) {
            //Open my user profile
            if (feedList.get(position).getAuthor().equals(ProfileActivity.currentSignedInUser.getUid())) {
                ProfileActivity.bottomNavigationView.setSelectedItemId(R.id.action_nav_profile);
                transaction.replace(R.id.fragment_container, UserProfileFragment.newInstance()).addToBackStack(null).commit();
            }
            //Open other user profile
            else {
                ProfileActivity.bottomNavigationView.setSelectedItemId(R.id.uncheckedItem);
                transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                        .newInstance(UsersManager.getUserByKey(feedList.get(position).getAuthor()))).addToBackStack(null).commit();
            }
        }
        //Open other user profile
        else {
            ProfileActivity.bottomNavigationView.setSelectedItemId(R.id.uncheckedItem);
            transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                    .newInstance(UsersManager.getUserByKey(feedList.get(position).getAuthor()))).addToBackStack(null).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
