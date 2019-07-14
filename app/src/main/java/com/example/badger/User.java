package com.example.badger;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String uid;
    public String displayName;
    public String token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.badger.User.class)
    }

    public User(String uid, String displayName, String token) {
        this.uid = uid;
        this.displayName = displayName;
        this.token = token;
    }
}