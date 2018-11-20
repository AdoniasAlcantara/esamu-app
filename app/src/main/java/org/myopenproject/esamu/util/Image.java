package org.myopenproject.esamu.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Surface;

public class Image {
    public static Bitmap fixRotation(byte[] image, int rotation) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        return fixRotation(bitmap, rotation);
    }

    public static Bitmap fixRotation(Bitmap image, int rotation) {
        if (image.getWidth() > image.getHeight()) {
            switch (rotation) {
                case Surface.ROTATION_0:    // Portrait
                    rotation = 90;
                    break;

                case Surface.ROTATION_180:  // Reverse portrait
                    rotation = 270;
                    break;

                case Surface.ROTATION_270:  // Reverse landscape
                    rotation = 180;
                    break;

                default:
                    rotation = 0;
            }
        }

        // Fix image rotation
        if (rotation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            image = Bitmap.createBitmap(image, 0, 0,
                    image.getWidth(), image.getHeight(), matrix, true);
        }

        return image;
    }
}
