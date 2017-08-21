package com.example.ariel.mypdfapp;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ariel.mypdfapp.Users.UserRegister;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import dmax.dialog.SpotsDialog;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText editTextEmail;
    private EditText editTextPass;
    private String email;
    private String pass;
    private Button buttonLogin;
    private boolean validateField=true;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail = (EditText) findViewById(R.id.input_email);
        editTextPass = (EditText) findViewById(R.id.input_password);
        buttonLogin = (Button) findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(this);
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setVisibility(View.VISIBLE);
        loginButton.setReadPermissions(Arrays.asList("email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("PASO","0");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.e("PASO","1");
                    loginButton.setVisibility(View.GONE);
                    //goMainScreen();
                }
            }
        };
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        //progressBar.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
        final AlertDialog dialog2 = new SpotsDialog(this);
        dialog2.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                }
                Log.e("PASO","2");
                dialog2.dismiss();
                //progressBar.setVisibility(View.GONE);
                goMainScreen();
                //loginButton.setVisibility(View.VISIBLE);
            }
        });

    }
    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.link_signup:
                UserRegister myFragment = new UserRegister();
                getSupportFragmentManager().beginTransaction().replace(R.id.RegisterFragment,myFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.btn_login:
                validateField();
                if (validateField) {
                    final AlertDialog dialog = new SpotsDialog(this);
                    dialog.show();
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                goMainScreen();
                            }else {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(LoginActivity.this, "Password invalid", Toast.LENGTH_SHORT).show();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(LoginActivity.this, "Formato invalido del correo", Toast.LENGTH_SHORT).show();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(LoginActivity.this, "Email en uso", Toast.LENGTH_SHORT).show();
                                } catch(Exception e) {
                                    Toast.makeText(LoginActivity.this, "Usuario no econtrado", Toast.LENGTH_SHORT).show();
                                    Log.e("ERROR", e.getMessage());
                                }
                            }
                        }
                    });
                }
        }
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            getFragmentManager().popBackStack();
        } else {
            getFragmentManager().popBackStack();
        }

    }
    public void validateField(){
        email = editTextEmail.getText().toString();
        pass = editTextPass.getText().toString();
        if (TextUtils.isEmpty(email)) {
            validateField=false;
            Toast.makeText(this, "Ingrese email", Toast.LENGTH_SHORT).show();
        }else {
            validateField=true;
        }
        if (TextUtils.isEmpty(pass)){
            validateField=false;
            Toast.makeText(this, "Ingrese password", Toast.LENGTH_SHORT).show();
        }else {
            validateField=true;
        }
    }
}
