package com.onesandzeros.patima;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/users/")
    Call<UserResponse> createUser(@Body User user);

    @GET("api/users/login/{email}/{password}/True")
    Call<LoginResponse> checkUserLogin(@Path("email") String email, @Path("password") String password);

}

