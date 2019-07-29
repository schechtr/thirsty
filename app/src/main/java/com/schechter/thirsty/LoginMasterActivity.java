package com.schechter.thirsty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginMasterActivity extends AppCompatActivity {


    final int GOOGLE_SIGN_IN = 3;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    Button btn_google_login, btn_login, btn_register, btn_confirm_registration;
    ProgressBar progressBar;
    TextInputEditText mEmail, mPassword, mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_master);


        // initialize elements
        btn_google_login = findViewById(R.id.btn_google_sign_in);
        btn_login = findViewById(R.id.btn_master_login);
        btn_register = findViewById(R.id.btn_master_register);
        btn_confirm_registration = findViewById(R.id.btn_confirm_registration);
        progressBar = findViewById(R.id.progress_circular);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirm_password);


        // setup ui
        progressBar.setVisibility(View.GONE);
        btn_confirm_registration.setVisibility(View.GONE);
        mConfirmPassword.setVisibility(View.GONE);


        // get auth instance
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }


        // basic login
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                if (email.equals("") || password.equals("")) {
                    Toast.makeText(LoginMasterActivity.this, "please enter your email and password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginMasterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (!task.isSuccessful())
                                Toast.makeText(LoginMasterActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                            else if (task.isSuccessful())
                                finish();


                        }
                    });
                }
            }
        });

        // setup registration ui
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_google_login.setVisibility(View.GONE);
                btn_login.setVisibility(View.GONE);
                btn_register.setVisibility(View.GONE);
                btn_confirm_registration.setVisibility(View.VISIBLE);
                mConfirmPassword.setVisibility(View.VISIBLE);

            }
        });

        // basic registration
        btn_confirm_registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String confirm_password = mConfirmPassword.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                if (email.equals("") || password.equals("") || confirm_password.equals("")) {
                    Toast.makeText(LoginMasterActivity.this, "please enter your email and password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                } else if (!password.equals(confirm_password)) {
                    Toast.makeText(LoginMasterActivity.this, "passwords don't match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginMasterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful())
                                Toast.makeText(LoginMasterActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                            else {

                                addUserToDatabase(mAuth.getCurrentUser().getUid());

                                if (mAuth.getCurrentUser() != null) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                }
                                finish();
                            }
                        }
                    });
                }

            }
        });


        // google login/register
        btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInGoogle();
            }
        });



    }

    private void addUserToDatabase(String uid){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(uid).setValue(new User(uid));

    }

    private void SignInGoogle() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressBar.setVisibility(View.GONE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginMasterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            btn_login.setVisibility(View.GONE);
            btn_register.setVisibility(View.GONE);
            btn_google_login.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {

            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
            btn_google_login.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
