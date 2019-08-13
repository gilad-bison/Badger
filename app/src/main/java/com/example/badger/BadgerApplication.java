package com.example.badger;

import android.app.Application;
import android.content.Context;

public class BadgerApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        BadgerApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BadgerApplication.context;
    }
}