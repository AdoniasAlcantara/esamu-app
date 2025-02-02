package org.myopenproject.esamu.data.service;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.ResponseBody;

public class ErrorDecoder {
    private static final String TAG = "ErrorDecoder";

    private ErrorDecoder() {}

    public static Message decode(ResponseBody responseBody) {
        if (responseBody == null) {
            return null;
        }

        Message error = null;

        try {
             error = new Gson().fromJson(responseBody.string(), Message.class);
        } catch (JsonSyntaxException | IOException e) {
            Log.w(TAG, "Failed to decode error message", e);
        }

        return error;
    }
}
