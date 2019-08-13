package com.example.badger.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Post {
    public String key;
    public String userId;
    public String imageDownloadUrl;
    public String description;
    public ArrayList<String> badges;

    // these properties will not be saved to the database
    @Exclude
    public User user;

    @Exclude
    public int likes = 0;

    @Exclude
    public boolean hasLiked = false;

    @Exclude
    public String userLike;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(com.example.badger.models.User.class)
    }

    public Post(String key, String userId, String imageDownloadUrl, String description, ArrayList<String> badges) {
        this.key = key;
        this.userId = userId;
        this.imageDownloadUrl = imageDownloadUrl;
        this.description = description;
        this.badges = badges;
    }

    public void addLike() {
        this.likes++;
    }

    public void removeLike() {
        this.likes--;
    }

}