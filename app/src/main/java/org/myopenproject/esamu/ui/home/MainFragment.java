package org.myopenproject.esamu.ui.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.ui.emergency.EmergencyActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.widget.CountDownButton;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainFragment extends Fragment {
    private static final int REQUEST_CALL_EMERGENCY = 1;

    private CountDownButton btnEmergency;
    private Button btnCancel;
    private PulsatorLayout lytPulsator;
    private ProgressDialog progress;
    private OnHomeInteractionListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnHomeInteractionListener) {
            listener = (OnHomeInteractionListener) context;
        } else {
            throw new RuntimeException("Activity class must implements "
                    + OnHomeInteractionListener.class.getName());
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        // Inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        lytPulsator = view.findViewById(R.id.homelytPulsator);
        btnCancel = view.findViewById(R.id.homeBtnCancel);
        btnCancel.setOnClickListener(v -> btnEmergency.reset());
        btnEmergency = view.findViewById(R.id.homeBtnEmergency);

        // Set up emergency button events
        btnEmergency.setCountDownListeners(new CountDownButton.CountDownListeners() {
            @Override
            public void onStartCount() {
                btnCancel.animate()
                        .setDuration(500)
                        .translationY(1 - btnCancel.getHeight())
                        .start();
            }

            @Override
            public void onStopCount(int count) {
                btnCancel.animate()
                        .setDuration(500)
                        .translationY(btnCancel.getHeight() - 1)
                        .start();
            }

            @Override
            public void onTick(int count) {
                Device.vibrate(getContext(), 200);
                lytPulsator.start();
            }

            @Override
            public void onFinish() {
                Device.vibrate(getContext(), 500);
                lytPulsator.start();
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
        if (progress.isShowing()) {
            progress.dismiss();
        }

        btnEmergency.reset();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (listener != null
                && requestCode == REQUEST_CALL_EMERGENCY
                && resultCode == Activity.RESULT_OK) {
            listener.showPage(HomeActivity.PAGE_HISTORY);
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    public void reset() {
        if (btnEmergency != null && btnEmergency.isCounting()) {
            btnEmergency.reset();
        }
    }

    private void startEmergency() {
        progress.show();
        Intent it = new Intent(getContext(), EmergencyActivity.class);
        startActivityForResult(it, REQUEST_CALL_EMERGENCY);
    }
}
