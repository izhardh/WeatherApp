package com.example.myapplication.network

import com.example.myapplication.models.WheaterResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WheaterService {

    @GET("2.5/weather")
    fun getWheater(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?,
    ) : Call<WheaterResponse>
}