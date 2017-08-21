package com.example.ariel.mypdfapp.Users;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ariel.mypdfapp.LoginActivity;
import com.example.ariel.mypdfapp.MainActivity;
import com.example.ariel.mypdfapp.Model.Users;
import com.example.ariel.mypdfapp.R;
import com.example.ariel.mypdfapp.References.FirebaseReferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ariel on 16/08/17.
 */

public class UserRegister extends Fragment implements View.OnClickListener {

    private ImageView iconCamera;
    private CircleImageView imageViewPerfil;
    private Button btnRegister;
    private final static int CAMERA_REQUEST_CODE = 1;
    private StorageReference mStorage;
    private Uri downloadUri;
    private EditText lblNombre;
    private EditText lblemail;
    private EditText lblpass;
    private FirebaseAuth firebaseAuth;
    private boolean conection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_register,container,false);

        firebaseAuth = FirebaseAuth.getInstance();

             imageViewPerfil = (CircleImageView) view.findViewById(R.id.imgPerfil);
             mStorage = FirebaseStorage.getInstance().getReference();
             iconCamera = (ImageView) view.findViewById(R.id.iconCamera);
             btnRegister = (Button) view.findViewById(R.id.btn_Register);
            lblemail = (EditText) view.findViewById(R.id.input_register_mail);
            lblNombre = (EditText) view.findViewById(R.id.input_register_name);
            lblpass = (EditText) view.findViewById(R.id.input_register_password);
        btnRegister.setOnClickListener(this);
        iconCamera.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iconCamera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST_CODE);
                break;
            case R.id.btn_Register:
                View view2 = getActivity().getCurrentFocus();
                if (view2 != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (conection=isNetworkAvailable())
                RegisterUser();
                else {
                    Toast.makeText(getContext(), "Error de conexión  ", Toast.LENGTH_SHORT).show();
                }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            final AlertDialog dialog = new SpotsDialog(getContext());
            dialog.show();
            Uri uri = data.getData();
            StorageReference pathfile = mStorage.child("photos").child(uri.getLastPathSegment());
            pathfile.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Succes", Toast.LENGTH_SHORT).show();
                    downloadUri= taskSnapshot.getDownloadUrl();
                    Picasso.with(getActivity()).load(downloadUri).into(imageViewPerfil);
                    dialog.dismiss();
                }
            });
        }


    }

    public void RegisterUser(){
      final String name = lblNombre.getText().toString();
        final String email = lblemail.getText().toString();
        final String pass = lblpass.getText().toString();
        boolean validation=true;

        if (TextUtils.isEmpty(name)){
            validation=false;
            Toast.makeText(getContext(), "Campo nombre vacio", Toast.LENGTH_SHORT).show();
            return;
        }else if (TextUtils.isEmpty(email)){
            validation=false;
            Toast.makeText(getContext(), "Campo email vacio", Toast.LENGTH_SHORT).show();
            return;
        }else if (TextUtils.isEmpty(pass)){
            validation=false;
            Toast.makeText(getContext(), "Campo password vacio", Toast.LENGTH_SHORT).show();
            return;
        }else if (downloadUri == null){
            validation=false;
            Toast.makeText(getContext(), "Tome una foto para su perfil", Toast.LENGTH_SHORT).show();
            return;
        }
        if (validation){
            final AlertDialog dialog = new SpotsDialog(getContext());
            dialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "User added", Toast.LENGTH_SHORT).show();
                    FirebaseUser userid = FirebaseAuth.getInstance().getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(FirebaseReferences.USERREFERENCES);
                    Users user = new Users(name.toString(),email.toString(),pass.toString(),downloadUri.toString());
                    myRef.child(userid.getUid().toString()).setValue(user);
                    goMainScreen();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    dialog.dismiss();
                    if(!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            Toast.makeText(getContext(), "Password invalid", Toast.LENGTH_SHORT).show();
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(getContext(), "Formato invalido del correo", Toast.LENGTH_SHORT).show();
                        } catch(FirebaseAuthUserCollisionException e) {
                            Toast.makeText(getContext(), "Email en uso", Toast.LENGTH_SHORT).show();
                        } catch(Exception e) {
                            Log.e("ERROR", e.getMessage());
                        }
                    }
                }
            });
        }
    }
    private void goMainScreen() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
