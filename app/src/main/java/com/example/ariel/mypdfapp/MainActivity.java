package com.example.ariel.mypdfapp;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ariel.mypdfapp.Model.Users;
import com.example.ariel.mypdfapp.References.FirebaseReferences;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView uidTextView;
    private CircleImageView circleImageView;
    private ProgressBar progressBar;
    private boolean find=false;
    private List<Users> listUsers;
    private boolean conection;
    private String photoUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listUsers = new ArrayList<>();
        progressBar = new ProgressBar(this);

        //Verifica conección

        if (!(conection=isNetworkAvailable())){
            Toast.makeText(this, "Error de conexión ", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            goLoginScreen();
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (MyPDFApp.userType.equals("firebase")) {
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext())
                            .load(photoUser)
                            .into(circleImageView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                    circleImageView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Error to load img", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        toggle.syncState();
       // NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        nameTextView = (TextView) hView.findViewById(R.id.nameTextView);
        emailTextView = (TextView) hView.findViewById(R.id.mailTextView);
        circleImageView = (CircleImageView) hView.findViewById(R.id.circleImgPerfil);
        progressBar = (ProgressBar) hView.findViewById(R.id.progressBar_photo);


        //FirebaseAuth.getInstance().getCurrentUser().getProviderId();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            String keyid= user.getUid();
            final String mail = user.getEmail();
            for (UserInfo useraux: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                if (useraux.getProviderId().equals("facebook.com")) {
                    circleImageView.setVisibility(View.VISIBLE);
                    find=true;
                    MyPDFApp.userType="facebook";
                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    Uri photoUrl = user.getPhotoUrl();
                    String uid = user.getUid();
                    if (email==null) {
                        email="Email privado en facebook";
                    }
                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    Picasso.with(getApplicationContext())
                            .load(photoUrl)
                            .into(circleImageView);
                    break;
                }
            }
            if (find==false){
                MyPDFApp.userType="firebase";
                FirebaseDatabase database =FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(FirebaseReferences.USERREFERENCES);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        listUsers.removeAll(listUsers);
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            Users users = dataSnapshot1.getValue(Users.class);
                            dataSnapshot.getKey();
                            listUsers.add(users);
                            if (mail.equals(users.getEmail())){
                                String name = users.getName();
                                String email = users.getEmail();
                                photoUser = users.getPhotourl();
                                nameTextView.setText(name);
                                emailTextView.setText(email);
                                MyPDFApp.name=name;
                                MyPDFApp.email=email;
                                MyPDFApp.urlPhoto=photoUser;
                                break;
                            }
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        }else {
            goLoginScreen();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this,WebViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Perfil myFragment = new Perfil();
            getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment,myFragment)
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            goLoginScreen();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void goLoginScreen(){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
