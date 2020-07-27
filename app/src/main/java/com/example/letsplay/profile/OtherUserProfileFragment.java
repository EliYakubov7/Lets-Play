package com.example.letsplay.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.activities.SignUpActivity;
import com.example.letsplay.chats.ChatWindowFragment;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.User;
import com.example.letsplay.search.FriendsListFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfileFragment extends Fragment implements Serializable {

    private Context context;
    private CircleImageView profilePhoto;
    private ImageView backgroundPhoto;
    private boolean isProfile;
    private User otherUser;
    private Handler handler = new Handler();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public static OtherUserProfileFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("User", user);
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        fragment.setArguments(args);
        return fragment;
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
        otherUser = (User) getArguments().getSerializable("User");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile_fragment, container, false);

        // Hide edit profile Btn
        Button editProfileBtn = rootView.findViewById(R.id.edit_profile_btn);
        editProfileBtn.setVisibility(View.GONE);

        // Appbar always expanded
        AppBarLayout appBarLayout = rootView.findViewById(R.id.app_bar_profile);
        appBarLayout.setExpanded(true);

        // uncheck bottom navi
        final ProgressBar progressBar = rootView.findViewById(R.id.user_fragment_progress_bar);
        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.uncheckedItem).setChecked(true);

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.profile_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        //Define the Collapsing Toolbar and user name
        CollapsingToolbarLayout collapsing = rootView.findViewById(R.id.profile_collapsing_layout);
        collapsing.setTitle(otherUser.getFirstName() + " " + otherUser.getLastName());

        // Set user basic info
        TextView basicInfoTv = rootView.findViewById(R.id.profile_basic_info_tv);
        if (otherUser.getAge() >= 0 && otherUser.getGender() != null && otherUser.getLocation() != null) {
            basicInfoTv.setText(otherUser.getAge() + ", " +
                    ((otherUser.getGender().equals("Male")||otherUser.getGender().equals("זכר"))?
                    getContext().getResources().getString(R.string.gender_male) :
                            getContext().getResources().getString(R.string.gender_female))
                    + "\n" + otherUser.getLocation());
        }

        //Set user biography
        TextView aboutMeTv = rootView.findViewById(R.id.profile_biography_tv);
        if (otherUser.getSelfDescription() != null) {
            aboutMeTv.setText(otherUser.getSelfDescription());
        }

        //Set user instruments
        TextView mainInstrumentTv = rootView.findViewById(R.id.profile_main_instrument_tv);
        TextView secondaryInstrumentTv = rootView.findViewById(R.id.profile_secondary_instrument_tv);
        if (otherUser.getMainInstrument() != null) {
            // if instrument is none, don't display skill
            if (otherUser.getMainInstrument().getInstrumentResId() == 2131820680) { // equal to none instrumentInt
                mainInstrumentTv.setText(context.getResources().getString(otherUser.getMainInstrument()
                        .getInstrumentResId()));
            } else { //display instrument with skill
                mainInstrumentTv.setText(context.getResources().getString(otherUser.getMainInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(otherUser.getMainInstrument().getSkillResId())));
            }
        }
        if (otherUser.getSecondaryInstrument() != null) {

            // if instrument is none, don't display skill
            if (otherUser.getSecondaryInstrument().getInstrumentResId() == 2131820680) {
                secondaryInstrumentTv.setText(context.getResources().getString(otherUser.getSecondaryInstrument().getInstrumentResId()));
            } else { //display instrument with skill
                secondaryInstrumentTv.setText(context.getResources().getString(otherUser.getSecondaryInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(otherUser.getSecondaryInstrument().getSkillResId())));
            }
        }
        //Set user genres
        TextView genresTv = rootView.findViewById(R.id.profile_genres_tv);
        if (otherUser.getMusicGenres() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < otherUser.getMusicGenres().size(); i++) {
                if (i < otherUser.getMusicGenres().size() - 1)
                    stringBuilder.append(context.getResources().getString(otherUser.getMusicGenres().get(i))).append(", ");
                else
                    stringBuilder.append(context.getResources().getString(otherUser.getMusicGenres().get(i)));
            }
            genresTv.setText(stringBuilder.toString());
        }

        //Set user Favorite bands
        TextView favoriteBandsTv = rootView.findViewById(R.id.favorite_artist_tv);
        if (otherUser.getArtists() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < otherUser.getArtists().size(); i++) {
                if (i < otherUser.getArtists().size() - 1)
                    stringBuilder.append(otherUser.getArtists().get(i)).append(", ");
                else
                    stringBuilder.append(otherUser.getArtists().get(i));
            }
            favoriteBandsTv.setText(stringBuilder.toString());
        }


        // Profile Circle Image
        profilePhoto = rootView.findViewById(R.id.profile_circle_image_view);
        //Upload profile photo from storage
        if (otherUser.getProfileImageURL() != null) {
            StorageReference photoRef = storageRef.child(otherUser.getProfileImageURL());

            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(profilePhoto);
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else
            progressBar.setVisibility(View.GONE);

        // Profile background
        backgroundPhoto = rootView.findViewById(R.id.profile_background_iv);
        //Upload background photo from storage
        if (otherUser.getBackgroundImageURL() != null) {
            StorageReference photoRef = storageRef.child(otherUser.getBackgroundImageURL());
            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(backgroundPhoto);
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else
            progressBar.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.other_profile_top_toolbar_menu, menu);

        if(!ProfileActivity.isAGuest) {
            /*Check whether this user is my friend or not and change the menu item if that's true*/
            if (ProfileActivity.currentSignedInUser.getFriends() != null) {
                if (ProfileActivity.currentSignedInUser.getFriends().contains(otherUser.getUid())) {
                    menu.getItem(0).setIcon(R.drawable.ic_baseline_person_remove_24);
                }
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Add/Remove friend
            case R.id.action_add_remove_friend:

                if(ProfileActivity.isAGuest){
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    break;
                }

                if(ProfileActivity.currentSignedInUser.getFriends().contains(otherUser.getUid()))
                    removeFriendFromFriendList(item);
                else
                    addFriendToFriendList(item);
                break;
            // Friends List
            case R.id.other_profile_action_friends_list:

                if(ProfileActivity.isAGuest){
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    break;
                }

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container,
                        FriendsListFragment.newInstance(otherUser.getUid()),FriendsListFragment.TAG)
                        .addToBackStack(null).commit();
                break;

            // Send Message
            case R.id.action_send_message:

                if(ProfileActivity.isAGuest){
                    displaySnackbar(getResources().getString(R.string.label_guest_warning), getString(R.string.sign_up), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), SignUpActivity.class));
                            getActivity().finish();
                        }
                    });
                    break;
                }

                openChatWindow();
                break;

            case android.R.id.home:
                getActivity().onBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /*Add friend to friend list*/
    private void addFriendToFriendList(final MenuItem item){
        if(ProfileActivity.currentSignedInUser.getFriends()==null)
            ProfileActivity.currentSignedInUser.setFriends(new ArrayList<String>());
        //Update my friend list in my user object and in firebase
        ProfileActivity.currentSignedInUser.getFriends().add(otherUser.getUid());
        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,
                                            otherUser.getFirstName().concat(" "+context.getResources().
                                                    getString(R.string.label_added_successfully)), Toast.LENGTH_SHORT).show();
                                    item.setIcon(R.drawable.ic_baseline_person_remove_24);
                                }
                                else {
                                    Toast.makeText(context, context.getResources().getString(R.string.label_addition_faild),
                                            Toast.LENGTH_SHORT).show();
                                    ProfileActivity.currentSignedInUser.getFriends().remove(otherUser.getUid());
                                }
                            }
                        });
                    }
                });

    }

    /*Remove friend from friend list*/
    private void removeFriendFromFriendList(final MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.label_warning))
                .setMessage(context.getResources().getString(R.string.label_removal_question_part1)
                        .concat(" "+otherUser.getFirstName())
                        .concat(" "+context.getResources().getString(R.string.label_removal_question_part2)))
                .setPositiveButton(context.getResources().getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFriend(item);
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.label_no), null)
                .show();
    }

    private void removeFriend(final MenuItem item){
        //Update my friend list in my user object and in firebase
        ProfileActivity.currentSignedInUser.getFriends().remove(otherUser.getUid());
        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,
                                            otherUser.getFirstName().concat(" "+context.getResources().
                                                    getString(R.string.label_removed_successfully)), Toast.LENGTH_SHORT).show();
                                    item.setIcon(R.drawable.ic_baseline_person_add_24);
                                }
                                else {
                                    Toast.makeText(context, context.getResources().getString(R.string.label_removal_failed),
                                            Toast.LENGTH_SHORT).show();
                                    ProfileActivity.currentSignedInUser.getFriends().add(otherUser.getUid());
                                }
                            }
                        });
                    }
                });

    }

    /*Opens the chat window fragment with "otherUser"*/
    private void openChatWindow(){
        //Check if the user has a chat list, if he doesn't - initializes a new one
        if(ProfileActivity.currentSignedInUser.getChats()==null)
            ProfileActivity.currentSignedInUser.setChats(new ArrayList<Chat>());
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        //Check if the user has a chat window with that "otherUser"
        for(Chat chat:ProfileActivity.currentSignedInUser.getChats()){
            if(chat.getOtherSideUserKey().equals(otherUser.getUid())){
                transaction.replace(R.id.fragment_container, ChatWindowFragment.newInstance(chat,otherUser))
                        .addToBackStack(null).commit();
                return;
            }
        }

       //Initialize new window chat with "otherUser" and update the firebase
        Chat chat = new Chat(ProfileActivity.currentSignedInUser.getUid(),otherUser.getUid());
        ProfileActivity.currentSignedInUser.getChats().add(chat);
        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid())
                .child("chats").setValue(ProfileActivity.currentSignedInUser.getChats());
        transaction.replace(R.id.fragment_container, ChatWindowFragment.newInstance
                (ProfileActivity.currentSignedInUser.getChats().get(ProfileActivity.currentSignedInUser.getChats().size()-1),otherUser))
                .addToBackStack(null).commit();
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT)
                .setAction(actionName, action);
        snack.show();
    }
}