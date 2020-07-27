package com.example.letsplay.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.search.FriendsListFragment;
import com.example.letsplay.settings.LocationDialogFragment;
import com.example.letsplay.settings.SettingsActivity;
import com.example.letsplay.settings.UserPreferenceFragment;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment implements Serializable {

    private Context context;
    private CircleImageView profilePhoto;
    private ImageView backgroundPhoto;
    private boolean isProfile;
    private ProgressBar progressBar;
    private Button editProfileBtn;
    private boolean isFirstEnter = true;
    private CollapsingToolbarLayout collapsing;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public static UserProfileFragment newInstance() {
        Bundle args = new Bundle();
        UserProfileFragment fragment = new UserProfileFragment();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set user basic info
        TextView basicInfoTv = getView().findViewById(R.id.profile_basic_info_tv);
        if (ProfileActivity.currentSignedInUser.getAge() > 0 && ProfileActivity.currentSignedInUser.getGender() != null && ProfileActivity.currentSignedInUser.getLocation() != null) {
            basicInfoTv.setText(ProfileActivity.currentSignedInUser.getAge() + ", " +
                    ((ProfileActivity.currentSignedInUser.getGender().equals("Male") || ProfileActivity.currentSignedInUser.getGender().equals("זכר")) ?
                            getContext().getResources().getString(R.string.gender_male) :
                            getContext().getResources().getString(R.string.gender_female))
                    + "\n" + ProfileActivity.currentSignedInUser.getLocation());
        }

        //Set user biography
        TextView aboutMeTv = getView().findViewById(R.id.profile_biography_tv);
        if (ProfileActivity.currentSignedInUser.getSelfDescription() != null) {
            aboutMeTv.setText(ProfileActivity.currentSignedInUser.getSelfDescription());
        }

        //Set user instruments
        TextView mainInstrumentTv = getView().findViewById(R.id.profile_main_instrument_tv);
        TextView secondaryInstrumentTv = getView().findViewById(R.id.profile_secondary_instrument_tv);
        if (ProfileActivity.currentSignedInUser.getMainInstrument() != null) {
            // if instrument is none, don't display skill
            if (ProfileActivity.currentSignedInUser.getMainInstrument().getInstrumentResId() == 2131820680) { // equal to none instrumentInt
                mainInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument()
                        .getInstrumentResId()));
            } else { //display instrument with skill
                mainInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument().getSkillResId())));
            }
        }

        if (ProfileActivity.currentSignedInUser.getSecondaryInstrument() != null) {
            // if instrument is none, don't display skill
            if (ProfileActivity.currentSignedInUser.getSecondaryInstrument().getInstrumentResId() == 2131820680) {
                secondaryInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument().getInstrumentResId()));
            } else { //display instrument with skill
                secondaryInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument().getSkillResId())));
            }
        }

        //Set user genres
        TextView genresTv = getView().findViewById(R.id.profile_genres_tv);
        if (ProfileActivity.currentSignedInUser.getMusicGenres() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < ProfileActivity.currentSignedInUser.getMusicGenres().size(); i++) {
                if (i < ProfileActivity.currentSignedInUser.getMusicGenres().size() - 1)
                    stringBuilder.append(context.getResources().getString(ProfileActivity.currentSignedInUser.getMusicGenres().get(i))).append(", ");
                else
                    stringBuilder.append(context.getResources().getString(ProfileActivity.currentSignedInUser.getMusicGenres().get(i)));
            }
            genresTv.setText(stringBuilder.toString());
        }

        //Set user Favorite bands
        TextView favoriteBandsTv = getView().findViewById(R.id.favorite_artist_tv);
        if (ProfileActivity.currentSignedInUser.getArtists() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < ProfileActivity.currentSignedInUser.getArtists().size(); i++) {
                if (i < ProfileActivity.currentSignedInUser.getArtists().size() - 1)
                    stringBuilder.append(ProfileActivity.currentSignedInUser.getArtists().get(i)).append(", ");
                else
                    stringBuilder.append(ProfileActivity.currentSignedInUser.getArtists().get(i));
            }
            favoriteBandsTv.setText(stringBuilder.toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile_fragment, container, false);
        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.action_nav_profile).setChecked(true);


        // Add token of the user to db
        if (isFirstEnter) {
            new Thread() {
                @Override
                public void run() {
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersTokens/" + ProfileActivity.currentSignedInUser.getUid());
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            reference.child("token").setValue(instanceIdResult.getToken());
                            Log.d("testyLogin", reference.child("token/result/token").getDatabase().toString());
                            ProfileActivity.currentSignedInUser.setTokenRegistered(true);
                            FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                        }
                    });
                }
            }.start();
            isFirstEnter = false;
        }

        progressBar = rootView.findViewById(R.id.user_fragment_progress_bar);

        // Appbar always expanded
        AppBarLayout appBarLayout = rootView.findViewById(R.id.app_bar_profile);
        appBarLayout.setExpanded(true);

        // Edit Profile Btn
        editProfileBtn = rootView.findViewById(R.id.edit_profile_btn);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SettingsActivity.class));
            }
        });

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.profile_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        //Define the Collapsing Toolbar and user name
        collapsing = rootView.findViewById(R.id.profile_collapsing_layout);
        collapsing.setTitle(ProfileActivity.currentSignedInUser.getFirstName() + " " + ProfileActivity.currentSignedInUser.getLastName());

        // Set user basic info
        TextView basicInfoTv = rootView.findViewById(R.id.profile_basic_info_tv);
        if (ProfileActivity.currentSignedInUser.getAge() > 0 && ProfileActivity.currentSignedInUser.getGender() != null && ProfileActivity.currentSignedInUser.getLocation() != null) {
            basicInfoTv.setText(ProfileActivity.currentSignedInUser.getAge() + ", " + ProfileActivity.currentSignedInUser.getGender() + "\n" + ProfileActivity.currentSignedInUser.getLocation());
        }

        //Set user biography
        TextView aboutMeTv = rootView.findViewById(R.id.profile_biography_tv);
        if (ProfileActivity.currentSignedInUser.getSelfDescription() != null) {
            aboutMeTv.setText(ProfileActivity.currentSignedInUser.getSelfDescription());
        }

        //Set user instruments
        TextView mainInstrumentTv = rootView.findViewById(R.id.profile_main_instrument_tv);
        TextView secondaryInstrumentTv = rootView.findViewById(R.id.profile_secondary_instrument_tv);
        if (ProfileActivity.currentSignedInUser.getMainInstrument() != null) {
            // if instrument is none, don't display skill
            if (ProfileActivity.currentSignedInUser.getMainInstrument().getInstrumentResId() == 2131820680) { // equal to none instrumentInt
                mainInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument()
                        .getInstrumentResId()));
            } else { //display instrument with skill
                mainInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(ProfileActivity.currentSignedInUser.getMainInstrument().getSkillResId())));
            }
        }

        if (ProfileActivity.currentSignedInUser.getSecondaryInstrument() != null) {
            // if instrument is none, don't display skill
            if (ProfileActivity.currentSignedInUser.getMainInstrument().getInstrumentResId() == 2131820680) {
                secondaryInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument().getInstrumentResId()));
            } else { //display instrument with skill
                secondaryInstrumentTv.setText(context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument()
                        .getInstrumentResId()).concat(", " + context.getResources().getString(ProfileActivity.currentSignedInUser.getSecondaryInstrument().getSkillResId())));
            }
        }

        //Set user genres
        TextView genresTv = rootView.findViewById(R.id.profile_genres_tv);
        if (ProfileActivity.currentSignedInUser.getMusicGenres() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < ProfileActivity.currentSignedInUser.getMusicGenres().size(); i++) {
                if (i < ProfileActivity.currentSignedInUser.getMusicGenres().size() - 1)
                    stringBuilder.append(context.getResources().getString(ProfileActivity.currentSignedInUser.getMusicGenres().get(i))).append(", ");
                else
                    stringBuilder.append(context.getResources().getString(ProfileActivity.currentSignedInUser.getMusicGenres().get(i)));
            }
            genresTv.setText(stringBuilder.toString());
        }

        //Set user Favorite bands
        TextView favoriteBandsTv = rootView.findViewById(R.id.favorite_artist_tv);
        if (ProfileActivity.currentSignedInUser.getArtists() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < ProfileActivity.currentSignedInUser.getArtists().size(); i++) {
                if (i < ProfileActivity.currentSignedInUser.getArtists().size() - 1)
                    stringBuilder.append(ProfileActivity.currentSignedInUser.getArtists().get(i)).append(", ");
                else
                    stringBuilder.append(ProfileActivity.currentSignedInUser.getArtists().get(i));
            }
            favoriteBandsTv.setText(stringBuilder.toString());
        }


        // Profile Circle Image
        profilePhoto = rootView.findViewById(R.id.profile_circle_image_view);
        //Upload profile photo from storage
        if (ProfileActivity.currentSignedInUser.getProfileImageURL() != null) {
            StorageReference photoRef = storageRef.child(ProfileActivity.currentSignedInUser.getProfileImageURL());

            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(profilePhoto);
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else
            progressBar.setVisibility(View.GONE);

        // Add\Change photo listener
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfile = true;
                ImagePicker.Companion.with(UserProfileFragment.this)
                        .cropSquare()
                        .compress(512)
                        .maxResultSize(512, 512)
                        .start();
            }
        });

        // Profile background
        backgroundPhoto = rootView.findViewById(R.id.profile_background_iv);
        //Upload background photo from storage
        if (ProfileActivity.currentSignedInUser.getBackgroundImageURL() != null) {
            StorageReference photoRef = storageRef.child(ProfileActivity.currentSignedInUser.getBackgroundImageURL());
            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(backgroundPhoto);
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else
            progressBar.setVisibility(View.GONE);

        // Add\Change photo listener
        backgroundPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfile = false;
                ImagePicker.Companion.with(UserProfileFragment.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1024, 1024)
                        .start();
            }
        });

        if(ProfileActivity.currentSignedInUser.isFirstTime()) {
            welcomeShowCase();
            ProfileActivity.currentSignedInUser.setFirstTime(false);
            FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
        }

        return rootView;
    }

    private void welcomeShowCase() {
        BottomNavigationView menu = ProfileActivity.bottomNavigationView;

        FancyShowCaseView fancy1 = new FancyShowCaseView.Builder(getActivity())
                .title(getString(R.string.welcome_to_letsplay))
                .titleStyle(R.style.MyShowCaseTitle, Gravity.CENTER)
                .titleGravity(Gravity.CENTER)
                .build();

        FancyShowCaseView fancy2 = new FancyShowCaseView.Builder(getActivity())
                .focusOn(editProfileBtn)
                .title(getString(R.string.welcome_lets_play_edit))
                .titleStyle(R.style.MyShowCaseTitle, Gravity.CENTER)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleSize(40, TypedValue.COMPLEX_UNIT_SP)
                .titleGravity(Gravity.CENTER)
                .build();

        FancyShowCaseView fancy3 = new FancyShowCaseView.Builder(getActivity())
                .focusOn(menu)
                .title(getString(R.string.welcome_discover))
                .titleStyle(R.style.MyShowCaseTitle, Gravity.CENTER)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .titleSize(40, TypedValue.COMPLEX_UNIT_SP)
                .titleGravity(Gravity.CENTER)
                .build();


        FancyShowCaseQueue tutorialQueue = new FancyShowCaseQueue()
                .add(fancy1).add(fancy3).add(fancy2);

        tutorialQueue.show();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_top_toolbar_menu, menu);

