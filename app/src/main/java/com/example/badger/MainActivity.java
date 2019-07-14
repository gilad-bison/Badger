package com.example.badger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    DatabaseReference database;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance().getReference();

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fbUser != null) {
            // get the FCM token
            String token = FirebaseInstanceId.getInstance().getToken();

            // save the user info in the database to users/UID/
            // we'll use the UID as part of the path
            com.example.badger.User user = new com.example.badger.User(fbUser.getUid(), fbUser.getDisplayName(), token);
            database.child("users").child(user.uid).setValue(user);

            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
        }
    }

//    public void Login(View view) {
//        Intent intent = new Intent(this, HomePageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editText);
//        String message = editText.getText().toString();
//        intent.putExtra("gilad", message);
//        startActivity(intent);
//    }

    public void signIn(View view) {
        startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder().build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in

                // get the Firebase user
                FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

                // get the FCM token
                String token = FirebaseInstanceId.getInstance().getToken();

                // save the user info in the database to users/UID/
                // we'll use the UID as part of the path
                User user = new User(fbUser.getUid(), fbUser.getDisplayName(), token);
                database.child("users").child(user.uid).setValue(user);

                Intent intent = new Intent(this, HomePageActivity.class);
                startActivity(intent);
            } else {
                // Sign in failed, check response for error code
                if (response != null) {
                    Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
