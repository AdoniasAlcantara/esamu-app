package org.myopenproject.esamu.presentation;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.domain.Preferences;
import org.myopenproject.esamu.presentation.signup.SignUpActivity;
import org.myopenproject.esamu.util.Permission;

public class MainActivity extends AppCompatActivity {
    private static final int SIGNUP_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up button event
        findViewById(R.id.mainButtonNext).setOnClickListener(v -> startSignUp());

        // Check app permissions on startup
        Permission.validate(this, 0,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE);

        // Check if user has the credentials
        if (Preferences.getInstance().isUserRegistered())
            startHome();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // If sign up was successful then start HomeActivity.
        // Otherwise kill the application.
        if (requestCode == SIGNUP_REQUEST && resultCode == RESULT_OK)
            startHome();
        else
            finish();
    }

    private void startHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void startSignUp() {
        startActivityForResult(new Intent(this, SignUpActivity.class), SIGNUP_REQUEST);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
