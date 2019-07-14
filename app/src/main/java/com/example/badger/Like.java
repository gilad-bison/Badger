package com.example.badger;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Like {
    public String imageId;
    public String userId;

    public Like() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.badger.Like.class)
    }

    public Like(String imageId, String userId) {
        this.imageId = imageId;
        this.userId = userId;
    }
}