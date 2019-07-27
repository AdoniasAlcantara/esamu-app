package org.myopenproject.esamu.presentation.signup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.myopenproject.esamu.R;

public class InfoFragment extends Fragment
{
    private SignUpActivity activity;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof SignUpActivity) {
            activity = (SignUpActivity) context;
        } else {
            throw new RuntimeException("Activity class must be " + SignUpActivity.class.getName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        view.findViewById(R.id.signUpInfoButtonNext)
            .setOnClickListener(v -> activity.showPhonePage());

        return view;
    }

    @Override
    public void onDetach()
    {
        activity = null;
        super.onDetach();
    }
}
