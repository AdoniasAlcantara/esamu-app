package org.myopenproject.esamu.ui.emergency;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
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

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.common.EmergencyDto;
import org.myopenproject.esamu.common.ResponseDto;
import org.myopenproject.esamu.common.UserDto;
import org.myopenproject.esamu.App;
import org.myopenproject.esamu.data.model.EmergencyGateway;
import org.myopenproject.esamu.data.model.EmergencyRecord;
import org.myopenproject.esamu.data.model.EmergencyService;
import org.myopenproject.esamu.ui.signup.SignUpActivity;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.util.Permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

@SuppressLint("MissingPermission")
public class EmergencyActivity extends AppCompatActivity
{
    private static final String TAG = "EMERGENCY";

    // Location refresh interval in milliseconds
    private static final int REFRESH_LOCATION_INTERVAL = 100;

    // Countdown in seconds to wait for location after
    // the user taps the "Send" button
    private static final int MAX_REMAINING_TIME = 10;

    // Minimum acceptable accuracy in meters
    private static final float MIN_ACCURACY_ACCEPTABLE = 30.0f;

    // Request codes
    private static final int REQUEST_LOCATION_PERMISSION = 999;
    private static final int REQUEST_IMEI_PERMISSION = 998;
    private static final int REQUEST_ACQUIRE_IMAGE = 997;
    private static final int REQUEST_CHECK_SETTINGS = 996;
    private static final int REQUEST_SIGN_UP = 995;

    private EmergencyDto eDto;
    private byte[] picture;

    // Location API objects
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Flags
    private boolean isLocationKnown;    // True when location provider reaches acceptable accuracy
    private boolean isBackPressed;      // True when user taps back button at least once
    private boolean isPictureTaken;     // True when picture is taken at least once

    private CountDownTimer timer;

