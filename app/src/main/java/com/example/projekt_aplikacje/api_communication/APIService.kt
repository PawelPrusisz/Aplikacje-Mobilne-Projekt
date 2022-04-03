package com.example.projekt_aplikacje.api_communication

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class APIService {
    companion object {
        val api: RecipeEndpoints = Retrofit.Builder()
            .baseUrl(API.URL).addConverterFactory(MoshiConverterFactory.create())
            .build().create(RecipeEndpoints::class.java)
    }
}