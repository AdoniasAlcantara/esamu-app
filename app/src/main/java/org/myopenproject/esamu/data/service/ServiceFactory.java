package org.myopenproject.esamu.data.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceFactory {
    private static final String BASE_URL = "http://10.0.0.253:8080/esamu/api/";

    private static ServiceFactory instance;
    private Retrofit retrofit;

    private ServiceFactory() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }

        return instance;
    }

    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }
}
