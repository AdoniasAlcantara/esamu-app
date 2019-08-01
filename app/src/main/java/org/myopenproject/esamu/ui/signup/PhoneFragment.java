package org.myopenproject.esamu.ui.signup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.dto.UserDto;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public class PhoneFragment extends Fragment {
    private static final String TAG = "SignUp";

    private OnSignUpInteractionListener listener;
    private UserDto userDto = new UserDto();

    // Views
    private TextInputLayout tilName;
    private TextInputLayout tilPhone;
    private ProgressDialog progress;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SignUpActivity) {
            listener = (OnSignUpInteractionListener) context;
        } else {
            throw new RuntimeException("Activity class must implements "
                    + OnSignUpInteractionListener.class.getName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);

        // Set up views
        view.findViewById(R.id.signUpInfoBtnNext).setOnClickListener(v -> validate());
        tilName = view.findViewById(R.id.signUpTilName);
        tilPhone = view.findViewById(R.id.signUpTilPhone);

        // Set up mask for phone number input
        EditText etPhone = tilPhone.getEditText();
        String phoneMask = getString(R.string.signup_text_phone_mask);
        MaskTextWatcher mtw = new MaskTextWatcher(etPhone, new SimpleMaskFormatter(phoneMask));
        etPhone.addTextChangedListener(mtw);

        progress = Dialog.makeProgress(getContext(), R.string.dialog_wait, R.string.dialog_sending);
        return view;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    private void validate() {
        if (isNameValid() && isPhoneValid() && isNetworkAvailable()) {
            progress.show();
            Log.i(TAG, "Request phone verification for " + userDto.getPhone());

            // Verify phone number with Firebase
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    userDto.getPhone(),
                    60,
                    TimeUnit.SECONDS,
                    getActivity(),
                    callbacks);
        }
    }

    private boolean isNameValid() {
        String name = tilName.getEditText().getText().toString();

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_empty));
            tilName.requestFocus();
            return false;
        }

        tilName.setError(null);
        userDto.setName(name);
        return true;
    }

    private boolean isPhoneValid() {
        String phone = tilPhone.getEditText().getText().toString().replaceAll("[ ()-]", "");

        if (!phone.matches("\\d{10,11}")) {
            if (phone.isEmpty()) {
                tilPhone.setError(getString(R.string.error_empty));
            } else {
                tilPhone.setError(String.format(getString(R.string.error_numeric_min), 10));
            }

            tilPhone.requestFocus();
            return false;
        }

        tilName.setError(null);
        userDto.setPhone("+55" + phone);
        return true;
    }

    private boolean isNetworkAvailable() {
        if (!Device.isNetworkAvailable(getContext())) {
            Dialog.alert(getContext(), R.string.error_network_title, R.string.error_network_msg);
            return false;
        }

        return true;
    }

    private OnVerificationStateChangedCallbacks callbacks =
            new OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(String id, ForceResendingToken forceResendingToken) {
                    Log.i(TAG, "Token sent. ID " + id);
                    progress.dismiss();

                    if (listener != null) {
                        listener.showTokenPage(userDto, id);
                    }
                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    if (listener != null) {
                        listener.authenticate(phoneAuthCredential);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    progress.dismiss();

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        tilPhone.setError(getString(R.string.signup_error_invalid_phone));
                        tilPhone.requestFocus();
                    } else {
                        int title;
                        int message;

                        if (e instanceof FirebaseApiNotAvailableException) {
                            // Maybe Google play isn't installed
                            title = R.string.error_googleplay_title;
                            message = R.string.error_googleplay_msg;
                        } else {
                            // SMS quota exceeded
                            title = R.string.error_unavailable_title;
                            message = R.string.signup_error_sms_exceeded;
                        }

                        Log.e(TAG, "Authentication failed", e);
                        Dialog.alert(getContext(), title, message);
                    }
                }

                @Override
                public void onCodeAutoRetrievalTimeOut(String s) {
                    progress.dismiss();
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.signup_error_expiration_time_title)
                            .setMessage(R.string.signup_error_expiration_time_retry)
                            .setPositiveButton(
                                    R.string.dialog_yes, (dialog, button) -> validate())
                            .setNegativeButton(
                                    R.string.dialog_no, (dialog, button) -> dialog.dismiss())
                            .show();
                }
            };
}
