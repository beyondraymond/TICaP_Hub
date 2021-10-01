package com.cyberace.ticaphub

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    val api: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl) //add the specific url for this
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }
}