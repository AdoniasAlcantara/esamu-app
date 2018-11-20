package org.myopenproject.esamu.presentation.emergency;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.common.EmergencyDto;
import org.myopenproject.esamu.common.ResponseDto;
import org.myopenproject.esamu.common.UserDto;
import org.myopenproject.esamu.domain.EmergencyService;
import org.myopenproject.esamu.domain.Preferences;
import org.myopenproject.esamu.presentation.signup.SignUpActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.util.Permission;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressLint("MissingPermission")
public class EmergencyActivity extends AppCompatActivity {
    public static final String IS_PICTURE_TAKEN_PARAM = "isPictureTaken";

    private static final String TAG = "EMERGENCY";
    private static final int LOCATION_PERMISSION_REQUEST = 999;
    private static final int IMEI_PERMISSION_REQUEST = 998;
    private static final int ACQUIRE_IMAGE_REQUEST = 997;

    private FusedLocationProviderClient locationClient;
    private TextView labelTitle;
    private TextView labelMessage;
    private ImageView imagePictureTaken;
    private ProgressDialog progress;
    private FloatingActionsMenu buttonAdd;
    private Button buttonSend;
    private boolean isBackPressed;
    private boolean isPictureTaken;
    private byte[] picture;
    EmergencyDto emergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        emergency = new EmergencyDto();

        // Set up location service
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check runtime permission to get location
        if (Permission.validate(this, LOCATION_PERMISSION_REQUEST,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            locationClient.getLastLocation().addOnSuccessListener(this, this::setLocation);

        // Check runtime permission to get IMEI
        if (Permission.validate(this, IMEI_PERMISSION_REQUEST,
                Manifest.permission.READ_PHONE_STATE))
            emergency.setImei(Device.getIMEI(this));

        // Set up views
        imagePictureTaken = findViewById(R.id.emergencyImagePictureTaken);
        labelTitle = findViewById(R.id.emergencyLabelTitle);
        labelMessage = findViewById(R.id.emergencyLabelMsg);
        buttonAdd = findViewById(R.id.emergencyButtonAdd);
        buttonSend = findViewById(R.id.emergencyButtonSend);
        buttonSend.setVisibility(View.GONE);
        buttonSend.setOnClickListener(v -> send());
        findViewById(R.id.emergencyButtonCamera).setOnClickListener(v -> startCamera());
        findViewById(R.id.emergencyButtonVoice).setOnClickListener(v -> startVoice());
        progress = Dialog.makeProgress(this,
                R.string.error_unavailable_title,
                R.string.error_unavailable_msg);

        startCamera(); // Starts camera automatically
    }

    @Override
    protected void onStop() {
        buttonAdd.collapseImmediately();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ACQUIRE_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {
            picture = data.getByteArrayExtra(CameraActivity.IMAGE_EXTRA);
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            imagePictureTaken.setImageBitmap(bitmap);
            isPictureTaken = true;
            labelTitle.setText(R.string.emergency_text_ready_title);
            labelMessage.setText(R.string.emergency_text_ready_msg);
            buttonSend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0)
            return;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    locationClient.getLastLocation().addOnSuccessListener(this, this::setLocation);
                break;

            case IMEI_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    emergency.setImei(Device.getIMEI(this));
        }
    }

    @Override
    public void onBackPressed() {
        if (isBackPressed) {
            super.onBackPressed();
        } else {
            Dialog.toast(this, getString(R.string.emergency_toast_back_pressed));
            isBackPressed = true;
        }
    }

    private void startCamera() {
        Intent it = new Intent(this, CameraActivity.class);
        it.putExtra(IS_PICTURE_TAKEN_PARAM, isPictureTaken);
        startActivityForResult(it, ACQUIRE_IMAGE_REQUEST);
    }

    private void startVoice() {
        Dialog.toast(this, getString(R.string.error_not_implemented));
    }

    private void showPhoneCallDialog() {
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(EmergencyActivity.this);
        dBuilder.setTitle(R.string.error_unreachable_server);
        dBuilder.setCancelable(false);
        dBuilder.setMessage(R.string.emergency_dialog_suggest_call);
        dBuilder.setPositiveButton(R.string.emergency_dialog_call_192,
                (dialog, which) -> {
                    Device.doPhoneCall(this, "10331");
                    finish();
                });
        dBuilder.setNegativeButton(R.string.dialog_retry,
                (dialog, which) -> send());
    }

    private void setLocation(Location location) {
        emergency.setLatitude(Double.toString(location.getLatitude()));
        emergency.setLongitude(Double.toString(location.getLongitude()));
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder
                    .getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                emergency.setAddress(address.getAddressLine(0));
                emergency.setCity(address.getSubAdminArea());
                emergency.setState(address.getAdminArea());
                emergency.setCountry(address.getCountryName());
                emergency.setPostalCode(address.getPostalCode().replace("-", ""));
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot generate Geocoder", e);
        }
    }

    private void send() {
        if (!Device.isNetworkAvailable(this)) {
            Dialog.alert(this, R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        UserDto user = Preferences.getInstance().getUser();

        if (user == null) {
            // User has not registered yet
            startActivity(new Intent(this, SignUpActivity.class));
            return;
        }

        progress.show();
        emergency.setUserId(user.getId());
        emergency.setPicture(Base64.encodeToString(picture, Base64.DEFAULT));
        Log.i(TAG, emergency.toString());
        doReportEmergencyTask();
    }

    @SuppressLint("StaticFieldLeak")
    private void doReportEmergencyTask() {
        new AsyncTask<Void, Void, ResponseDto>() {
            @Override
            protected ResponseDto doInBackground(Void... voids) {
                ResponseDto response = null;

                try {
                    response = new EmergencyService().report(emergency);
                } catch (IOException e) {
                    Log.e(TAG, "Error sending emergency", e);
                }

                return response;
            }

            @Override
            protected void onPostExecute(ResponseDto response) {
                progress.dismiss();

                if (response != null) {
                    if (response.getStatusCode() == 201) { // 201: CREATED
                        Log.i(TAG, "Emergency sent: " + emergency);

                        Dialog.alert(EmergencyActivity.this,
                                R.string.emergency_dialog_sent_title,
                                R.string.emergency_dialog_sent_msg,
                                (dialog, which) -> finish());
                    } else {
                        Log.e(TAG, response.toString());

                        if (response.getStatusCode() >= 500) {
                            // 5xx: SERVER FAULT
                            Dialog.alert(EmergencyActivity.this,
                                    R.string.error_unavailable_title,
                                    R.string.error_unavailable_msg);
                        } else if (response.getStatusCode() == 401) {
                            // 401: UNAUTHORIZED
                            startActivity(new Intent(EmergencyActivity.this, SignUpActivity.class));
                        } else {
                            Dialog.alert(EmergencyActivity.this,
                                    R.string.error_fatal,
                                    R.string.error_fatal,
                                    (dialog, which) -> finish());
                        }
                    }
                } else {
                    if (!Device.isNetworkAvailable(EmergencyActivity.this)) {
                        // When there is no connectivity on the device
                        Dialog.alert(EmergencyActivity.this,
                                R.string.error_network_title,
                                R.string.error_network_msg);
                    } else {
                        // When connected but the server is unreachable for some unknown reason
                        showPhoneCallDialog();
                    }
                }
            }
        }.execute();
    }
}
