package org.myopenproject.esamu.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Device {
    private Device() {
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void vibrate(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator())
            vibrator.vibrate(milliseconds);
    }

    public static Point getWindowSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);

        return size;
    }

    public static int getRotation(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getRotation();
    }

    @SuppressWarnings("deprecation")
    public static Camera getCamera(Activity activity) {
        return getCamera(activity, 0);
    }

    @SuppressWarnings("deprecation")
    public static Camera getCamera(Activity activity, int id) {
        Camera camera = Camera.open(id);
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
        return camera;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
    }

    public static Address getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address addr = null;

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses.size() > 0)
                addr = addresses.get(0);
        } catch (IOException ignored) {}

        return addr;
    }

    public static void doPhoneCall(Activity activity, String phoneNumber) {
        Intent it = new Intent();
        it.setAction(Intent.ACTION_CALL);
        it.setData(Uri.parse("tel:" + phoneNumber));
        activity.startActivity(it);
    }
}
