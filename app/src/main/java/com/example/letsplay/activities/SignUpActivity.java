package com.example.letsplay.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;


import com.example.letsplay.R;
import com.example.letsplay.location.AutocompleteLocationAdapter;
import com.example.letsplay.objects.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class SignUpActivity extends AppCompatActivity implements LocationListener {

    private final int LOCATION_PERMISSION_REQUEST = 1;
    private LocationManager location;
    private Geocoder geocoder;
    private double latitude = 0.0, longitude = 0.0;
    private LocalDate birthdate;

    private EditText emailEt, passwordEt, firstNameEt, lastNameEt, dobEt;
    private TextInputLayout passwordTil, emailTil, firstNameTil, lastNameTil, locationTil, dobTil;
    private ProgressBar locationProgressBar;
    private Button signUpBtn;
    private RadioGroup genderRg;

    private FirebaseAuth auth;

    private static ProgressDialog progressDialog;
    private TextView strengthOfPassword;

    private AutoCompleteTextView autoCompleteLocation;
    private AutocompleteLocationAdapter locationAdapter;

    private ImageButton getLocationBtn;

    private Handler handler = new Handler();


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Set Statusbar color
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary)); //status bar or the time bar at the top
        }

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        signUpBtn = findViewById(R.id.sign_up_button);
        emailEt = findViewById(R.id.email_signin_et);
        emailTil = findViewById(R.id.email_til);
        passwordEt = findViewById(R.id.password_signin_et);
        passwordTil = findViewById(R.id.password_til);
        firstNameEt = findViewById(R.id.firstName_signup_et);
        firstNameTil = findViewById(R.id.firstName_til);
        lastNameEt = findViewById(R.id.lastName_signup_et);
        lastNameTil = findViewById(R.id.lastName_til);
        locationTil = findViewById(R.id.location_til);
        dobEt = findViewById(R.id.dob_signup_et);
        dobTil = findViewById(R.id.dob_til);
        genderRg = findViewById(R.id.sign_up_gender_rg);
        locationProgressBar = findViewById(R.id.signup_location_progressbar);

        // DOB spinner dialog
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                birthdate = new org.joda.time.LocalDate(year, monthOfYear + 1, dayOfMonth);
                dobEt.setText(sdf.format(myCalendar.getTime()));
            }

        };

        dobEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUpActivity.this, R.style.MySpinnerDatePickerStyle, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        // Location autocomplete
        autoCompleteLocation = findViewById(R.id.location_signup_et);
        locationAdapter = new AutocompleteLocationAdapter(SignUpActivity.this, android.R.layout.simple_list_item_1);
        autoCompleteLocation.setAdapter(locationAdapter);

        getLocationBtn = findViewById(R.id.getLocation_signup_btn);

        geocoder = new Geocoder(this);

        // toolbar
        Toolbar toolbar = findViewById(R.id.signup_toolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
        }

        location = (LocationManager) getSystemService(LOCATION_SERVICE);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        locationProgressBar.setVisibility(View.VISIBLE);
                        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, SignUpActivity.this);
                    }
                } else {
                    locationProgressBar.setVisibility(View.VISIBLE);
                    location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, SignUpActivity.this);
                }
            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSignup()) {
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                    progressDialog.setTitle("Signing up, please wait...");
                    progressDialog.show();
                    //create user
                    auth.createUserWithEmailAndPassword(emailEt.getText().toString(), passwordEt.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                  //  Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                    /*Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();*/
                                        Snackbar.make(findViewById(R.id.linear_layout), "Authentication failed.", BaseTransientBottomBar.LENGTH_SHORT).show();
                                    } else {

                                        addUserToUsersTable();

                                        // Add token of the user to db
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersTokens/" + ProfileActivity.currentSignedInUser.getUid());
                                                reference.child("token").setValue(FirebaseInstanceId.getInstance().getInstanceId());
                                                Log.d("testyLogin", reference.child("token/result/token").getDatabase().toString());
                                            }
                                        });

                                        Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                                        intent.putExtra("fromSignUp", true);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });
                }
            }
        });

    }


    /*get reference (or create it if not exists yet) to table "users" in firebase, and add the user to this table*/
    private void addUserToUsersTable() {

        if(longitude == 0.0 && latitude == 0.0) {
           getLatLongFromAddress();
        }

        User user = new User(auth.getCurrentUser().getUid(), firstNameEt.getText().toString(), lastNameEt.getText().toString(),
                        ((RadioButton) findViewById(genderRg.getCheckedRadioButtonId())).getText().toString()
                        , dobEt.getText().toString(), Years.yearsBetween(birthdate, LocalDate.now()).getYears(), autoCompleteLocation.getText().toString(),
                        latitude, longitude, false, true);

        /*Updates the user's location in "Users-Locations" for detect him by distance later*/
        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("Users-Locations"));
        geoFire.setLocation(auth.getCurrentUser().getUid(), new GeoLocation(user.getLatitude(), user.getLongitude()));


        FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).setValue(user);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    /*Get user's location and find its address by geo coder*/
    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        new Thread() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                super.run();

                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    final Address bestAddress = addresses.get(0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Objects.requireNonNull(autoCompleteLocation).setText(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                            locationProgressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private boolean validateSignup() {
        boolean isMissing = false;
        boolean locationValid = false;
        // Check if user entered all the required info correctly
        if (TextUtils.isEmpty(firstNameEt.getText())) {
            firstNameTil.setError("First Name is required!");
            isMissing = true;
        } else if (firstNameEt.getText().toString().contains(" ")){
            firstNameTil.setError("Spaces are not allowed!");
            isMissing = true;
        }
        if (TextUtils.isEmpty(lastNameEt.getText())) {
            lastNameTil.setError("Last Name is required!");
            isMissing = true;
        } else if (lastNameEt.getText().toString().contains(" ")){
            lastNameTil.setError("Spaces are not allowed!");
            isMissing = true;
        }
        if (TextUtils.isEmpty(dobEt.getText())) {
            dobTil.setError("Date of Birth is required!");
            isMissing = true;
        }
        if (genderRg.getCheckedRadioButtonId() == -1) {
            RadioButton radioButton = findViewById(R.id.sign_up_female_rb);
            radioButton.setError("Choose Gender");
            isMissing = true;
        }
        if (TextUtils.isEmpty(emailEt.getText())) {
            emailTil.setError("Email is required!");
            isMissing = true;
        }
        if (TextUtils.isEmpty(autoCompleteLocation.getText())) {

            locationTil.setError("Location is required!");
            isMissing = true;
        } else {
            for (int i = 0; i < locationAdapter.getCount(); i++) {
                if (locationAdapter.getItem(i).equals(autoCompleteLocation.getText().toString())) {
                    locationValid = true;
                }
            }
            if (!locationValid) {
                locationTil.setError("Please choose location from the list");
                isMissing = true;
            }
        }

        if (TextUtils.isEmpty(passwordEt.getText())) {
            passwordTil.setError("Password is required!");
            isMissing = true;
        } else if (passwordEt.length() < 6) {
            passwordTil.setError("Password is too short!");
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            isMissing = true;
        }
        if (isMissing) {
            return false;
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*Set longitude and latitude from a given address*/
    private void getLatLongFromAddress(){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            //Get latLng from String
            address = coder.getFromLocationName(autoCompleteLocation.getText().toString(), 5);

            //check for null
            if (address != null) {
                //Lets take first possibility from the all possibilities.
                Address location = address.get(0);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
