package com.example.letsplay.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.letsplay.R;
import com.example.letsplay.application.MyApplication;
import com.example.letsplay.chats.ChatWindowFragment;
import com.example.letsplay.chats.ChatsFragment;
import com.example.letsplay.homefeeds.HomeFeedsFragment;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.Feed;
import com.example.letsplay.objects.Instrument;
import com.example.letsplay.objects.Message;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.example.letsplay.profile.UserProfileFragment;
import com.example.letsplay.search.SearchFragment;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements LocationListener {

    //Fragment tags for later identification and usage
    public static final String PROFILE_FRAGMENT_TAG = "profile_fragment",
            SEARCH_FRAGMENT_TAG = "search_fragment",
            HOME_FEEDS_FRAGMENT_TAG = "home_feeds_fragment",
            CHATS_FRAGMENT_TAG = "chats_fragment",
            OTHER_PROFILE_FRAGMENT = "other_profile_fragment";

    private SharedPreferences sp;

    //Latitude and longitude for current user location to pass for the fragments
    public static double latitude = 0.0, longitude = 0.0;

    //GeoFire for users' locations
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users-Locations");
    private GeoFire geoFire;

    // Firebase Functions
    private FirebaseFunctions mFunctions;


    //Firebase attributes to control the firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    //Creates or gets (if exists) reference to table "Users"
    private DatabaseReference users = database.getReference("Users");
    private FirebaseMessaging messaging = FirebaseMessaging.getInstance();

    private FirebaseMessaging messagingTest = FirebaseMessaging.getInstance();

    //The current logged in user
    public static User currentSignedInUser = null;
    public static BottomNavigationView bottomNavigationView;

    //Guests control
    public static boolean isAGuest = false;

    private ProgressBar progressBar;

    //Receiver for the message receive
    private BroadcastReceiver receiver;

    // Storage management
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    //for user's location updates
    private final int LOCATION_PERMISSION_REQUEST = 1;
    private LocationManager location;
    private Geocoder geocoder;

    private UserProfileFragment userProfileFragment = UserProfileFragment.newInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //The activity is alive
        MyApplication.activityCreated();

        // Set Status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top
        }

        sp = getSharedPreferences("is subscribed", MODE_PRIVATE);

        progressBar = findViewById(R.id.user_fragment_progress_bar);
        //Set the bottom navigation view and its listener
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Bottom navigation bar click listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_nav_feed:
                        if (bottomNavigationView.getSelectedItemId() == R.id.action_nav_feed)
                            break;
                        callDefaultFragment();
                        break;

                    case R.id.action_nav_profile:
                        if (bottomNavigationView.getSelectedItemId() == R.id.action_nav_profile)
                            break;
                        if (isAGuest) {
                            displaySnackbar(getResources().getString(R.string.label_guest_warning), "Sign Up", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(ProfileActivity.this, SignUpActivity.class));
                                    finish();
                                }
                            });
                            //bottomNavigationView.setSelectedItemId(R.id.action_nav_feed);
                            item.setCheckable(false);
                            return false;
                        }
                        bottomNavigationView.getMenu().findItem(R.id.action_nav_profile).setChecked(true);
                        callUserProfileFragment();
                        break;

                    case R.id.action_nav_search:
                        if (bottomNavigationView.getSelectedItemId() == R.id.action_nav_search)
                            break;
                        callSearchFragment();
                        break;

                    case R.id.action_nav_chats:
                        if (isAGuest) {
                            displaySnackbar(getResources().getString(R.string.label_guest_warning), "Sign Up", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(ProfileActivity.this, SignUpActivity.class));
                                    finish();
                                }
                            });
                            item.setCheckable(false);
                            return false;
                        }
                        if (bottomNavigationView.getSelectedItemId() == R.id.action_nav_chats)
                            break;
                        callChatFragment();
                        break;
                }
                return true;
            }
        });


        //Get the current logged in user
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Guests control
        isAGuest = getIntent().getBooleanExtra("is a guest", false);

        if (isAGuest) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.GONE);
            callDefaultFragment();
            return;
        }


        users.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
                //Get the current user object
                if (dataSnapshot.exists()) {
                    //Loads the user with the class loader
                    currentSignedInUser = dataSnapshot.getValue(User.class);

                    messaging.subscribeToTopic(currentSignedInUser.getUid());

                    //Updates the user's status - online
                    currentSignedInUser.setStatus(User.Status.ONLINE);
                    users.child(currentSignedInUser.getUid()).setValue(currentSignedInUser);


                    //Checks if the user clicked on the message notification and passes it to the chat window
                    if (getIntent().getBooleanExtra("open chat window", false)) {
                        final String uid = getIntent().getStringExtra("uid");
                        users.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (Chat chat : currentSignedInUser.getChats()) {
                                        if (chat.getOtherSideUserKey().equals(uid)) {
                                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container,
                                                    ChatWindowFragment.newInstance(chat, snapshot.getValue(User.class)))
                                                    .addToBackStack(null).commit();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


                    // Check if user came from sign up
                    if (ProfileActivity.this.getIntent().getBooleanExtra("fromSignUp", false)) {
                        /*Call the user profile fragment after sign up to complete details filling*/
                        callUserProfileFragment();
                        bottomNavigationView.setSelectedItemId(R.id.action_nav_profile);
                    } else
                        //Calls the default fragment after getting the user object, which is home feed
                        callDefaultFragment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getDetails());
            }
        });

        //Initialize the geo fire object
        geoFire = new GeoFire(ref);

        //Initialize the location related objects
        location = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this);
        requestLocationPermission();


        //Handle message receiving from a user
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if we are at the chats part, if we do - it will receive and handle the message itself
                if (bottomNavigationView.getSelectedItemId() == R.id.action_nav_chats)
                    return;
                //Receive the intent from the service with the message
                String message = intent.getStringExtra("message");
                String senderUserKey = intent.getStringExtra("name and from");
                String[] nameAndFrom = senderUserKey.split(" ");
                senderUserKey = nameAndFrom[1];

                Log.d("tests", "From: " + senderUserKey);
                Log.d("tests", "Message received to ProfileActivity: " + message);

                handleMessageReceiving(message, senderUserKey);
            }
        };

        //Register to the action from the service
        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.label_location_dialog_title))
                        .setMessage(getResources().getString(R.string.label_location_dialog_body))
                        .setPositiveButton(getResources().getString(R.string.label_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                        }).setNegativeButton(getResources().getString(R.string.label_quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
            }
    }

    /*Start of location work*/

    /*Request for location permission*/
    private void requestLocationPermission() {
        /*Request for location permission if not exist yet*/
        if (Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            } else {
                location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, this);
            }
        } else
            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, this);
    }

    /*Location listener methods*/
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                .setAction(actionName, action);
        snack.show();
    }


    /*Not in use*/
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /*Not in use*/
    @Override
    public void onProviderEnabled(String provider) {

    }

    /*Not in use*/
    @Override
    public void onProviderDisabled(String provider) {

    }

    /*End of location work*/


    /*Fragment calls*/

    /*Calls the default (home feeds) fragment after getting the user's object*/
    private void callDefaultFragment() {
        //Checks if the user clicked on the message notification and passes it to the other user window
        if (getIntent() != null &&
                getIntent().getBooleanExtra("addedFriendNotification", false)) {
            UsersManager.loadUsers();
            UsersManager.setListener(new UsersManager.LoadListener() {
                @Override
                public void onFinishLoad() {
                    final String otherUserUid = getIntent().getStringExtra("otherUserUid");
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    setIntent(null);
                    transaction.replace(R.id.fragment_container,
                            OtherUserProfileFragment.newInstance(UsersManager.getUserByKey(otherUserUid)))
                            .addToBackStack(null).commit();
                }
            });
        } else {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, HomeFeedsFragment.newInstance(), HOME_FEEDS_FRAGMENT_TAG).commit();
        }
    }

    /*Call the user profile fragment*/
    private void callUserProfileFragment() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, userProfileFragment, PROFILE_FRAGMENT_TAG).commit();
    }

    /*Call the chats fragment*/
    private void callChatFragment() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, ChatsFragment.newInstance(), CHATS_FRAGMENT_TAG).commit();
    }

    /*Call the search fragment*/
    private void callSearchFragment() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, SearchFragment.newInstance(),
                SEARCH_FRAGMENT_TAG).commit();
    }

    /*Handle message receiving*/
    private void handleMessageReceiving(String message, String senderUserKey) {
        //Check if the chat window exists
        for (Chat chat : currentSignedInUser.getChats()) {
            if (chat.getOtherSideUserKey().equals(senderUserKey)) {
                //If there are no messages yet
                if (chat.getMessages() == null)
                    chat.setMessages(new ArrayList<Message>());
                chat.getMessages().add(new Message(senderUserKey, message));
                chat.setLastMessageSeen(false);
                /* Collections.swap(currentSignedInUser.getChats(),currentSignedInUser.getChats().indexOf(chat),0);*/
                currentSignedInUser.getChats().remove(chat);
                currentSignedInUser.getChats().add(0, chat);
                //Updates the user in firebase
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(ProfileActivity.currentSignedInUser.getUid()).child("chats")
                        .setValue(ProfileActivity.currentSignedInUser.getChats());
                return;
            }
        }

        //The chat window doesn't exists

        //Check if the user has chat list already, if he doesn't - create a new list
        if (currentSignedInUser.getChats() == null)
            currentSignedInUser.setChats(new ArrayList<Chat>());
        //Create a new chat window
        Chat chat = new Chat(currentSignedInUser.getUid(), senderUserKey);
        chat.setMessages(new ArrayList<Message>());
        chat.getMessages().add(new Message(senderUserKey, message));
        chat.setLastMessageSeen(false);
        currentSignedInUser.getChats().add(0, chat);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).child("chats")
                .setValue(ProfileActivity.currentSignedInUser.getChats());
    }


    /*Add listener at onStart()*/
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth != null && authStateListener != null)
            firebaseAuth.addAuthStateListener(authStateListener);
        //Updates the user's status - online at onStart()
        if (users != null && currentSignedInUser != null) {
            users.child(currentSignedInUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentSignedInUser = snapshot.getValue(User.class);
                        currentSignedInUser.setStatus(User.Status.ONLINE);
                        users.child(currentSignedInUser.getUid()).setValue(currentSignedInUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        //The activity is alive
        MyApplication.activityCreated();
        Log.d("tests", "activity is alive- on start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("tests", "activity is alive- on resume");
    }

    /*Remove listener at onStop()*/
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null && authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
        /*Updates the user's status - offline at onStop()*/
        if (users != null && currentSignedInUser != null) {
            currentSignedInUser.setStatus(User.Status.OFFLINE);
            users.child(currentSignedInUser.getUid()).setValue(currentSignedInUser);
        }

        //The activity is not alive
        MyApplication.activityDestroyed();
        Log.d("tests", "activity is not alive - on stop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("tests", "activity is not alive - on pause");
    }

    /*Updates the user's status - online/offline at onDestroy()*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (users != null && currentSignedInUser != null) {
            users.child(currentSignedInUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentSignedInUser = snapshot.getValue(User.class);
                        currentSignedInUser.setStatus(User.Status.OFFLINE);
                        users.child(currentSignedInUser.getUid()).setValue(currentSignedInUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //The activity is destroyed
        MyApplication.activityDestroyed();
        Log.d("tests", "activity is not alive - on destroy");

        //Unregister to the action from the service
        if (receiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

}
