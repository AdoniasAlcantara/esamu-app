package org.myopenproject.esamu.presentation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.presentation.emergency.EmergencyActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.widget.CountDownButton;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class HomeActivity extends AppCompatActivity {
    CountDownButton buttonEmergency;
    Button buttonCancel;
    PulsatorLayout layoutPulsator;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        layoutPulsator = findViewById(R.id.mainPulsator);
        buttonCancel = findViewById(R.id.homeButtonCancel);
        buttonCancel.setOnClickListener(v -> buttonEmergency.reset());
        buttonEmergency = findViewById(R.id.homeButtonEmergency);

        // Adding emergency button events
        buttonEmergency.setCountDownListeners(new CountDownButton.CountDownListeners() {
            @Override
            public void onStartCount() {
                int h = buttonCancel.getHeight();
                buttonCancel.animate().setDuration(500).translationY(1 - h).start();
            }

            @Override
            public void onStopCount(int count) {
                int h = buttonCancel.getHeight();
                buttonCancel.animate().setDuration(500).translationY(h - 1).start();
            }

            @Override
            public void onTick(int count) {
                Device.vibrate(HomeActivity.this, 200);
                layoutPulsator.start();
            }

            @Override
            public void onFinish() {
                Device.vibrate(HomeActivity.this, 500);
                layoutPulsator.start();
                startEmergency();
            }
        });

        // Set up Progress Dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.dialog_wait));
    }

    @Override
    protected void onStop() {
        if (progress.isShowing())
            progress.dismiss();

        buttonEmergency.reset();
        super.onStop();
    }

    private void startEmergency() {
        progress.show();
        startActivity(new Intent(this, EmergencyActivity.class));
    }
}
