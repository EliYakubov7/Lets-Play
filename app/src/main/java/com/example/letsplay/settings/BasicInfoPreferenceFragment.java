package com.example.letsplay.settings;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.homefeeds.FeedFilterFragment;
import com.example.letsplay.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BasicInfoPreferenceFragment extends PreferenceFragmentCompat {

    private LocalDate birthday;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_basic_info, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        EditTextPreference editTextPreference;

        switch (preference.getKey()) {

            case "first_name_preference":
                editTextPreference = preference.getPreferenceManager().findPreference("first_name_preference");
                editTextPreference.setText(ProfileActivity.currentSignedInUser.getFirstName());
                editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ProfileActivity.currentSignedInUser.setFirstName((String) newValue);
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ProfileActivity.currentSignedInUser);
                        return false;
                    }
                });

                break;

            case "last_name_preference":
                editTextPreference = preference.getPreferenceManager().findPreference("last_name_preference");
                editTextPreference.setText(ProfileActivity.currentSignedInUser.getLastName());
                editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ProfileActivity.currentSignedInUser.setLastName((String) newValue);
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ProfileActivity.currentSignedInUser);
                        return false;
                    }
                });
                break;

            case "email_preference":
                editTextPreference = preference.getPreferenceManager().findPreference("email_preference");
                editTextPreference.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail((String) newValue);
                        return false;
                    }
                });
                break;

            case "address_preference":
                LocationDialogFragment.newInstance()
                        .show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "LocationDialogFragment");
                break;

            case "date_of_birth_preference":
                final Calendar myCalendar = Calendar.getInstance();
                // get current user dob to display on date picker
                DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yy");
                DateTime dt = formatter.parseDateTime(ProfileActivity.currentSignedInUser.getDateOfBirth());

                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String myFormat = "dd/MM/yy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                        birthday = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                        Log.d("testy", birthday.toString());


                        ProfileActivity.currentSignedInUser.setDateOfBirth(sdf.format(myCalendar.getTime()));

                        Log.d("testy", ProfileActivity.currentSignedInUser.getDateOfBirth());

                        ProfileActivity.currentSignedInUser.setAge(Years.yearsBetween(birthday, LocalDate.now()).getYears());
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ProfileActivity.currentSignedInUser);
                    }

                };

                new DatePickerDialog(getContext(), R.style.MySpinnerDatePickerStyle, date, dt.getYear(), dt.getMonthOfYear(),
                        dt.getDayOfMonth()).show();

                break;

        }

        return false;
    }


}
