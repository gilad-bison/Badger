package com.example.badger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.badger.models.Like;
import com.example.badger.models.Post;
import com.example.badger.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class FeedActivity extends AppCompatActivity {

    static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    static final int RC_IMAGE_GALLERY = 2;
    static final int RC_EDIT_POST = 3;

    FirebaseUser fbUser;
    DatabaseReference database;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    ProgressBar mProgressBar;
    PostAdapter mAdapter;
    ArrayList<Post> mPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent callerIntent = getIntent();
        Boolean isPersonal = callerIntent.getBooleanExtra("isPersonal", false);
        setContentView(R.layout.activity_feed);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            finish();
        }

        database = FirebaseDatabase.getInstance().getReference();
        recyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(mPosts, this, isPersonal);
        recyclerView.setAdapter(mAdapter);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        Query postsQuery;
        if (isPersonal) {
            String uid = fbUser.getUid();
            postsQuery = database.child("posts").orderByChild("userId").equalTo(uid).limitToFirst(100);
        }
        else {
            postsQuery = database.child("posts").orderByKey().limitToFirst(100);
        }

        postsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mProgressBar.setVisibility(View.GONE);
                final Post post = dataSnapshot.getValue(Post.class);


                database.child("users/" + post.userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        post.user = user;
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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
                        mAdapter.notifyDataSetChanged();
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
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mAdapter.addPost(post);
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
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void uploadEvent(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, RC_IMAGE_GALLERY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, RC_IMAGE_GALLERY);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            Intent intent = new Intent(this, CreatePostActivity.class);
            intent.putExtra("imageUri", uri.toString());
            startActivity(intent);
        }

        if (requestCode == RC_EDIT_POST && resultCode == RESULT_OK) {
            String description = data.getStringExtra("description");
            ArrayList<String> badges = data.getStringArrayListExtra("badges");
            String postKey = data.getStringExtra("postKey");
            updatePost(postKey, description, badges);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                signOut();
                return true;
            case R.id.profile:
                goToProfile();
                return true;
            case R.id.home:
                goToHomePage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToProfile() {
        Intent personalIntent = new Intent(FeedActivity.this, FeedActivity.class);
        personalIntent.putExtra("isPersonal", true);
        startActivity(personalIntent);
    }

    private void goToHomePage() {
        Intent personalIntent = new Intent(FeedActivity.this, FeedActivity.class);
        startActivity(personalIntent);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(FeedActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    public void DeletePost(Post post) {
        database.child("posts").child(post.key).removeValue();
        for (Post currPost: mPosts) {
            if (currPost.key == post.key) {
                mPosts.remove(currPost);
                break;
            }
        }

        RefreshRecycler();
    }

    private void updatePost(String postKey, String description, ArrayList<String> badges) {
        for (Post post : mPosts) {
            if (post.key.equals(postKey)) {
                post.description = description;
                post.badges = badges;
                break;
            }
        }

        RefreshRecycler();
    }

    private void RefreshRecycler() {
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.getRecycledViewPool().clear();
        mAdapter.notifyDataSetChanged();
    }

    public void EditPost(Post post) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("imageUri", post.imageDownloadUrl);
        intent.putExtra("description", post.description);
        intent.putExtra("badges", post.badges);
        intent.putExtra("editMode", true);
        intent.putExtra("postKey", post.key);
        startActivityForResult(intent, RC_EDIT_POST);
    }

    public void setLiked(Post post) {
        if(!post.hasLiked) {
            // add new Like
            post.hasLiked = true;
            Like like = new Like(post.key, fbUser.getUid());
            String key = database.child("likes").push().getKey();
            database.child("likes").child(key).setValue(like);
            post.userLike = key;
        } else {
            // remove Like
            post.hasLiked = false;
            if (post.userLike != null) {
                database.child("likes").child(post.userLike).removeValue();
            }
        }
    }
}

