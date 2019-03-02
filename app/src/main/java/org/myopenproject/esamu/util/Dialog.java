package org.myopenproject.esamu.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.myopenproject.esamu.R;

public class Dialog {
    private Dialog() {}

    public static void toast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ((TextView)((LinearLayout)toast.getView()).getChildAt(0))
                .setGravity(Gravity.CENTER_HORIZONTAL);
        toast.show();
    }

    public static void alert(Context context, int title) {
        alert(context, title, 0, null);
    }

    public static void alert(Context context, int title, int message) {
        alert(context, title, message, null);
    }

    public static void alert(Context context, int title, int message, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setPositiveButton("OK", listener);

        if (message > 0)
            builder.setMessage(message);

        builder.show();
    }

    public static ProgressDialog makeProgress(Context context, int title, int message) {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setCancelable(false);
        progress.setTitle(R.string.dialog_wait);
        progress.setMessage(context.getString(R.string.dialog_sending));

        return progress;
    }
}