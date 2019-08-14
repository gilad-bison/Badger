package com.example.badger.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.badger.R;
import com.example.badger.viewModels.UserViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private UserViewModel mUserViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        setContentView(R.layout.activity_main);
        View backgroundImage = findViewById(R.id.mainLayout);
        Drawable background = backgroundImage.getBackground();
        background.setAlpha(50);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fbUser != null) {
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }
    }

    public void signIn(View view) {
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().build(),
            RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                mUserViewModel.addNewUser(this);

                Intent intent = new Intent(this, FeedActivity.class);
                startActivity(intent);
            } else if(response != null) {
                Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
