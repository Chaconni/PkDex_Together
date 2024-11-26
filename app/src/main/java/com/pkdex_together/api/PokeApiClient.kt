package com.pkdex_together.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object PokeApiClient {
    private const val BASE_URL = "https://pokeapi.co/api/v2/" //enlace de la api

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}