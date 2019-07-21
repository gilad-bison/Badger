package com.example.badger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreatePostActivity extends AppCompatActivity {

    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        Intent callerIntent = getIntent();
        this.imageURI = Uri.parse(callerIntent.getStringExtra("imageUri"));
        ImageView previewImageView = findViewById(R.id.previewImage);
        previewImageView.setImageURI(this.imageURI);
    }

    public void sendPost(View view) {
//        Uri uri = data.getData();
//
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
//        StorageReference imagesRef = storageRef.child("images");
//        StorageReference userRef = imagesRef.child(fbUser.getUid());
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String filename = fbUser.getUid() + "_" + timeStamp;
//        final StorageReference fileRef = userRef.child(filename);
//
//        UploadTask uploadTask = fileRef.putFile(uri);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//                Toast.makeText(HomePageActivity.this, "Upload failed!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Uri downloadUrl = uri;
//                        Toast.makeText(HomePageActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();
//                        // save image to database
//                        String key = database.child("images").push().getKey();
//                        Image image = new Image(key, fbUser.getUid(), downloadUrl.toString());
//                        database.child("images").child(key).setValue(image);
//                    }
//                });
//
//            }
//        });
    }
}
