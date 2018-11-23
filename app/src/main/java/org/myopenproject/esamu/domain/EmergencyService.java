package org.myopenproject.esamu.domain;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.myopenproject.esamu.common.EmergencyDto;
import org.myopenproject.esamu.common.ResponseDto;
import org.myopenproject.esamu.common.UserDto;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EmergencyService {
    private static final String SERVICE = "http://192.168.1.250:8080/esamu/service";
    private static final String SIGNUP = "/users";
    private static final String REPORT = "/emergencies";
    private static final String STATUS = "/emergencies";

    private Gson gson;

    public EmergencyService() {
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public ResponseDto signUp(UserDto user) throws IOException {
        return sendJson(user, SERVICE + SIGNUP);
    }

    public ResponseDto report(EmergencyDto emergency) throws IOException {
        return sendJson(emergency, SERVICE + REPORT);
    }

    private ResponseDto sendJson(Object obj, String url) throws IOException {
        // Create http connection
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        String body = gson.toJson(obj); // Object to Json

        // Send object
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
        writer.write(body);
        writer.close();

        // Get response
        InputStreamReader reader;

        if (conn.getResponseCode() < 400)
            reader = new InputStreamReader(conn.getInputStream());
        else
            reader = new InputStreamReader(conn.getErrorStream());

        ResponseDto response = gson.fromJson(reader, ResponseDto.class);
        reader.close();

        return response;
    }
}
