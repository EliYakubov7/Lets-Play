package com.example.letsplay.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.example.letsplay.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private TextInputLayout emailTil, passwordTil;
    private FirebaseAuth auth;
    private Button btnSignup, btnLogin, btnReset, continueAsAGuestBtn;

    private TextView city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set Statusbar color
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top
        }

        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
//            LOGS USER IN ONCE IT FINDS HE HAD LOGGED IN!
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
            finish();
            return;
        }

        // set the view now
        setContentView(R.layout.activity_login);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        inputEmail = findViewById(R.id.email_signin_et);
        emailTil = findViewById(R.id.email_signin_til);
        inputPassword = findViewById(R.id.password_signin_et);
        passwordTil = findViewById(R.id.password_signin_til);
        btnSignup = findViewById(R.id.create_new_account_login_btn);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);
        continueAsAGuestBtn = findViewById(R.id.continue_as_a_guest_btn);

        city = findViewById(R.id.location_signup_et);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (validateSignin()) {
                    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("Signing in, please wait...");
                    progressDialog.show();
                    //authenticate user
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        // there was an error
                                        if (password.length() < 6) {
                                            inputPassword.setError(getString(R.string.minimum_password));
                                        } else {
                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        progressDialog.dismiss();

                                        // Add token of the user to db
//                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UsersTokens/" + auth.getUid());
//                                        reference.child("token").setValue(FirebaseInstanceId.getInstance().getInstanceId());
//                                        Log.d("testyLogin", reference.child("token/result/token").getDatabase().toString());

                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        continueAsAGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                intent.putExtra("is a guest", true);
                startActivity(intent);
            }
        });
    }

    private boolean validateSignin() {
        boolean isMissing = false;
        // Check if user entered all the required info correctly

        if (TextUtils.isEmpty(inputEmail.getText())) {
            emailTil.setError("Email is required!");
            isMissing = true;
        }
        if (TextUtils.isEmpty(inputPassword.getText())) {
            passwordTil.setError("Password is required!");
            isMissing = true;
        } else if (inputPassword.length() < 6) {
            passwordTil.setError("Password is too short!");
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            isMissing = true;
        }
        if (isMissing) {
            return false;
        }
        return true;
    }

}