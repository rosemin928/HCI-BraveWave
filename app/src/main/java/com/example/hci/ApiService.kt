package com.example.hci

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/get-p-value")
    fun getPValue(): Call<PValueResponse>
}