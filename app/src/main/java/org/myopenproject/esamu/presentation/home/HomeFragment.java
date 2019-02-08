package org.myopenproject.esamu.presentation.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.presentation.emergency.EmergencyActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.widget.CountDownButton;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class HomeFragment extends Fragment {
    private CountDownButton buttonEmergency;
    private Button buttonCancel;
    private PulsatorLayout layoutPulsator;
    private ProgressDialog progress;

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,

            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        layoutPulsator = view.findViewById(R.id.homePulsator);
        buttonCancel = view.findViewById(R.id.homeButtonCancel);
        buttonCancel.setOnClickListener(v -> buttonEmergency.reset());
        buttonEmergency = view.findViewById(R.id.homeButtonEmergency);

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
                Device.vibrate(getContext(), 200);
                layoutPulsator.start();
            }

            @Override
            public void onFinish() {
                Device.vibrate(getContext(), 500);
                layoutPulsator.start();
                startEmergency();
            }
        });

        // Set up Progress Dialog
        progress = new ProgressDialog(getContext());
        progress.setCancelable(false);
        progress.setMessage(getString(R.string.dialog_wait));

        return view;
    }

    @Override
    public void onStop() {
        reset();
        super.onStop();
    }

    public void reset() {
        if (progress.isShowing())
            progress.dismiss();

        buttonEmergency.reset();
    }

    private void startEmergency() {
        progress.show();
        startActivity(new Intent(getContext(), EmergencyActivity.class));
    }
}
