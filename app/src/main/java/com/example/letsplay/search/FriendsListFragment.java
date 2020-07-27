package com.example.letsplay.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

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
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.example.letsplay.profile.UserProfileFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends Fragment implements Serializable {

    private Context context;
    private FriendsAdapter adapter;
    private List<String> friendList = new ArrayList<>();

    public static final String TAG = "friend_list_fragment";

    private List<User> users = new ArrayList<>();
    private TextView noFriendsYetTv;
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    public FriendsListFragment() {
    }

    public static FriendsListFragment newInstance(String key) {
        FriendsListFragment friendsListFragment = new FriendsListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key",key);
        friendsListFragment.setArguments(bundle);
        return friendsListFragment;
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
        View rootView = inflater.inflate(R.layout.friend_list_fragment, container, false);

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.home_feeds_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        progressBar = rootView.findViewById(R.id.friend_list_progress_bar);

        RecyclerView recyclerView = rootView.findViewById(R.id.friends_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        noFriendsYetTv = rootView.findViewById(R.id.no_friends_yet_tv);

        //Search view
        SearchView chatSearchView = rootView.findViewById(R.id.friends_search_view);
        chatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        users.clear();
        /*Loads all user from firebase, and filter them to the current user's friends only by key inside "users"*/
        UsersManager.loadUsers();
        UsersManager.setListener(new UsersManager.LoadListener() {
            @Override
            public void onFinishLoad() {
                String key = getArguments().getString("key");
                User userToShowHisFriendList = UsersManager.getUserByKey(key);
                progressBar.setVisibility(View.GONE);
                /*Takes all user's friends as keys and create user lists in "users"*/
                if(userToShowHisFriendList.getFriends()!=null&&userToShowHisFriendList.getFriends().size()>0){
                    friendList = userToShowHisFriendList.getFriends();
                    for (User user : UsersManager.getUsers()) {
                        if (friendList.contains(user.getUid()))
                            users.add(user);
                    }
                }
                else {
                    //Show message that the user has no friends yet
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            noFriendsYetTv.setText(context.getResources().getString(R.string.label_no_friends_yet));
                        }
                    });
                }

                adapter.setFriendListFull(users);
                adapter.notifyDataSetChanged();
            }
        });


        adapter = new FriendsAdapter(users, context);
        adapter.setListener(new FriendsAdapter.FriendsListener() {
            @Override
            public void onFriendClick(int position, View v) {
                loadProfileFragment(position);
            }
        });

        recyclerView.setAdapter(adapter);

        return rootView;
    }

    /*Loads user profile fragment*/
    private void loadProfileFragment(int position){
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        //Open my user profile
        if(users.get(position).getUid().equals(ProfileActivity.currentSignedInUser.getUid())){
            transaction.replace(R.id.fragment_container, UserProfileFragment.newInstance()).addToBackStack(null).commit();
        }
        //Open other user profile
        else{
            transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                    .newInstance(users.get(position))).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
