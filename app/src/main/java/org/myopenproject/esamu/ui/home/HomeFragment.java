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

public class HomeFragment extends Fragment
{
    private static final int CALL_EMERGENCY_REQUEST = 1;
    private CountDownButton buttonEmergency;
    private Button buttonCancel;
    private PulsatorLayout layoutPulsator;
    private ProgressDialog progress;
    private HomeActivity activity;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof HomeActivity) {
            activity = (HomeActivity) context;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
        // Inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        layoutPulsator = view.findViewById(R.id.homePulsator);
        buttonCancel = view.findViewById(R.id.homeButtonCancel);
        buttonCancel.setOnClickListener(v -> buttonEmergency.reset());
        buttonEmergency = view.findViewById(R.id.homeButtonEmergency);

        // Set up emergency button events
        buttonEmergency.setCountDownListeners(new CountDownButton.CountDownListeners()
        {
            @Override
            public void onStartCount()
            {
                int h = buttonCancel.getHeight();
                buttonCancel.animate().setDuration(500).translationY(1 - h).start();
            }

            @Override
            public void onStopCount(int count)
            {
                int h = buttonCancel.getHeight();
                buttonCancel.animate().setDuration(500).translationY(h - 1).start();
            }

            @Override
            public void onTick(int count)
            {
                Device.vibrate(getContext(), 200);
                layoutPulsator.start();
            }

            @Override
            public void onFinish()
            {
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
    public void onStop()
    {
        if (progress.isShowing()) {
            progress.dismiss();
        }

        buttonEmergency.reset();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (activity != null
            && requestCode == CALL_EMERGENCY_REQUEST
            && resultCode == Activity.RESULT_OK)
        {
            activity.showPage(HomeActivity.PAGE_HISTORY);
        }
    }

    @Override
    public void onDetach()
    {
        activity = null;
        super.onDetach();
    }

    public void reset()
    {
        if (buttonEmergency != null && buttonEmergency.isCounting()) {
            buttonEmergency.reset();
        }
    }

    private void startEmergency()
    {
        progress.show();
        startActivityForResult(
            new Intent(getContext(), EmergencyActivity.class), CALL_EMERGENCY_REQUEST);
    }
}