    // Views
    private ProgressDialog progress;
    private TextView labelTitle;
    private TextView labelMessage;
    private ImageView imagePictureTaken;
    private FloatingActionsMenu buttonAdd;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Prevent user to take screenshot
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE);

        // Create an emergency instance as soon as possible
        // as it will be hydrated gradually
        eDto = new EmergencyDto();

        // Check runtime permission before getting location
        if (Permission.validate(this, REQUEST_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            setupLocationProvider();
        }

        // Check permission to get IMEI
        if (Permission.validate(this, REQUEST_IMEI_PERMISSION,
            Manifest.permission.READ_PHONE_STATE))
        {
            eDto.setImei(Device.getIMEI(this));
        }

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

        // Start camera activity on the fly
        startCamera();
    }

    @Override
    protected void onStop()
    {
        buttonAdd.collapseImmediately();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        if (locationClient != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    @Override
    public void onBackPressed()
    {
        // User should tap back button twice to exit activity
        if (isBackPressed) {
            super.onBackPressed();
        } else {
            Dialog.toast(this, getString(R.string.emergency_toast_back_pressed));
            isBackPressed = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch (requestCode) {
            case REQUEST_ACQUIRE_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    onPictureTaken(data.getStringExtra(CameraActivity.RET_IMAGE));
                }
                break;

            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    Log.w(TAG, "Fused location turned off by user");
                    // TODO show a message to user, maybe?
                    finish();
                }
                break;

            case REQUEST_SIGN_UP:
                if (resultCode == RESULT_OK) {
                    send();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length == 0) {
            return;
        }

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    setupLocationProvider();
                }
                break;

            case REQUEST_IMEI_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    eDto.setImei(Device.getIMEI(this));
                }
        }
    }

    private void setupLocationProvider()
    {
        // Get location provider
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Prepare location request
        locationRequest = LocationRequest.create()
            .setInterval(REFRESH_LOCATION_INTERVAL)
            .setFastestInterval(100)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Check whether the current location settings are satisfied
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build());

        // This callback is called when locations start to become available
        locationCallback = new LocationCallback()
        {
            float mostAccurate = MIN_ACCURACY_ACCEPTABLE;

            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                Log.d(TAG, "lat: " + location.getLatitude()
                    + ", lng: " + location.getLongitude()
                    + ", accuracy: " + location.getAccuracy());

                // Pick only the most accurate location
                if (location.getAccuracy() < mostAccurate) {
                    eDto.setLatitude(Double.toString(location.getLatitude()));
                    eDto.setLongitude(Double.toString(location.getLongitude()));
                    mostAccurate = location.getAccuracy();
                    isLocationKnown = true;
                }
            }
        };

        // Location settings ok
        task.addOnSuccessListener(this, locationSettingsResponse ->
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null));

        // Location settings are not satisfied
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException rae = ((ResolvableApiException) e);
                    rae.startResolutionForResult(EmergencyActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                    Log.e(TAG, "Failed to request configuration change to user");
                }
            }
        });
    }

    private void onPictureTaken(String path)
    {
        File file = new File(path);

        try {
            FileInputStream fis = new FileInputStream(file);
            picture = new byte[(int) file.length()];

            if (fis.read(picture) != picture.length) {
                Log.w(TAG, "Length of byte array doesn't match image size");
            }

            fis.close();

            if (!file.delete()) {
                Log.w(TAG, "Cannot delete image file from " + path);
            }

            // Update views as needed
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            imagePictureTaken.setImageBitmap(bitmap);
            labelTitle.setText(R.string.emergency_text_ready_title);
            labelMessage.setText(R.string.emergency_text_ready_msg);
            buttonSend.setVisibility(View.VISIBLE);
            isPictureTaken = true;
        } catch (IOException e) {
            Log.e(TAG, "Cannot read image from " + path, e);
        }
    }

    private void startCamera()
    {
        Intent it = new Intent(this, CameraActivity.class);
        it.putExtra(CameraActivity.PARAM_IS_PICTURE_TAKEN, isPictureTaken);
        startActivityForResult(it, REQUEST_ACQUIRE_IMAGE);
    }

    private void startVoice()
    {
        Dialog.toast(this, getString(R.string.error_not_implemented));
    }

    private void showPhoneCallDialog(String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(EmergencyActivity.this);
        builder.setTitle(title);
        builder.setMessage(R.string.emergency_dialog_suggest_call);

        builder.setPositiveButton(
            R.string.emergency_dialog_call_192,
            (dialog, which) -> {
                Device.doPhoneCall(this, "10331");
                finish();
            });

        builder.setNegativeButton(R.string.dialog_retry, (dialog, which) -> send());
        builder.show();
    }

    private void send()
    {
        if (!Device.isNetworkAvailable(this)) {
            Dialog.alert(this, R.string.error_network_title, R.string.error_network_msg);
            return;
        }

        // TODO
        UserDto uDto = new UserDto();

        if (uDto == null) {
            // User not yet registered
            startActivityForResult(new Intent(this, SignUpActivity.class), REQUEST_SIGN_UP);
            return;
        }

        progress = Dialog.makeProgress(
            this,
            R.string.dialog_wait,
            R.string.dialog_sending);

        progress.show();

        if (isLocationKnown) {
            // Stop location provider
            locationClient.removeLocationUpdates(locationCallback);
            eDto.setUserId(uDto.getId());
            eDto.setPicture(Base64.encodeToString(picture, Base64.DEFAULT));
            doReportEmergencyTask();
        } else {
            if (timer == null) {
                timer = new CountDownTimer(MAX_REMAINING_TIME * 1000, REFRESH_LOCATION_INTERVAL)
                {
                    String msg = getString(R.string.emergency_progress_msg);
                    int secondsRemaining = MAX_REMAINING_TIME;

                    @Override
                    public void onTick(long millisUntilFinished)
                    {
                        if (secondsRemaining * 1000 >= millisUntilFinished) {
                            progress.setMessage(
                                String.format(msg, Integer.toString(secondsRemaining--))
                            );
                        }

                        if (isLocationKnown) {
                            cancel();
                            progress.dismiss();
                            send(); // Try again
                        }
                    }

                    @Override
                    public void onFinish()
                    {
                        progress.dismiss();
                        showPhoneCallDialog(getString(R.string.error_unkown_location));
                    }
                };
            }

            timer.start();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void doReportEmergencyTask()
    {
        new AsyncTask<Void, Void, ResponseDto>()
        {
            @Override
            protected ResponseDto doInBackground(Void... voids)
            {
                ResponseDto response = null;

                try {
                    response = new EmergencyService().report(eDto);
                } catch (Throwable e) {
                    Log.e(TAG, "Error sending eDto", e);
                }

                return response;
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            protected void onPostExecute(ResponseDto response)
            {
                progress.dismiss();

                if (response != null) {
                    if (response.getStatusCode() == 201) {
                        // 201: CREATED
                        Log.i(TAG, "Emergency sent: " + eDto);

                        // Save emergency to storage
                        EmergencyRecord eRecord = new EmergencyRecord();
                        eRecord.setId(Long.parseLong(response.getDetails().get("key")));
                        eRecord.setStatus(EmergencyRecord.Status.PENDENT);

                        Address addr = Device.getAddress(
                            EmergencyActivity.this,
                            Double.parseDouble(eDto.getLatitude()),
                            Double.parseDouble(eDto.getLongitude()));

                        if (addr != null) {
                            eRecord.setLocation(addr.getAddressLine(0));
                        }

                        long timestamp = Long.parseLong(response.getDetails().get("timestamp"));
                        eRecord.setDateTime(new Date(timestamp));

                        EmergencyGateway gateway = new EmergencyGateway(EmergencyActivity.this);
                        gateway.create(eRecord);
                        App.getInstance().getBus().post("refreshHistory");

                        Dialog.alert(
                            EmergencyActivity.this,
                            R.string.emergency_dialog_sent_title,
                            R.string.emergency_dialog_sent_msg,
                            (dialog, which) -> {
                                setResult(RESULT_OK);
                                finish();
                            });
                    } else {
                        Log.e(TAG, response.toString());

                        if (response.getStatusCode() >= 500) {
                            // 5xx: SERVER FAULT
                            Dialog.alert(
                                EmergencyActivity.this,
                                R.string.error_unavailable_title,
                                R.string.error_unavailable_msg);
                        } else if (response.getStatusCode() == 401) {
                            // 401: UNAUTHORIZED
                            startActivity(new Intent(EmergencyActivity.this, SignUpActivity.class));
                        } else {
                            Dialog.alert(
                                EmergencyActivity.this,
                                R.string.error_unknown,
                                R.string.error_unknown,
                                (dialog, which) -> finish());
                        }
                    }
                } else {
                    if (!Device.isNetworkAvailable(EmergencyActivity.this)) {
                        // When there is no connectivity on device
                        Dialog.alert(
                            EmergencyActivity.this,
                            R.string.error_network_title,
                            R.string.error_network_msg);
                    } else {
                        // When connected but the server is unreachable for some unknown reason
                        showPhoneCallDialog(getString(R.string.error_unavailable_title));
                    }
                }
            }
        }.execute();
    }
}
