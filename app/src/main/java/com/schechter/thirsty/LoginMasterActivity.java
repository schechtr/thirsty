package com.schechter.thirsty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginMasterActivity extends AppCompatActivity {


    final int GOOGLE_SIGN_IN = 3;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    Button btn_google_login, btn_login, btn_register;
    ProgressBar progressBar;
    TextInputEditText mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_master);

        btn_google_login = findViewById(R.id.btn_google_sign_in);
        btn_login = findViewById(R.id.btn_master_login);
        btn_register = findViewById(R.id.btn_master_register);
        progressBar = findViewById(R.id.progress_circular);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        progressBar.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance();



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginMasterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if(!task.isSuccessful())
                            Toast.makeText(LoginMasterActivity.this,"sign in error", Toast.LENGTH_SHORT).show();
                        else if(task.isSuccessful())
                            finish();


                    }
                });
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginMasterActivity.this ,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if(!task.isSuccessful())
                            Toast.makeText(LoginMasterActivity.this,"sign up error", Toast.LENGTH_SHORT).show();
                        else {
                            //String user_id = mAuth.getCurrentUser().getUid();
                            finish();
                        }
                    }
                });

            }
        });


        btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInGoogle();
            }
        });

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    public void SignInGoogle() {

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

                            progressBar.setVisibility(View.INVISIBLE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            progressBar.setVisibility(View.INVISIBLE);
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
            btn_login.setVisibility(View.INVISIBLE);
            btn_register.setVisibility(View.INVISIBLE);
            btn_google_login.setVisibility(View.INVISIBLE);
        } else {

            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
            btn_google_login.setVisibility(View.VISIBLE);
        }
    }
}
