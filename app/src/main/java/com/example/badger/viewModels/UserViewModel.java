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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserViewModel extends ViewModel {
    private DatabaseReference mDatabaseReference;
    private BadgerDatabase mBadgerDatabase;
    private FirebaseUser fbUser;

    public UserViewModel() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public User getUserFromCache(String uid) {
        return mBadgerDatabase.getUserDAO().getUserById(uid);
    }

    public void addNewUser(Activity activity) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        User user = new User(fbUser.getUid(), fbUser.getDisplayName(), token);
        BadgerDatabase mBadgerDatabase = Room.databaseBuilder(activity, BadgerDatabase.class, "mydb")
                .allowMainThreadQueries()
                .build();

        mBadgerDatabase.getUserDAO().insert(user);
        mDatabaseReference.child("users").child(user.uid).setValue(user);
    }
}
