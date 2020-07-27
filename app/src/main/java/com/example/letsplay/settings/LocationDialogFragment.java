package com.example.letsplay.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.activities.SignUpActivity;
import com.example.letsplay.location.AutocompleteLocationAdapter;
import com.example.letsplay.objects.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

public class LocationDialogFragment extends DialogFragment implements LocationListener {

    private final int LOCATION_PERMISSION_REQUEST = 1;
    private Geocoder geocoder;
    private AutoCompleteTextView locationEt;
    private AutocompleteLocationAdapter locationAdapter;
    private ProgressBar progressBar;
    private TextInputLayout locationTil;
    private Handler handler = new Handler();

    private double latitude = 0.0;
    private double longitude = 0.0;

    public static LocationDialogFragment newInstance() {
        Bundle args = new Bundle();
        LocationDialogFragment fragment = new LocationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_dialog_fragment, container, false);

        locationEt = rootView.findViewById(R.id.location_dialog_frag_location_et);
        locationTil = rootView.findViewById(R.id.location_dialog_frag_location_til);
        ImageButton locationBtn = rootView.findViewById(R.id.location_dialog_frag_location_btn);
        Button submitBtn = rootView.findViewById(R.id.location_dialog_frag_change_btn);
        Button cancelBtn = rootView.findViewById(R.id.location_dialog_frag_cancel_btn);
        locationAdapter = new AutocompleteLocationAdapter(getContext(), android.R.layout.simple_list_item_1);
        locationEt.setAdapter(locationAdapter);
        geocoder = new Geocoder(getContext());
        progressBar = rootView.findViewById(R.id.location_pref_progressbar);

        // Set to current user location
        locationEt.setText(ProfileActivity.currentSignedInUser.getLocation());

        if (Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
        }

        final LocationManager location = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, LocationDialogFragment.this);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, LocationDialogFragment.this);
                }
            }
        });


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateLocationInput()) {

                    if(longitude == 0.0 && latitude == 0.0) {
                        getLatLongFromAddress();
                    }

                    ProfileActivity.currentSignedInUser.setLocation(locationEt.getText().toString());
                    ProfileActivity.currentSignedInUser.setLongitude(longitude);
                    ProfileActivity.currentSignedInUser.setLatitude(latitude);

                    /*Updates the user's location in "Users-Locations" for detect hom by distance later*/
                    GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("Users-Locations"));
                    geoFire.setLocation( ProfileActivity.currentSignedInUser.getUid(), new GeoLocation
                            (ProfileActivity.currentSignedInUser.getLatitude(),  ProfileActivity.currentSignedInUser.getLongitude()));

                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ProfileActivity.currentSignedInUser);
                    dismiss();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }


    @Override
    public void onLocationChanged(final Location location) {
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
                            Objects.requireNonNull(locationEt).setText(bestAddress.getLocality() + ", " + bestAddress.getCountryName());
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.label_location_dialog_title))
                        .setMessage(getResources().getString(R.string.label_location_dialog_body))
                        .setPositiveButton(getResources().getString(R.string.label_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                        }).setNegativeButton(getResources().getString(R.string.label_quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).setCancelable(false).show();
            }
    }

    private boolean validateLocationInput() {
        boolean locationValid = false;

        if (TextUtils.isEmpty(locationEt.getText())) {
            locationTil.setError("Location is required!");
            return false;
        } else {
            for (int i = 0; i < locationAdapter.getCount(); i++) {
                if (locationAdapter.getItem(i).equals(locationEt.getText().toString())) {
                    locationValid = true;
                }
            }
            if (!locationValid) {
                locationTil.setError("Please choose location from list");
                return false;
            }
        }
        return true;
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
        Geocoder coder = new Geocoder(getContext());
        List<Address> address;
        try {
            //Get latLng from String
            address = coder.getFromLocationName(locationEt.getText().toString(), 5);

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
