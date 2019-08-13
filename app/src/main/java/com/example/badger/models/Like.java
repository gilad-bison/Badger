package com.example.badger.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Like {
    public String postId;
    public String userId;

    public Like() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.badger.models.Like.class)
    }

    public Like(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }
}