package org.myopenproject.esamu.presentation.signup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.common.UserDto;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.widget.CustomViewPager;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SIGNUP";
    private static final int PAGE_INFO = 0;
    private static final int PAGE_PHONE = 1;
    private static final int PAGE_TOKEN = 2;
    private static final int NUM_PAGES = 3;

    // Fragments (aka "pages")
    private InfoFragment fragInfo;      // Shows some information to the user
    private PhoneFragment fragPhone;    // Form with phone number
    private TokenFragment fragToken;    // Form with confirmation token

    private CustomViewPager pager;
    private ProgressDialog progress;

    private UserDto user; // The user to register

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_signup);

        // Set things up
        fragInfo = InfoFragment.newInstance();
        fragPhone = PhoneFragment.newInstance();
        fragToken = TokenFragment.newInstance();

        pager = findViewById(R.id.signUpPager);
        pager.setPagingEnabled(false);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setTitle(R.string.dialog_wait);
        progress.setMessage(getString(R.string.dialog_sending));

        user = new UserDto();
    }

    public void finishSuccess() {
        Dialog.toast(this, getString(R.string.signup_text_success));
        setResult(RESULT_OK);
        finish();
    }

    public void finishUnsuccessful() {
        Dialog.toast(this, getString(R.string.signup_text_unsuccessful));
        finish();
    }

    public void showPhonePage() {
        pager.setCurrentItem(PAGE_PHONE);
    }

    public void sendPhone(String name, String phone) {
        progress.show();
        user.setName(name);
        user.setPhone(phone);

        // Send phone number to Firebase
        Log.i(TAG, "Request phone verification for " + phone);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone, 60, TimeUnit.SECONDS, this, callbacks);
    }

    // Phone number verification callbacks
    private OnVerificationStateChangedCallbacks callbacks =
            new OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(String id, ForceResendingToken forceResendingToken) {
                    Log.i(TAG, "Token sent. ID " + id);
                    fragToken.onTokenSent(user, id);
                    pager.setCurrentItem(PAGE_TOKEN);
                    progress.dismiss();
                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    fragToken.authenticate(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.e(TAG, "Phone number verification failed", e);
                    progress.dismiss();

                    // Error checking
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Malformed phone number
                        fragPhone.showInvalidPhone();
                    } else {
                        int title;
                        int message;

                        if (e instanceof FirebaseApiNotAvailableException) {
                            // Google play isn't installed
                            title = R.string.error_googleplay_title;
                            message = R.string.error_googleplay_msg;
                        } else {
                            // SMS quota was exceeded
                            title = R.string.error_unavailable_title;
                            message = R.string.signup_error_sms_exceeded;
                        }

                        // Show an error dialog and exit the activity
                        Dialog.alert(SignUpActivity.this, title, message,
                                (dialog, which) -> finishUnsuccessful());
                    }
                }
            };

    // Adapter for sign up pages
    private class PagerAdapter extends FragmentStatePagerAdapter {
        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case PAGE_INFO: return fragInfo;
                case PAGE_PHONE: return fragPhone;
                case PAGE_TOKEN: return fragToken;
                default: return fragInfo;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
