package org.myopenproject.esamu.ui.emergency;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.myopenproject.esamu.R;
import org.myopenproject.esamu.util.Device;
import org.myopenproject.esamu.util.Dialog;
import org.myopenproject.esamu.util.Image;
import org.myopenproject.esamu.util.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "Camera";

    // If this is the first time user take the photo
    public static final String PARAM_IS_PICTURE_TAKEN = "isPictureTaken";

    // Returned after picture is taken
    public static final String RET_IMAGE = "pictureTaken";

    // Allowed resolutions range
    private static final int RESOLUTION_MAX = 1280;
    private static final int RESOLUTION_MIN = 640;

    private Camera camera;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_camera);

        // Prevent user to take screenshot
        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);*/

        // Check runtime permission
        if (Permission.checkPermission(this, Manifest.permission.CAMERA)) {
            setupCamera();
        }

        // Set up button event
        ImageButton btnTakePicture = findViewById(R.id.cameraBtnTakePicture);
        btnTakePicture.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);

            // Prevent user from tapping button more than once
            btnTakePicture.setEnabled(false);

            // Prevent user from rotating device while the picture is being taking
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

            // Camera callbacks
            camera.takePicture(null, null, (data, cam) -> {
                camera.release();
                onPictureTaken(data);
            });
        });

        progress = findViewById(R.id.cameraProgress);

        // Show a dialog when the photo is first taken
        boolean isPictureTaken = getIntent().getBooleanExtra(PARAM_IS_PICTURE_TAKEN, false);

        if (!isPictureTaken) {
            Dialog.alert(this, R.string.camera_dialog_title, R.string.camera_dialog_msg);
        }
    }

    @Override
    protected void onDestroy() {
        if (camera != null) {
            camera.release();
        }

        super.onDestroy();
    }

    private void setupCamera() {
        camera = Device.getCamera(this);
        Camera.Parameters params = camera.getParameters();

        // Set camera resolution
        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        for (Camera.Size s : sizes) {
            if (s.width <= RESOLUTION_MAX && s.width >= RESOLUTION_MIN) {
                params.setPictureSize(s.width, s.height);
                Log.d(TAG, "Chosen resolution: " + s.width + " x " + s.height);
                break;
            }
        }

        // Install preview in the camera
        CameraPreview preview = new CameraPreview(this, camera);
        ViewGroup previewHolder = findViewById(R.id.cameraPreview);
        previewHolder.addView(preview);
    }

    private void onPictureTaken(byte[] data) {
        new Thread(() -> {
            // Get current rotation and unlock screen orientation
            int rotation = Device.getRotation(this);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

            Log.d(TAG, "Image bytes: " + data.length);

            // Fix image rotation on Samsung and Sony devices
            Bitmap bitmap = Image.fixRotation(data, rotation);

            try {
                // Save it and return the resulting image path
                String path = tempImage(bitmap, "picture");
                Intent result = new Intent();
                result.putExtra(RET_IMAGE, path);
                setResult(RESULT_OK, result);
            } catch (IOException e) {
                Log.e(TAG, "Cannot save temp image", e);
            }

            runOnUiThread(this::finish);
        }).run();
    }

    // Create temporary file and return the absolute file path
    public String tempImage(Bitmap bitmap, String name) throws IOException {
        File outputDir = getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");
        OutputStream os = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os);
        os.flush();
        os.close();

        return imageFile.getAbsolutePath();
    }
}
