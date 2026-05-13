package com.example.tunenet.data.remote

import com.example.tunenet.data.model.DeezerSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApiService {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerSearchResponse

    companion object {
        private const val BASE_URL = "https://api.deezer.com/"

        fun create(): DeezerApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DeezerApiService::class.java)
        }
    }
}
