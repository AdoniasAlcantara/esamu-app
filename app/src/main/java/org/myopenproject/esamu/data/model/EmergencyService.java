package org.myopenproject.esamu.data.model;

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
    // Webservice URL
    private static final String SERVICE = "http://nbcgib.uesc.br/esamu/service";
    //private static final String SERVICE = "http://192.168.1.10:8080/esamu/service";

    // Resource identifiers
    private static final String SIGN_UP = "/users";
    private static final String REPORT = "/emergencies";

    // The timeout of connection in milliseconds
    private static final int TIMEOUT = 30000;

    private Gson gson;

    public EmergencyService() {
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    // Request to register user
    public ResponseDto signUp(UserDto user) throws IOException {
        return sendJson(user, SERVICE + SIGN_UP);
    }

    // Request to ask for help
    public ResponseDto report(EmergencyDto emergency) throws IOException {
        return sendJson(emergency, SERVICE + REPORT);
    }

    private ResponseDto sendJson(Object obj, String url) throws IOException {
        // Create HTTP connection
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setReadTimeout(TIMEOUT);
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
