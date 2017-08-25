package com.example.ariel.mypdfapp;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

/**
 * Created by ariel on 15/08/17.
 */

public class Perfil extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private CircleImageView circleImageView;
    int a;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_perfil,null);
        nameTextView = (TextView) view.findViewById(R.id.namePerfil);
        emailTextView = (TextView) view.findViewById(R.id.mailperfil);
        circleImageView = (CircleImageView) view.findViewById(R.id.circleImageViewPerfil);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (MyPDFApp.userType.equals("facebook")) {
                final AlertDialog dialog = new SpotsDialog(getContext());
                dialog.show();
                String name = user.getDisplayName();
                String email = user.getEmail();
                Uri photoUrl = user.getPhotoUrl();
                String uid = user.getUid();
                nameTextView.setText(name);
                if (email==null) {
                    email="Email privado en facebook";
                    emailTextView.setText(email);
                }
                Picasso.with(getContext())
                        .load(photoUrl)
                        .into(circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onError() {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "ERROR TO LOAD IMG", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else if (MyPDFApp.userType.equals("firebase")){
                final AlertDialog dialog = new SpotsDialog(getContext());
                dialog.show();
                nameTextView.setText(MyPDFApp.name);
                emailTextView.setText(MyPDFApp.email);
                Picasso.with(getContext())
                        .load(MyPDFApp.urlPhoto)
                        .into(circleImageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onError() {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "ERROR TO LOAD IMG", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        return view;
    }

}
