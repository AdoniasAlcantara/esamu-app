package org.myopenproject.esamu.ui.signup;

import com.google.firebase.auth.PhoneAuthCredential;

import org.myopenproject.esamu.data.dto.UserDto;

public interface OnSignUpInteractionListener {
    void finishSuccess();
    void showPhonePage();
    void showTokenPage(UserDto userDto, String verificationId);
    void authenticate(PhoneAuthCredential credential);
}
