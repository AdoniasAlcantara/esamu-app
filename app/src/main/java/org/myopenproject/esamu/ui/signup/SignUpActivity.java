package org.myopenproject.esamu.ui.signup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.PhoneAuthCredential;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.dto.UserDto;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.widget.LockableViewPager;

public class SignUpActivity extends AppCompatActivity implements OnSignUpInteractionListener {
    private static final String TAG = "SignUp";

    // ViewPager index
    private static final int PAGE_INFO = 0;
    private static final int PAGE_PHONE = 1;
    private static final int PAGE_TOKEN = 2;
    private static final int NUM_PAGES = 3;

    private LockableViewPager pager;

    // Fragments
    private InfoFragment fragInfo;      // Shows useful information for the user
    private PhoneFragment fragPhone;    // Ask the user to enter phone number
    private TokenFragment fragToken;    // Ask the user to enter the received token

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_signup);

        fragInfo = new InfoFragment();
        fragPhone = new PhoneFragment();
        fragToken = new TokenFragment();

        // Set up ViewPager
        pager = findViewById(R.id.signUpPager);
        pager.setSwipeEnabled(false);
        pager.setAdapter(new SingUpAdapter(getSupportFragmentManager()));
    }

    @Override
    public void finishSuccess() {
        Dialog.toast(this, getString(R.string.signup_success));
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void showPhonePage() {
        pager.setCurrentItem(PAGE_PHONE);
    }

    @Override
    public void showTokenPage(UserDto userDto, String verificationId) {
        fragToken.onTokenSent(userDto, verificationId);
        pager.setCurrentItem(PAGE_TOKEN);
    }

    @Override
    public void authenticate(PhoneAuthCredential credential) {
        fragToken.authenticate(credential);
    }

    private class SingUpAdapter extends FragmentStatePagerAdapter {
        SingUpAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case PAGE_INFO:
                    return fragInfo;
                case PAGE_PHONE:
                    return fragPhone;
                case PAGE_TOKEN:
                    return fragToken;
                default:
                    return fragInfo;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
