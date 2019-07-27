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

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity
{
    private static final String TAG = "SIGN_UP";

    // ViewPager index
    private static final int PAGE_INFO = 0;
    private static final int PAGE_PHONE = 1;
    private static final int PAGE_TOKEN = 2;
    private static final int NUM_PAGES = 3;

    // Fragments
    private InfoFragment fragInfo;      // Shows useful information for the user
    private PhoneFragment fragPhone;    // Ask the user to enter phone number
    private TokenFragment fragToken;    // Ask the user to enter the received token

    private SignUpViewPager pager;      // A custom ViewPager
    private ProgressDialog progress;

    private UserDto user;               // The user to register

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_signup);

        fragInfo = new InfoFragment();
        fragPhone = new PhoneFragment();
        fragToken = new TokenFragment();

        // Set up ViewPager
        pager = findViewById(R.id.signUpPager);
        pager.setPagingEnabled(false);
        pager.setAdapter(new SingUpAdapter(getSupportFragmentManager()));

        progress = Dialog.makeProgress(this, R.string.dialog_wait, R.string.dialog_sending);

        user = new UserDto();
    }

    public void finishSuccess()
    {
        Dialog.toast(this, getString(R.string.signup_text_success));
        setResult(RESULT_OK);
        finish();
    }

    public void finishUnsuccessful()
    {
        Dialog.toast(this, getString(R.string.signup_text_unsuccessful));
        finish();
    }

    public void showPhonePage()
    {
        pager.setCurrentItem(PAGE_PHONE);
    }

    public void sendPhone(String name, String phone)
    {
        progress.show();
        user.setName(name);
        user.setPhone(phone);

        Log.i(TAG, "Request phone verification for " + phone);

        // Send phone number to Firebase
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone,              // The number to verify
            60,                 // Maximum wait time
            TimeUnit.SECONDS,   // The time in seconds
            this,
            callbacks);
    }

    // Phone number verification callbacks
    private OnVerificationStateChangedCallbacks callbacks =
        new OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onCodeSent(String id, ForceResendingToken forceResendingToken)
            {
                Log.i(TAG, "Token sent. ID " + id);

                fragToken.onTokenSent(user, id);
                pager.setCurrentItem(PAGE_TOKEN);
                progress.dismiss();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                fragToken.authenticate(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                Log.e(TAG, "Phone number verification failed", e);
                progress.dismiss();

                // Checking what happened
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Malformed phone number
                    fragPhone.showInvalidPhone();
                } else {
                    int title;
                    int message;

                    if (e instanceof FirebaseApiNotAvailableException) {
                        // Maybe Google play isn't installed
                        title = R.string.error_googleplay_title;
                        message = R.string.error_googleplay_msg;
                    } else {
                        // SMS quota was exceeded
                        title = R.string.error_unavailable_title;
                        message = R.string.signup_error_sms_exceeded;
                    }

                    Log.e(TAG, e.getMessage());

                    // Show an error dialog and exit the activity
                    Dialog.alert(
                        SignUpActivity.this,
                        title,
                        message,
                        (dialog, which) -> finishUnsuccessful());
                }
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s)
            {
                // TODO
            }
        };

    private class SingUpAdapter extends FragmentStatePagerAdapter
    {
        SingUpAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
            switch (i) {
                case PAGE_INFO: return fragInfo;
                case PAGE_PHONE: return fragPhone;
                case PAGE_TOKEN: return fragToken;
                default: return fragInfo;
            }
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }
    }
}
