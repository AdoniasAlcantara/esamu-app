package org.myopenproject.esamu.presentation.emergency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;

@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private static final String TAG = "CAMERA";
    private Camera camera;

    public CameraPreview(Context context, Camera camera)
    {
        super(context);
        this.camera = camera;

        // Install SurfaceHolder callbacks
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (holder.getSurface() == null) {
            return; // Preview surface does not exist
        }

        // Stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // Ignore
        }

        // Setup aspect ratio 4:3
        if (width < height) // Portrait
        {
            setLayoutParams(new FrameLayout.LayoutParams(width, (int) (width * 1.33f)));
        } else // Landscape
        {
            setLayoutParams(new FrameLayout.LayoutParams((int) (height * 1.33), height));
        }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview.", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }
}
