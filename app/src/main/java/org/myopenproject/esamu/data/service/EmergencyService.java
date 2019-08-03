package org.myopenproject.esamu.data.service;

import org.myopenproject.esamu.data.dto.EmergencyDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface EmergencyService {
    @POST("emergencies")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<Message> reportEmergency(@Body EmergencyDto emergencyDto);
}
