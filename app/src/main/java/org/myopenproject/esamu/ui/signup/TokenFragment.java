package org.myopenproject.esamu.ui.signup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.onesignal.OneSignal;

import org.myopenproject.esamu.App;
import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.dto.UserDto;
import org.myopenproject.esamu.data.service.ErrorDecoder;
import org.myopenproject.esamu.data.service.Message;
import org.myopenproject.esamu.data.service.ServiceFactory;
import org.myopenproject.esamu.data.service.UserService;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

@SuppressWarnings("ConstantConditions")
public class TokenFragment extends Fragment {
    private static final String TAG = "SignUp";

    private OnSignUpInteractionListener listener;
    private String verificationId;  // The verification ID provided by Firebase
    private UserDto userDto;
    private UserService service = ServiceFactory
            .getInstance()
            .create(UserService.class);

    // Views
    private TextInputLayout tilToken;
    private ProgressDialog progress;
    private TextView tvSmsSent;
    private TextView tvChangePhone;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSignUpInteractionListener) {
            listener = (OnSignUpInteractionListener) context;
        } else {
            throw new RuntimeException("Activity class must implements "
                    + OnSignUpInteractionListener.class.getName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View layout = inflater.inflate(R.layout.fragment_token, container, false);

        layout.findViewById(R.id.signUpButtonConfirm).setOnClickListener(v -> validate());
        tilToken = layout.findViewById(R.id.signUpTilToken);
        tvSmsSent = layout.findViewById(R.id.signUpTvSmsSent);
        tvChangePhone = layout.findViewById(R.id.signUpTvChangePhone);
        progress = Dialog.makeProgress(
                container.getContext(), R.string.dialog_wait, R.string.dialog_sending);

        return layout;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    public void onTokenSent(UserDto userDto, String verificationId) {
        this.userDto = userDto;
        this.verificationId = verificationId;

        // Ask the user to enter the received SMS token
        String smsSent = String.format(getString(R.string.signup_sms_sent), userDto.getPhone());
        tvSmsSent.setText(smsSent);

        // Show a link that allows the user to change the phone number
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (listener != null) {
                    listener.showPhonePage();
                }
            }
        };

        SpannableString changePhone = new SpannableString(getText(R.string.signup_change_phone));
        changePhone.setSpan(span, 30, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvChangePhone.setText(changePhone);
        tvChangePhone.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void validate() {
        String token = tilToken.getEditText().getText().toString();

        // Validate user input
        if (!token.matches("\\d{6}")) {
            if (token.isEmpty()) {
                tilToken.setError(getString(R.string.error_empty));
            } else {
                tilToken.setError(String.format(getString(R.string.error_numeric_min), 6));
            }

            tilToken.requestFocus();
            return;
        }

        // Check network one more time
        if (!Device.isNetworkAvailable(getContext())) {
            Dialog.alert(getContext(), R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        tilToken.setError(null);
        authenticate(PhoneAuthProvider.getCredential(verificationId, token));
    }

    public void authenticate(PhoneAuthCredential credential) {
        Log.i(TAG, "SMS code: " + credential.getSmsCode());

        // Show the received token
        tilToken.getEditText().setText(credential.getSmsCode());
        progress.show();

        // Authenticate Firebase using credential
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get user ID created by Firebase and OneSignal
                        FirebaseUser firebaseUser = task.getResult().getUser();

                        userDto.setId(firebaseUser.getUid());
                        userDto.setNotificationKey(OneSignal
                                .getPermissionSubscriptionState()
                                .getSubscriptionStatus()
                                .getUserId());

                        // At this point the phone number is already authenticated.
                        // Now, register the ID generated through e-SAMU webservice
                        signUp();
                    } else {
                        // Incorrect verification code
                        tilToken.setError(getString(R.string.signup_error_auth));
                        progress.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send verification token");
                    Dialog.alert(
                            getContext(),
                            R.string.error_auth,
                            R.string.signup_error_auth_failure);
                });
    }

    private void signUp() {
        Log.d(TAG, "Registering user");
        service.save(userDto).enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                progress.dismiss();

                if (response.isSuccessful()) {
                    Log.d(TAG, "User has been registered. ID: " + userDto.getId());

                    // Save user locally
                    App.getInstance().saveUser(userDto);

                    if (listener != null) {
                        listener.finishSuccess();
                    }
                } else {
                    Message error = ErrorDecoder.decode(response.errorBody());
                    Log.d(TAG, "Returned with error code " + response.code()
                            + ", resource " + call.request().url().toString()
                            + ". Response: " + error);

                    Dialog.alert(
                            getContext(),
                            R.string.error_unavailable_title,
                            R.string.error_unavailable_msg);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to register user", t);
                progress.dismiss();
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.error_unreachable_server)
                        .setPositiveButton(
                                R.string.dialog_retry, (dialog, button) -> signUp())
                        .setNegativeButton(
                                R.string.dialog_cancel, (dialog, button) -> dialog.dismiss())
                        .show();
            }
        });
    }
}
