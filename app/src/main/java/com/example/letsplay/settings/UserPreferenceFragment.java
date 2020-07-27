package com.example.letsplay.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


import com.example.letsplay.R;
import com.example.letsplay.activities.LoginActivity;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.Instrument;
import com.example.letsplay.objects.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserPreferenceFragment extends PreferenceFragmentCompat {

    private SharedPreferences sp;

    public static UserPreferenceFragment newInstance() {
        Bundle args = new Bundle();
        UserPreferenceFragment fragment = new UserPreferenceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        Button cancelBtn;
        Button updateBtn;

        switch (preference.getKey()) {

            case "biography_preference":
                final View dialogViewBio = inflater.inflate(R.layout.about_me_preference_dialog, null);
                final EditText aboutMeEt = dialogViewBio.findViewById(R.id.about_me_dialog_et);
                if (ProfileActivity.currentSignedInUser.getSelfDescription() != null)
                    aboutMeEt.setText(ProfileActivity.currentSignedInUser.getSelfDescription());

                dialogBuilder.setView(dialogViewBio).setTitle(getContext().getResources().getString(R.string.label_about_me))
                        .setPositiveButton(getContext().getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProfileActivity.currentSignedInUser.setSelfDescription(aboutMeEt.getText().toString());
                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ProfileActivity.currentSignedInUser);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getContext().getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;

            case "instrument_preference":
                final View dialogViewInstru = inflater.inflate(R.layout.instrument_preference_dialog, null);
                final Spinner typeInsSpinner = dialogViewInstru.findViewById(R.id.instrument_type_spinner_spinner_pref_dialog);
                final Spinner instrumentSpinner = dialogViewInstru.findViewById(R.id.instruments_spinner_pref_dialog);
                final Spinner skillSpinner = dialogViewInstru.findViewById(R.id.skills_spinner_pref_dialog);
                final int[] instrumentArr = getContext().getResources().getIntArray(R.array.instrumentsInts);
                final int[] skillsArr = getContext().getResources().getIntArray(R.array.skillsInts);


                dialogBuilder.setView(dialogViewInstru).setTitle(getContext().getResources().getString(R.string.label_choose_instrument))
                        .setPositiveButton(getContext().getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Create instrument instance if null
                                if (ProfileActivity.currentSignedInUser.getMainInstrument() == null) {
                                    ProfileActivity.currentSignedInUser.setMainInstrument(new Instrument(2131820680, 2131820838));
                                }
                                if (ProfileActivity.currentSignedInUser.getSecondaryInstrument() == null) {
                                    ProfileActivity.currentSignedInUser.setSecondaryInstrument(new Instrument(2131820680, 2131820838));
                                }

                                if (typeInsSpinner.getSelectedItemPosition() == 0) {
                                    ProfileActivity.currentSignedInUser.getMainInstrument()
                                            .setInstrumentResId(instrumentArr[instrumentSpinner.getSelectedItemPosition()]);

                                    ProfileActivity.currentSignedInUser.getMainInstrument()
                                            .setSkillResId(skillsArr[skillSpinner.getSelectedItemPosition()]);

                                } else {
                                    ProfileActivity.currentSignedInUser.getSecondaryInstrument()
                                            .setInstrumentResId(instrumentArr[instrumentSpinner.getSelectedItemPosition()]);

                                    ProfileActivity.currentSignedInUser.getSecondaryInstrument()
                                            .setSkillResId(skillsArr[skillSpinner.getSelectedItemPosition()]);
                                }
                                FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);

                                dialog.dismiss();
                            }
                        }).setNegativeButton(getContext().getResources().getString(R.string.label_cancel), null);
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                break;

            case "genres_preference":
                FragmentTransaction transactionGen = getParentFragmentManager().beginTransaction();
                transactionGen.replace(R.id.preferences_fragment_container, GenresFragment.newInstance()).addToBackStack(null).commit();
                break;

            case "favorite_artist_preference":
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.preferences_fragment_container, ArtistsFragment.newInstance()).addToBackStack(null).commit();
                break;

            case "logout_preference":
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getContext().getResources().getString(R.string.label_prefs_logout))
                        .setMessage(getContext().getResources().getString(R.string.label_are_you_sure_logout))
                        .setPositiveButton(getResources().getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).child("status").setValue(User.Status.OFFLINE);
                                        FirebaseAuth.getInstance().signOut();
                                        ProfileActivity.currentSignedInUser = null;
                                        Intent signoutIntentLogout = new Intent(getContext(), LoginActivity.class);
                                        signoutIntentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(signoutIntentLogout);
                                    }
                                }
                        ).setNegativeButton(getResources().getString(R.string.label_no), null).show();
                break;

            case "remove_preference":
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                builder2.setTitle(getContext().getResources().getString(R.string.label_delete_account))
                        .setMessage(getContext().getResources().getString(R.string.label_delete_Account_warning))
                        .setPositiveButton(getResources().getString(R.string.label_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ProfileActivity.currentSignedInUser=null;
                                                Intent signoutIntentRemove = new Intent(getContext(), LoginActivity.class);
                                                signoutIntentRemove.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(signoutIntentRemove);
                                            }
                                        });

                                    }
                                }
                        ).setNegativeButton(getResources().getString(R.string.label_no), null).show();
                break;
        }


        return super.onPreferenceTreeClick(preference);
    }

}
