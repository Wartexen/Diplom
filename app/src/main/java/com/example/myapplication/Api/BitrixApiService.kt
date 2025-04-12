package com.example.myapplication.Api

import com.example.myapplication.AccessTokenResponse
import com.example.myapplication.UserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BitrixApiService {
    @GET("oauth/token/")
    fun getAccessToken(
        @Query("grant_type") grant_type: String,
        @Query("code") code: String,
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String
    ): Call<AccessTokenResponse>

    @GET("user.info.json")
    fun getUserInfo(
        @Query("auth") auth: String
    ): Call<UserInfoResponse>
}