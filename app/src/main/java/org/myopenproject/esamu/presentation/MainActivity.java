package org.myopenproject.esamu.presentation;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.App;
import org.myopenproject.esamu.presentation.home.HomeActivity;
import org.myopenproject.esamu.presentation.signup.SignUpActivity;
import org.myopenproject.esamu.util.Permission;

public class MainActivity extends AppCompatActivity
{
    // Request codes
    private static final int REQUEST_SIGN_UP = 999;
    private static final int REQUEST_PERMISSION_ALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up start button event
        findViewById(R.id.mainButtonNext).setOnClickListener(v -> startSignUp());

        // Check app permissions on startup
        Permission.validate(this, REQUEST_PERMISSION_ALL,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE);

        // Check if user has the credentials
        if (App.getInstance().isUserRegistered()) {
            startHome();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        // If sign up was successful then start HomeActivity.
        // Otherwise kill the application.
        if (requestCode == REQUEST_SIGN_UP && resultCode == RESULT_OK) {
            startHome();
        } else {
            finish();
        }
    }

    private void startHome()
    {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void startSignUp()
    {
        startActivityForResult(new Intent(this, SignUpActivity.class), REQUEST_SIGN_UP);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
