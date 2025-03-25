package br.edu.infnet.atv.one.tasklist.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import br.edu.infnet.atv.one.tasklist.services.WeatherApiService
object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}