//        for(int i = 0; i < menu.size(); i++){
//            Drawable drawable = menu.getItem(i).getIcon();
//            menu.getItem(i).setti
//            if(drawable != null) {
//                drawable.mutate();
//                drawable.item(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
//            }
//        }
        super.onCreateOptionsMenu(menu, inflater);
        // User Profile menu

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Settings
            case R.id.action_setting:
                startActivity(new Intent(context, SettingsActivity.class));
                break;

            // Friends List
            case R.id.action_friends_list:
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container,
                        FriendsListFragment.newInstance(ProfileActivity.currentSignedInUser.getUid()), FriendsListFragment.TAG)
                        .addToBackStack(null).commit();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // File Uri on phone
            final Uri fileUri = data.getData();

            if (isProfile) { //If the result comes from the profile photo
                // Get storage ref and save user photo to the storage under path
                progressBar.setVisibility(View.VISIBLE);

                final StorageReference photoImageRef = storageRef.child("images/" + ProfileActivity.currentSignedInUser.getUid() + "/profile/profilePhoto.jpg");
                profilePhoto.setImageResource(android.R.color.transparent);
                // Start upload task
                UploadTask uploadTask = photoImageRef.putFile(fileUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Set user profile photo URI
                        ProfileActivity.currentSignedInUser.setProfileImageURL(photoImageRef.getPath());
                        profilePhoto.setImageURI(fileUri);
                        progressBar.setVisibility(View.GONE);
                        Log.d("upload", photoImageRef.getPath());
                        //Updates the user in user's table
                        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Uplaod failed, Please try again", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                    }
                });

            } else { // If the result comes from the background photo
                // Get storage ref and save user photo to the storage under path
                progressBar.setVisibility(View.VISIBLE);

                final StorageReference photoImageRef = storageRef.child("images/" + ProfileActivity.currentSignedInUser.getUid() + "/profile/backgroundPhoto.jpg");
                backgroundPhoto.setImageResource(android.R.color.transparent);
                // Start upload task
                UploadTask uploadTask = photoImageRef.putFile(fileUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Set user profile photo URI
                        ProfileActivity.currentSignedInUser.setBackgroundImageURL(photoImageRef.getPath());
                        backgroundPhoto.setImageURI(fileUri);
                        progressBar.setVisibility(View.GONE);
                        Log.d("upload", taskSnapshot.getMetadata().toString());
                        //Updates the user in user's table
                        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Uplaod failed, Please try again", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

}
