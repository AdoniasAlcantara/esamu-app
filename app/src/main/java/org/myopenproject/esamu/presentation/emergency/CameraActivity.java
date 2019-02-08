package org.myopenproject.esamu.presentation.emergency;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.util.Image;
import org.myopenproject.esamu.util.Permission;

import java.io.ByteArrayOutputStream;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {
    public static final String IMAGE_EXTRA = "pictureTaken";
    private static final int CAMERA_PERMISSION_REQUEST = 999;
    private Camera camera;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_camera);

        if (Permission.validate(this, CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA))
            initCamera();

        // Set button event
        ImageButton buttonTakePicture = findViewById(R.id.cameraButtonTakePicture);
        buttonTakePicture.setOnClickListener(v -> {
            buttonTakePicture.setEnabled(false);
            camera.takePicture(null, null, (data, cam) -> {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                onPictureTaken(data);
            });
        });

        boolean isPictureTaken = getIntent()
                .getBooleanExtra(EmergencyActivity.IS_PICTURE_TAKEN_PARAM, false);

        if (!isPictureTaken)
            Dialog.alert(this, R.string.camera_dialog_title, R.string.camera_dialog_msg);
    }

    @Override
    protected void onDestroy() {
        if (camera != null)
            camera.release();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else {
            Dialog.alert(this,
                    R.string.error_permission_title,
                    R.string.error_permission_camera,
                    (dialog, which) -> finish());
        }
    }

    private void initCamera() {
        camera = Device.getCamera(this);
        CameraPreview preview = new CameraPreview(this, camera);
        ViewGroup layoutPreview = findViewById(R.id.cameraLayoutPreview);
        layoutPreview.addView(preview);
    }

    private void onPictureTaken(byte[] data) {
        int rotation = Device.getRotation(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

        // Fix image rotation on Samsung and Sony devices
        Bitmap bitmap = Image.fixRotation(data, rotation);

        // Convert bitmap into JPEG format
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
        data = bStream.toByteArray();

        // Return the resulting image
        Intent result = new Intent();
        result.putExtra(IMAGE_EXTRA, data);
        setResult(RESULT_OK, result);
        finish();
    }
}
