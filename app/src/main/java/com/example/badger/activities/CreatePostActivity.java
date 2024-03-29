package com.example.badger.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.badger.fragments.BadgesPickFragment;
import com.example.badger.R;
import com.example.badger.viewModels.PostEditViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CreatePostActivity extends AppCompatActivity {

    private String mImageLocalURI;
    private String mImageDownloadUrl;
    private ProgressBar mProgress;
    private EditText mDescriptionEditText;
    private ImageView mPreviewImageView;
    private Button mPostButton;
    private String mPostKey;
    private boolean mIsEditMode;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private PostEditViewModel mViewModel;
    private BadgesPickFragment mBadgesPickFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        mViewModel = ViewModelProviders.of(this).get(PostEditViewModel.class);
        mProgress = findViewById(R.id.progress_bar);
        Intent callerIntent = getIntent();
        mPostKey = callerIntent.getStringExtra("postKey");
        mIsEditMode = callerIntent.getBooleanExtra("editMode", false);
        mPreviewImageView = findViewById(R.id.previewImage);
        if (mIsEditMode) {
            mImageDownloadUrl = callerIntent.getStringExtra("imageDownloadUrl");
            Picasso.get().load(mImageDownloadUrl).into(mPreviewImageView);
        }
        else {
            this.mImageLocalURI = callerIntent.getStringExtra("imageUri");
            mPreviewImageView.setImageURI(Uri.parse(this.mImageLocalURI));
        }

        mDescriptionEditText = findViewById(R.id.editText);
        String currentDescription = callerIntent.getStringExtra("description");
        mViewModel.setDescriptionIfNotInitialized(currentDescription);
        mDescriptionEditText.setText(currentDescription);

        mPostButton = findViewById(R.id.postButton);
        ArrayList<String> currentBadges = callerIntent.getStringArrayListExtra("badges");
        mBadgesPickFragment = (BadgesPickFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.badgesFragment);
        Bundle args = new Bundle();
        args.putStringArrayList("badges", currentBadges);
        mBadgesPickFragment.setArguments(args);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            finish();
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        addListeners();

    }

    private void addListeners() {
        mDescriptionEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if(s.length() > 0) {
                    mViewModel.setDescription(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
    }


    private String getPostKey() {
        if (mIsEditMode && mPostKey != null) {
            return mPostKey;
        }

        return mDatabaseReference.child("posts").push().getKey();
    }

    private void disableView() {
        mBadgesPickFragment.disableView();
        mDescriptionEditText.setEnabled(false);
        mPreviewImageView.setEnabled(false);
        mPostButton.setEnabled(false);
    }

    private void createPostObjectAndUpload() {
        mProgress.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.putExtra("description", mViewModel.getDescription().getValue());
        intent.putExtra("badges", mViewModel.getBadges().getValue());
        intent.putExtra("postKey", getPostKey());
        intent.putExtra("imageDownloadUri", mImageDownloadUrl);
        intent.putExtra("imageLocalUri", mImageLocalURI);
        setResult(RESULT_OK,intent);
        finish();
        onBackPressed();
    }

    public void sendPost(View view) {
        disableView();
        mProgress.setVisibility(View.VISIBLE);
        createPostObjectAndUpload();
        mProgress.setVisibility(View.GONE);
    }
}
