package com.example.badger.viewModels;

import android.app.Activity;
import android.view.ActionMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.example.badger.BadgerDatabase;
import com.example.badger.models.Like;
import com.example.badger.models.Post;
import com.example.badger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostViewModel extends ViewModel {
    DatabaseReference database;
    private MutableLiveData<List<Post>> postsLiveData;
    private List<Post> posts;
    private BadgerDatabase mBadgerDatabase;
    private FirebaseUser fbUser;

    public PostViewModel() {
        database = FirebaseDatabase.getInstance().getReference();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        postsLiveData = new MutableLiveData<>();
        posts = new ArrayList<>();
    }

    public LiveData<List<Post>> getPosts(Activity activity) {
        mBadgerDatabase = Room.databaseBuilder(activity, BadgerDatabase.class, "mydb")
                .allowMainThreadQueries()
                .build();

        Query postsQuery = database.child("posts").orderByKey().limitToFirst(100);
        loadPosts(postsQuery);
        return postsLiveData;
    }

    public LiveData<List<Post>> getPersonalPosts(Activity activity, String uid) {
        mBadgerDatabase = Room.databaseBuilder(activity, BadgerDatabase.class, "mydb")
                .allowMainThreadQueries()
                .build();

        Query postsQuery = database.child("posts").orderByChild("userId").equalTo(uid).limitToFirst(100);
        loadPosts(postsQuery);
        return postsLiveData;
    }

    private void loadPosts(Query postsQuery) {
        postsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final Post post = dataSnapshot.getValue(Post.class);

                User cachedUser = mBadgerDatabase.getUserDAO().getUserById(post.userId);
                if (cachedUser != null) {
                    post.user = cachedUser;
                }
                else {
                    database.child("users/" + post.userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            post.user = user;
                            mBadgerDatabase.getUserDAO().insert(user);
                            postsLiveData.setValue(posts);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                Query likesQuery = database.child("likes").orderByChild("postId").equalTo(post.key);
                likesQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Like like = dataSnapshot.getValue(Like.class);
                        post.addLike();

                        if(like.userId.equals(fbUser.getUid())) {
                            post.hasLiked = true;
                            post.userLike = dataSnapshot.getKey();
                        }

                        postsLiveData.setValue(posts);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Like like = dataSnapshot.getValue(Like.class);
                        post.removeLike();
                        if(like.userId.equals(fbUser.getUid())) {
                            post.hasLiked = false;
                            post.userLike = null;
                        }

                        postsLiveData.setValue(posts);

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                posts.add(0, post);
                postsLiveData.setValue(posts);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
