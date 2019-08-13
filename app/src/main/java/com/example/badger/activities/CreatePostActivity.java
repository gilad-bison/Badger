package com.example.badger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.badger.R;
import com.example.badger.models.Post;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private ChipGroup mBadgesChipGroup;
    private Button mPostButton;
    private String mPostKey;
    private boolean mIsEditMode;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
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
        if (currentDescription != null) {
            mDescriptionEditText.setText(currentDescription);
        }

        mPostButton = findViewById(R.id.postButton);
        mBadgesChipGroup = findViewById(R.id.filter_chip_group);
        ArrayList<String> currentBadges = callerIntent.getStringArrayListExtra("badges");
        if (currentBadges != null && currentBadges.size() > 0) {
            populateBadges(currentBadges);
        }

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            finish();
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void populateBadges(ArrayList<String> currentBadges) {
        for (String currBadge : currentBadges) {
            for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
                Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
                if (currentChip.getText().toString().equals(currBadge)) {
                    currentChip.setChecked(true);
                }
            }
        }
    }

    private String getPostKey() {
        if (mIsEditMode && mPostKey != null) {
            return mPostKey;
        }

        return mDatabaseReference.child("posts").push().getKey();
    }

    private Post getPostFromUI() {
        String description = mDescriptionEditText.getText().toString();
        ArrayList<String> badges = new ArrayList<>();
        for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
            Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
            if (currentChip.isChecked()) {
                badges.add(currentChip.getText().toString());
            }
        }

        String key = getPostKey();
        Post postFromUI = new Post(key, mFirebaseUser.getUid(), description, badges);

        if (this.mImageLocalURI != null) {
            postFromUI.imageLocalUri = this.mImageLocalURI;
        }
        else if (this.mImageDownloadUrl != null) {
            postFromUI.imageDownloadUrl = this.mImageDownloadUrl;
        }

        return postFromUI;
    }

    private void disableView() {
        mBadgesChipGroup.setEnabled(false);
        mDescriptionEditText.setEnabled(false);
        mPreviewImageView.setEnabled(false);
        mPostButton.setEnabled(false);
    }

    private void createPostObjectAndUpload() {
        Post postToUpload = this.getPostFromUI();
        // save image to database
        mProgress.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.putExtra("description", postToUpload.description);
        intent.putExtra("badges", postToUpload.badges);
        intent.putExtra("postKey", postToUpload.key);
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
