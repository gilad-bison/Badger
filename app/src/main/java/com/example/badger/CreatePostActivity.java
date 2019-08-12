package com.example.badger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private Uri imageURI;
    private ProgressBar mProgress;
    private EditText mDescriptionEditText;
    private ImageView mPreviewImageView;
    private ChipGroup mBadgesChipGroup;
    private Button mPostButton;
    private String mPostKey;
    private boolean mIsEditMode;
    DatabaseReference database;
    FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        mProgress = findViewById(R.id.progress_bar);
        Intent callerIntent = getIntent();
        mPostKey = callerIntent.getStringExtra("postKey");
        mIsEditMode = callerIntent.getBooleanExtra("editMode", false);

        this.imageURI = Uri.parse(callerIntent.getStringExtra("imageUri"));
        mPreviewImageView = findViewById(R.id.previewImage);
        if (mIsEditMode) {
            Picasso.get().load(this.imageURI.toString()).into(mPreviewImageView);
        }
        else {
            mPreviewImageView.setImageURI(this.imageURI);
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
            for (String currBadge : currentBadges) {
                for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
                    Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
                    if (currentChip.getText().toString().equals(currBadge)) {
                        currentChip.setChecked(true);
                    }
                }
            }
        }

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            finish();
        }

        database = FirebaseDatabase.getInstance().getReference();
    }


    private String getPostKey() {
        if (mIsEditMode && mPostKey != null) {
            return mPostKey;
        }

        return database.child("posts").push().getKey();
    }

    private Post getPostFromUI() {
        String description = mDescriptionEditText.getText().toString();
        ArrayList<String> badges = new ArrayList<String>();
        for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
            Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
            if (currentChip.isChecked()) {
                badges.add(currentChip.getText().toString());
            }
        }

        String key = getPostKey();
        return new Post(key, fbUser.getUid(), this.imageURI.toString(), description, badges);
    }

    private void disableView() {
        mBadgesChipGroup.setEnabled(false);
        mDescriptionEditText.setEnabled(false);
        mPreviewImageView.setEnabled(false);
        mPostButton.setEnabled(false);
    }

    private void createPostObjectAndUpload(String downloadUrl) {
        Post postToUpload = this.getPostFromUI();
        postToUpload.imageDownloadUrl = downloadUrl;
        Toast.makeText(CreatePostActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();
        // save image to database
        database.child("posts").child(postToUpload.key).setValue(postToUpload);
        mProgress.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.putExtra("description", postToUpload.description);
        intent.putExtra("badges", postToUpload.badges);
        intent.putExtra("postKey", postToUpload.key);
        setResult(RESULT_OK,intent);
        finish();
        onBackPressed();
    }

    public void sendPost(View view) {
        disableView();
        mProgress.setVisibility(View.VISIBLE);
        if (mIsEditMode) {
            createPostObjectAndUpload(this.imageURI.toString());
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images");
        StorageReference userRef = imagesRef.child(fbUser.getUid());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = fbUser.getUid() + "_" + timeStamp;
        final StorageReference fileRef = userRef.child(filename);

        UploadTask uploadTask = fileRef.putFile(this.imageURI);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(CreatePostActivity.this, "Upload failed!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
                mProgress.setVisibility(View.GONE);
                onBackPressed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        createPostObjectAndUpload(downloadUrl);
                    }
                });
            }
        });
    }
}
