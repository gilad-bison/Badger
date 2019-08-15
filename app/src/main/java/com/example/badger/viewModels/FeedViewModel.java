package com.example.badger.viewModels;

import android.app.Activity;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.example.badger.BadgerDatabase;
import com.example.badger.models.Like;
import com.example.badger.models.Post;
import com.example.badger.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedViewModel extends ViewModel {
    private DatabaseReference mDatabaseReference;
    private MutableLiveData<List<Post>> mPostsLiveData;
    private List<Post> mPosts;
    private BadgerDatabase mBadgerDatabase;
    private FirebaseUser fbUser;

    public FeedViewModel() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        mPostsLiveData = new MutableLiveData<>();
        mPosts = new ArrayList<>();
    }

    public LiveData<List<Post>> getPosts(Activity activity) {
        mBadgerDatabase = Room.databaseBuilder(activity, BadgerDatabase.class, "mydb")
                .allowMainThreadQueries()
                .build();

        Query postsQuery = mDatabaseReference.child("posts").orderByKey().limitToFirst(100);
        loadPosts(postsQuery);
        return mPostsLiveData;
    }

    public LiveData<List<Post>> getPersonalPosts(Activity activity, String uid) {
        mBadgerDatabase = Room.databaseBuilder(activity, BadgerDatabase.class, "mydb")
                .allowMainThreadQueries()
                .build();

        Query postsQuery = mDatabaseReference.child("posts").orderByChild("userId").equalTo(uid).limitToFirst(100);
        loadPosts(postsQuery);
        return mPostsLiveData;
    }

    private void upsertPost(Post post) {
        boolean found = false;
        for (int i = 0; i < mPosts.size(); i++) {
            Post currPost = mPosts.get(i);
            if (currPost.key.equals(post.key)) {
                mPosts.set(i, post);
                found = true;
                break;
            }
        }

        if (!found) {
            mPosts.add(0, post);
        }

        mPostsLiveData.setValue(mPosts);
    }

    public User getUserFromCache(String uid) {
        return mBadgerDatabase.getUserDAO().getUserById(uid);
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
                    mDatabaseReference.child("users/" + post.userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            post.user = user;
                            mBadgerDatabase.getUserDAO().insert(user);
                            mPostsLiveData.setValue(mPosts);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                Query likesQuery = mDatabaseReference.child("likes").orderByChild("postId").equalTo(post.key);
                likesQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Like like = dataSnapshot.getValue(Like.class);
                        post.upsertLike(like);

                        if(like.userId.equals(fbUser.getUid())) {
                            post.hasLiked = true;
                            post.userLike = dataSnapshot.getKey();
                        }

                        mPostsLiveData.setValue(mPosts);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Like like = dataSnapshot.getValue(Like.class);
                        post.removeLike(like);
                        if(like.userId.equals(fbUser.getUid())) {
                            post.hasLiked = false;
                            post.userLike = null;
                        }

                        mPostsLiveData.setValue(mPosts);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                upsertPost(post);
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
                    mPostsLiveData.setValue(mPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addLikeToPost(Like like, String key) {
        mDatabaseReference.child("likes").child(key).setValue(like);
    }

    public void removeLikeFromPost(String key) {
        mDatabaseReference.child("likes").child(key).removeValue();
    }

    public void removePost(Post post) {
        for (Post currPost: mPosts) {
            if (currPost.key == post.key) {
                mPosts.remove(currPost);
                break;
            }
        }

        mPostsLiveData.setValue(mPosts);
        mDatabaseReference.child("posts").child(post.key).removeValue();
    }

    public void updatePost(String postKey, String description, ArrayList<String> badges) {
        Post selectedPost = null;
        for (Post post : mPosts) {
            if (post.key.equals(postKey)) {
                post.description = description;
                post.badges = badges;
                selectedPost = post;
                break;
            }
        }

        mPostsLiveData.setValue(mPosts);
        if (selectedPost != null) {
            mDatabaseReference.child("posts").child(selectedPost.key).setValue(selectedPost);
        }
    }

    public void addPost (Post post) {
        mPosts.add(0, post);
        mPostsLiveData.setValue(mPosts);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images");
        StorageReference userRef = imagesRef.child(fbUser.getUid());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = fbUser.getUid() + "_" + timeStamp;
        final StorageReference fileRef = userRef.child(filename);

        UploadTask uploadTask = fileRef.putFile(Uri.parse(post.imageLocalUri));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        post.imageDownloadUrl = downloadUrl;
                        mDatabaseReference.child("posts").child(post.key).setValue(post);
                    }
                });
            }
        });
    }
}
