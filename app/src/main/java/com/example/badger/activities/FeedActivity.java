package com.example.badger.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
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
import android.widget.TextView;

import com.example.badger.PostAdapter;
import com.example.badger.R;
import com.example.badger.models.Like;
import com.example.badger.models.Post;
import com.example.badger.viewModels.FeedViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    static final int RC_IMAGE_GALLERY = 2;
    static final int RC_EDIT_POST = 3;
    static final int RC_CREATE_POST = 4;

    FirebaseUser mFirebaseUser;
    DatabaseReference mDatabaseReference;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    ProgressBar mProgressBar;
    PostAdapter mAdapter;
    FeedViewModel mViewModel;
    TextView mHelloTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent callerIntent = getIntent();
        Boolean isPersonal = callerIntent.getBooleanExtra("isPersonal", false);
        setContentView(R.layout.activity_feed);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            finish();
        }

        mViewModel = ViewModelProviders.of(this).get(FeedViewModel.class);

        LiveData<List<Post>> adapterDataSource;
        if (isPersonal) {
            adapterDataSource = mViewModel.getPersonalPosts(this, mFirebaseUser.getUid());
        }
        else {
            adapterDataSource = mViewModel.getPosts(this);
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(adapterDataSource, this);
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mHelloTextView = findViewById(R.id.badgerFeedTextBox);



        String userDisplayName = mViewModel.getUserFromCache(mFirebaseUser.getUid()).displayName;
        mHelloTextView.setText("Hi " + userDisplayName + "!");
    }

    public void uploadEvent(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            startImageSelectionActivity();
        }
    }

    private void startImageSelectionActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_IMAGE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImageSelectionActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            Intent intent = new Intent(this, CreatePostActivity.class);
            intent.putExtra("imageUri", uri.toString());
            startActivityForResult(intent, RC_CREATE_POST);
        }

        if (requestCode == RC_EDIT_POST && resultCode == RESULT_OK) {
            String description = data.getStringExtra("description");
            ArrayList<String> badges = data.getStringArrayListExtra("badges");
            String postKey = data.getStringExtra("postKey");
            updatePost(postKey, description, badges);
        }

        if (requestCode == RC_CREATE_POST && resultCode == RESULT_OK) {
            String description = data.getStringExtra("description");
            ArrayList<String> badges = data.getStringArrayListExtra("badges");
            String postKey = data.getStringExtra("postKey");
            String imageLocalUri = data.getStringExtra("imageLocalUri");
            createPost(postKey, description, badges, imageLocalUri);
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
        mViewModel.removePost(post);
        RefreshRecycler();
    }

    private void updatePost(String postKey, String description, ArrayList<String> badges) {
        mViewModel.updatePost(postKey, description, badges);
        RefreshRecycler();
    }

    private void createPost(String postKey, String description, ArrayList<String> badges, String imageLocalUri) {
        Post newPost = new Post(postKey, mFirebaseUser.getUid(), description, badges, true);
        newPost.imageLocalUri = imageLocalUri;
        newPost.user = mViewModel.getUserFromCache(mFirebaseUser.getUid());
        mViewModel.addPost(newPost);

        RefreshRecycler();
    }

    private void RefreshRecycler() {
        mRecyclerView.setAdapter(null);
        mRecyclerView.setLayoutManager(null);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.getRecycledViewPool().clear();
        mAdapter.notifyDataSetChanged();
    }

    public void EditPost(Post post) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("imageDownloadUrl", post.imageDownloadUrl);
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
            post.upsertLike(new Like(post.key, mFirebaseUser.getUid()));
            Like like = new Like(post.key, mFirebaseUser.getUid());
            String key = mDatabaseReference.child("likes").push().getKey();
            mViewModel.addLikeToPost(like, key);
            post.userLike = key;
        } else {
            // remove Like
            post.removeLike(new Like(post.key, mFirebaseUser.getUid()));
            post.hasLiked = false;
            if (post.userLike != null) {
                mViewModel.removeLikeFromPost(post.userLike);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void onPostResults() {
        mProgressBar.setVisibility(View.GONE);
    }
}

