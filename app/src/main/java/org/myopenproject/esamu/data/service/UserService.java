package org.myopenproject.esamu.data.service;

import org.myopenproject.esamu.data.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("users")
    Call<Void> save(@Body UserDto userDto);
}
