package org.myopenproject.esamu.presentation.signup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;

@SuppressWarnings("ConstantConditions")
public class PhoneFragment extends Fragment
{
    private SignUpActivity activity;

    // Views for pick up user's phone number and username
    private TextInputLayout inputName;
    private TextInputLayout inputPhone;

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
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        view.findViewById(R.id.signUpPhoneButtonNext).setOnClickListener(v -> validate());
        inputName = view.findViewById(R.id.signUpInputName);
        inputPhone = view.findViewById(R.id.signUpInputPhone);

        // Set up mask for phone input
        EditText editText = inputPhone.getEditText();

        MaskTextWatcher mtw = new MaskTextWatcher(
            editText,
            new SimpleMaskFormatter(getString(R.string.signup_text_phone_mask)));

        editText.addTextChangedListener(mtw);

        return view;
    }

    @Override
    public void onDetach()
    {
        activity = null;
        super.onDetach();
    }

    public void showInvalidPhone()
    {
        inputPhone.setError(getString(R.string.signup_error_invalid_phone));
        inputPhone.requestFocus();
    }

    private void validate()
    {
        // Validate name
        String name = inputName.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            inputName.setError(getString(R.string.error_empty));
            inputName.requestFocus();
            return;
        } else {
            inputName.setError(null);
        }

        // Validate phone number
        String phone = inputPhone.getEditText().getText().toString().replaceAll("[ ()-]", "");

        if (!phone.matches("\\d{10,}")) {
            if (phone.isEmpty()) {
                inputPhone.setError(getString(R.string.error_empty));
            } else {
                inputPhone.setError(String.format(getString(R.string.error_numeric_min), 10));
            }

            inputPhone.requestFocus();
            return;
        } else {
            inputPhone.setError(null);
        }

        // Verify network connection
        if (!Device.isNetworkAvailable(activity)) {
            Dialog.alert(activity, R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        // Include country code prefix and send
        activity.sendPhone(name, getString(R.string.country_code) + phone);
    }
}
