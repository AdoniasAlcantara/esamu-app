package org.myopenproject.esamu.presentation.signup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.common.ResponseDto;
import org.myopenproject.esamu.common.UserDto;
import org.myopenproject.esamu.domain.EmergencyService;
import org.myopenproject.esamu.domain.App;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;

@SuppressWarnings("ConstantConditions")
public class TokenFragment extends Fragment {
    private static final String TAG = "SIGNUP";

    private SignUpActivity activity;
    private ProgressDialog progress;
    private TextInputLayout inputToken;

    private String verificationId;  // The verification ID provided by Firebase
    private UserDto user;

    public TokenFragment() {}

    public static TokenFragment newInstance() {
        return new TokenFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SignUpActivity)
            activity = (SignUpActivity) context;
        else
            throw new RuntimeException("Activity class must be " + SignUpActivity.class.getName());

        progress = new ProgressDialog(activity);
        progress.setCancelable(false);
        progress.setTitle(R.string.dialog_wait);
        progress.setMessage(getString(R.string.dialog_sending));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_token, container, false);
        view.findViewById(R.id.signUpButtonConfirm).setOnClickListener(v -> validate());
        inputToken = view.findViewById(R.id.signUpInputToken);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public void onTokenSent(UserDto user, String verificationId) {
        this.user = user;
        this.verificationId = verificationId;

        // Ask the user to enter the received SMS token
        TextView tv = getView().findViewById(R.id.signUpTextSmsSent);
        tv.setText(String.format(getString(R.string.signup_text_sms_sent), user.getPhone()));
    }

    private void validate() {
        // Token validation
        String token = inputToken.getEditText().getText().toString();

        if (!token.matches("\\d{6}")) {
            if (token.isEmpty())
                inputToken.setError(getString(R.string.error_empty));
            else
                inputToken.setError(String.format(getString(R.string.error_numeric_min), 6));

            inputToken.requestFocus();
            return;
        } else {
            inputToken.setError(null);
        }

        authenticate(PhoneAuthProvider.getCredential(verificationId, token));
    }

    public void authenticate(PhoneAuthCredential credential) {
        Log.i(TAG, "Token: " + credential.getSmsCode());
        inputToken.getEditText().setText(credential.getSmsCode());

        // Verify network one last time
        if (!Device.isNetworkAvailable(activity)) {
            Dialog.alert(activity, R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        // This task can be time-consuming, so show a progress dialog
        progress.show();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                // Retrieve user data created by Firebase and OneSignal
                FirebaseUser fUser = task.getResult().getUser();
                user.setId(fUser.getUid());
                user.setNotificationKey(OneSignal
                        .getPermissionSubscriptionState()
                        .getSubscriptionStatus()
                        .getUserId());

                // At this point the phone number is already authenticated,
                // now it's time to register the user in the web service.
                doSignUpTask();
            } else {
                // Wrong verification token
                inputToken.setError(getString(R.string.signup_error_auth));
                progress.dismiss();
            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    private void doSignUpTask() {
        new AsyncTask<Void, Void, ResponseDto>() {
            @Override
            protected ResponseDto doInBackground(Void... voids) {
                ResponseDto response = null;

                try {
                    response = new EmergencyService().signUp(user);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot sign up " + user, e);
                }

                return response;
            }

            @Override
            protected void onPostExecute(ResponseDto response) {
                progress.dismiss();

                if (response != null) {
                    if (response.getStatusCode() == 201) { // 201: CREATED
                        Log.i(TAG, response.toString());

                        // User has been registered then persist it
                        App.getInstance().saveUser(user);
                        activity.finishSuccess();
                    } else {
                        // Something went very, very wrong
                        Log.e(TAG, response.toString());

                        if (response.getStatusCode() >= 500) { // 5xx: SERVER FAULT
                            Dialog.alert(activity,
                                    R.string.error_unavailable_title,
                                    R.string.error_unavailable_msg);
                        } else { // 4xx: CLIENT FAULT
                            // Fatal. Kill the application :(
                            activity.finishUnsuccessful();
                        }
                    }
                } else {
                    int title;
                    int message;

                    if (!Device.isNetworkAvailable(activity)) {
                        // When there is no connectivity on the device
                        title = R.string.error_network_title;
                        message = R.string.error_network_msg;
                    } else {
                        // When connected but the server is unreachable for some unknown reason
                        title = R.string.error_unavailable_title;
                        message = R.string.error_unavailable_msg;
                    }

                    Dialog.alert(activity, title, message);
                }
            }
        }.execute();
    }
}
