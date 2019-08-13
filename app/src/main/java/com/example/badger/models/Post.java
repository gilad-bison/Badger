package com.example.badger.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Post {
    public String key;
    public String userId;
    public String imageDownloadUrl;
    public String description;
    public ArrayList<String> badges;

    // these properties will not be saved to the database
    @Exclude
    public String imageLocalUri;

    @Exclude
    public User user;

    @Exclude
    public List<Like> likes = new ArrayList<>();

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

    public Post(String key, String userId, String description, ArrayList<String> badges) {
        this.key = key;
        this.userId = userId;
        this.description = description;
        this.badges = badges;
    }

    public void upsertLike(Like like) {
        for(int i = 0; i < likes.size(); i++) {
            Like currLike = likes.get(i);
            if (currLike.userId.equals(like.userId)) {
                likes.set(i, like);
                return;
            }
        }

        this.likes.add(like);
    }

    public void removeLike(Like like) {
        for(int i = 0; i < likes.size(); i++) {
            Like currLike = likes.get(i);
            if (currLike.userId.equals(like.userId)) {
                likes.remove(i);
                return;
            }
        }
    }

}