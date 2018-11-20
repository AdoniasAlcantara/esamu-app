package org.myopenproject.esamu.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {
    private Permission() {}

    public static boolean validate(Activity activity, int requestCode, String... permissions) {
        List<String> list = new ArrayList<>();

        for (String permission : permissions) {
            if (!checkPermission(activity, permission))
                list.add(permission);
        }

        if (list.isEmpty())
            return true;

        String[] newPermissions = new String[list.size()];
        list.toArray(newPermissions);
        ActivityCompat.requestPermissions(activity, newPermissions, requestCode);

        return false;
    }

    public static boolean checkPermission(Activity activity, String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
