package com.example.ariel.mypdfapp;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ariel on 15/08/17.
 */

public class MyPDFApp extends Application {

    public static String name="";
    public static String email="";
    public static String urlPhoto="";
    public static String userType = "";


    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}
