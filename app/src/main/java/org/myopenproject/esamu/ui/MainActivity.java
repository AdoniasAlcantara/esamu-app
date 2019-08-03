package org.myopenproject.esamu.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.App;
import org.myopenproject.esamu.ui.home.HomeActivity;
import org.myopenproject.esamu.ui.signup.SignUpActivity;
import org.myopenproject.esamu.util.Permission;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    // Request codes
    private static final int REQUEST_SIGN_UP = 1;
    private static final int REQUEST_PERMISSION_ALL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up button event
        findViewById(R.id.mainButtonNext).setOnClickListener(v -> startSignUp());

        // Check app permissions and user credential at startup
        if (grantPermissions() && App.getInstance().isUserRegistered()) {
            startHome();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // If sign up was successful then start HomeActivity.
        // Otherwise kill the application.
        if (requestCode == REQUEST_SIGN_UP && resultCode == RESULT_OK) {
            startHome();
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                Log.w(TAG, "Permission denied by user");
                finish();
            }
        }
    }

    private boolean grantPermissions() {
        return Permission.validate(this, REQUEST_PERMISSION_ALL,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE);
    }

    private void startHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void startSignUp() {
        startActivityForResult(new Intent(this, SignUpActivity.class), REQUEST_SIGN_UP);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
