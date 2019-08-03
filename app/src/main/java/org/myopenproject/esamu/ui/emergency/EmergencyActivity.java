package org.myopenproject.esamu.ui.emergency;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

import org.myopenproject.esamu.App;
import org.myopenproject.esamu.R;
import org.myopenproject.esamu.data.dto.EmergencyDto;
import org.myopenproject.esamu.data.dto.UserDto;
import org.myopenproject.esamu.data.model.EmergencyGateway;
import org.myopenproject.esamu.data.model.EmergencyRecord;
import org.myopenproject.esamu.data.service.EmergencyService;
import org.myopenproject.esamu.data.service.ErrorDecoder;
import org.myopenproject.esamu.data.service.Message;
import org.myopenproject.esamu.data.service.ServiceFactory;
import org.myopenproject.esamu.ui.signup.SignUpActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.util.Permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class EmergencyActivity extends AppCompatActivity {
    private static final String TAG = "Emergency";

    // Refresh location interval in milliseconds
    private static final int NORMAL_REFRESH_LOCATION_INTERVAL = 500;
    private static final int FAST_REFRESH_LOCATION_INTERVAL = 100;

    // Countdown in seconds to wait for location after
    // the user taps the "Send" button
    private static final int MAX_REMAINING_TIME = 10;

    // Minimum acceptable accuracy in meters
    private static final float MIN_ACCURACY_ACCEPTABLE = 30.0F;

    // Emergency service phone number
    private static final String EMERGENCY_PHONE = "10331";

    // Request codes
    private static final int REQUEST_ACQUIRE_IMAGE = 3;
    private static final int REQUEST_CHECK_SETTINGS = 4;
    private static final int REQUEST_SIGN_UP = 5;

    private EmergencyService service = ServiceFactory.getInstance().create(EmergencyService.class);
    private EmergencyDto emergencyDto;
    private byte[] picture;

    // Location API
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Flags
    private boolean isLocationKnown;    // True when location provider reaches acceptable accuracy
    private boolean isBackPressed;      // True when user taps back button at least once
    private boolean isPictureTaken;     // True when picture is taken at least once

    // Views
    private ProgressDialog progress;
    private TextView tvInfoTitle;
    private TextView tvInfoMessage;
    private ImageView ivPictureTaken;
    private FloatingActionsMenu famAdd;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Prevent user to take screenshot
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        emergencyDto = new EmergencyDto();

        // Check runtime permission before getting location
        if (Permission.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            setupLocationProvider();
        }

        // Check permission to get IMEI
        if (Permission.checkPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            emergencyDto.setImei(Device.getIMEI(this));
        }

        // Set up views
        progress = Dialog.makeProgress(this, R.string.dialog_wait, R.string.dialog_sending);
        ivPictureTaken = findViewById(R.id.emergencyIvPictureTaken);
        tvInfoTitle = findViewById(R.id.emergencyTvInfoTitle);
        tvInfoMessage = findViewById(R.id.emergencyTvInfoMessage);
        famAdd = findViewById(R.id.emergencyFamAdd);
        btnSend = findViewById(R.id.emergencyBtnSend);
        btnSend.setVisibility(View.GONE);
        btnSend.setOnClickListener(v -> validate());
        findViewById(R.id.emergencyButtonCamera).setOnClickListener(v -> startCamera());
        findViewById(R.id.emergencyButtonVoice).setOnClickListener(v -> startVoice());
    }

    @Override
    protected void onStop() {
        super.onStop();
        famAdd.collapseImmediately();
    }

    @Override
    protected void onDestroy() {
        if (locationClient != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        // User should tap back button twice to exit activity
        if (isBackPressed) {
            super.onBackPressed();
        } else {
            Dialog.toast(this, getString(R.string.emergency_back_pressed));
            isBackPressed = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ACQUIRE_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    onPictureTaken(data.getStringExtra(CameraActivity.RET_IMAGE));
                }
                break;

            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK && Permission
                        .checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    startCamera();
                } else {
                    Log.w(TAG, "Fused location turned off by user");
                    suggestEnableHighAccuracyLocation();
                }
                break;

            case REQUEST_SIGN_UP:
                if (resultCode == RESULT_OK) {
                    validate();
                }
        }
    }

    private void setupLocationProvider() {
        // Get Fused Location Provider
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // This callback is called when locations start to become available
        locationCallback = new LocationCallback() {
            float mostAccurate = MIN_ACCURACY_ACCEPTABLE;

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                Log.d(TAG, "lat: " + location.getLatitude()
                        + ", lng: " + location.getLongitude()
                        + ", accuracy: " + location.getAccuracy());

                // Pick only the most accurate location
                if (location.getAccuracy() < mostAccurate) {
                    emergencyDto.setLatitude(Double.toString(location.getLatitude()));
                    emergencyDto.setLongitude(Double.toString(location.getLongitude()));
                    mostAccurate = location.getAccuracy();
                    isLocationKnown = true;
                }
            }
        };

        // Prepare location request with requirements
        locationRequest = LocationRequest.create()
                .setInterval(NORMAL_REFRESH_LOCATION_INTERVAL)
                .setFastestInterval(FAST_REFRESH_LOCATION_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requestHighAccuracyLocation();
    }

    @SuppressLint("MissingPermission")
    private void requestHighAccuracyLocation() {
        // Ensure current location settings meet requirements
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        // Location settings OK
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            startCamera();
        });

        // Location settings weren't met
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    // Try to resolve the setting by asking user to change it
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(EmergencyActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                    Log.e(TAG, "Failed to request configuration to user", e);
                }
            }
        });
    }

    private void onPictureTaken(String path) {
        File file = new File(path);

        try (FileInputStream fis = new FileInputStream(file)) {
            picture = new byte[(int) file.length()];

            if (fis.read(picture) != picture.length) {
                Log.w(TAG, "Length of byte array doesn't match image size");
            }

            if (!file.delete()) {
                Log.w(TAG, "Cannot delete image file from " + path);
            }

            // Update views as needed
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            ivPictureTaken.setImageBitmap(bitmap);
            tvInfoTitle.setText(R.string.emergency_text_ready_title);
            tvInfoMessage.setText(R.string.emergency_text_ready_msg);
            btnSend.setVisibility(View.VISIBLE);
            isPictureTaken = true;
        } catch (IOException e) {
            Log.e(TAG, "Cannot read image from " + path, e);
        }
    }

    private void startCamera() {
        Intent it = new Intent(this, CameraActivity.class);
        it.putExtra(CameraActivity.PARAM_IS_PICTURE_TAKEN, isPictureTaken);
        startActivityForResult(it, REQUEST_ACQUIRE_IMAGE);
    }

    private void startVoice() {
        Dialog.toast(this, getString(R.string.error_not_implemented));
    }

    private void validate() {
        if (!Device.isNetworkAvailable(this)) {
            Dialog.alert(this, R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        UserDto userDto = App.getInstance().getUser();

        if (userDto == null) {
            // User not registered yet
            startActivityForResult(new Intent(this, SignUpActivity.class), REQUEST_SIGN_UP);
            return;
        }

        progress.show();

        if (isLocationKnown) {
            emergencyDto.setUserId(userDto.getId());
            emergencyDto.setPicture(Base64.encodeToString(picture, Base64.DEFAULT));
            send();
        } else {
            startCountDown();
        }
    }

    private void send() {
        Call<Message> call = service.reportEmergency(emergencyDto);
        call.enqueue(new Callback<Message>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Message> call, Response<Message> response) {
                progress.dismiss();

                if (response.isSuccessful()) {
                    Log.i(TAG, "Successfully reported emergency: " + response.body());
                    save(response.body());
                    new AlertDialog.Builder(EmergencyActivity.this)
                            .setCancelable(false)
                            .setTitle(R.string.emergency_sent_title)
                            .setMessage(R.string.emergency_sent_msg)
                            .setPositiveButton(android.R.string.ok, (dialog, button) -> {
                                setResult(RESULT_OK);
                                finish();
                            })
                            .show();
                } else if (response.code() == 401) {
                    // Unauthorized. Register user again
                    Intent it = new Intent(EmergencyActivity.this, SignUpActivity.class);
                    startActivityForResult(it, REQUEST_SIGN_UP);
                } else {
                    suggestMakePhoneCall(getString(R.string.error_unavailable_title));
                    Message error = ErrorDecoder.decode(response.errorBody());
                    Log.e(TAG, "Response code " + response.code() + ". Content: " + error);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Message> call, Throwable t) {
                progress.dismiss();
                suggestMakePhoneCall(getString(R.string.error_unreachable_server));
                Log.e(TAG, "Failed to send emergency", t);
            }
        });
    }

    private void save(Message response) {
        if (response == null) {
            Log.w(TAG, "Missing response");
            return;
        }

        EmergencyRecord emergencyRecord = new EmergencyRecord();
        emergencyRecord.setStatus(EmergencyRecord.Status.PENDENT);
        Map<String, String> details = response.getDetails();

        if (details != null) {
            String id = details.get("key");

            if (id != null) {
                emergencyRecord.setId(Long.parseLong(id));
            } else {
                Log.w(TAG, "Missing emergency key");
            }

            String timestampStr = details.get("timestamp");

            if (timestampStr != null) {
                long timestamp = Long.parseLong(timestampStr);
                emergencyRecord.setDateTime(new Date(timestamp));
            } else {
                Log.w(TAG, "Missing emergency timestamp");
            }

            emergencyRecord.setLocation(details.get("location"));
            EmergencyGateway gateway = new EmergencyGateway(this);
            gateway.create(emergencyRecord);
            App.getInstance().getBus().post("refreshHistory");
        } else {
            Log.w(TAG, "Missing emergency details. It will not be stored.");
        }
    }

    private void startCountDown() {
        new CountDownTimer(MAX_REMAINING_TIME * 1000, FAST_REFRESH_LOCATION_INTERVAL) {
            String msg = getString(R.string.emergency_progress_msg);
            int secondsRemaining = MAX_REMAINING_TIME;

            @Override
            public void onTick(long millisUntilFinished) {
                if (secondsRemaining * 1000 >= millisUntilFinished) {
                    progress.setMessage(
                            String.format(msg, Integer.toString(secondsRemaining--)));
                }

                if (isLocationKnown) {
                    cancel();
                    progress.dismiss();
                    validate(); // Try again
                }
            }

            @Override
            public void onFinish() {
                progress.dismiss();
                suggestMakePhoneCall(getString(R.string.error_unkown_location));
            }
        }.start();
    }

    private void suggestMakePhoneCall(String title) {
        new AlertDialog.Builder(EmergencyActivity.this)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(R.string.emergency_dialog_suggest_call)
                .setPositiveButton(R.string.emergency_dialog_call_192, (dialog, which) -> {
                    Device.makePhoneCall(this, EMERGENCY_PHONE);
                    finish();
                })
                .setNegativeButton(R.string.dialog_retry, (dialog, which) -> validate())
                .show();
    }

    private void suggestEnableHighAccuracyLocation() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.emergency_location_accuracy_title)
                .setMessage(R.string.emergency_location_accuracy_msg)
                .setPositiveButton(R.string.emergency_location_enable_high_accuracy,
                        (dialog, which) -> requestHighAccuracyLocation())
                .setNegativeButton(R.string.dialog_finish, (dialog, which) -> finish())
                .show();
    }
}