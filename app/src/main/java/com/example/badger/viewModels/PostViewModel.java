package com.example.badger.viewModels;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.example.badger.BadgerDatabase;
import com.example.badger.activities.CreatePostActivity;
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

    private void upsertPost(Post post) {
        boolean found = false;
        for (int i = 0; i < posts.size(); i++) {
            Post currPost = posts.get(i);
            if (currPost.key.equals(post.key)) {
                posts.set(i, post);
                found = true;
                break;
            }
        }

        if (!found) {
            posts.add(0, post);
        }

        postsLiveData.setValue(posts);
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
                        post.upsertLike(like);

                        if(like.userId.equals(fbUser.getUid())) {
                            post.hasLiked = true;
                            post.userLike = dataSnapshot.getKey();
                        }
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
                    postsLiveData.setValue(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addPost (Post post) {
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
//                Toast.makeText(CreatePostActivity.this, "Upload failed!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
//                mProgress.setVisibility(View.GONE);
//                onBackPressed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        post.imageDownloadUrl = downloadUrl;
                        database.child("posts").child(post.key).setValue(post);
                    }
                });
            }
        });
    }
}
