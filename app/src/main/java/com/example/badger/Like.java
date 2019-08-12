package com.example.badger;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Like {
    public String postId;
    public String userId;

    public Like() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.badger.Like.class)
    }

    public Like(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }
}