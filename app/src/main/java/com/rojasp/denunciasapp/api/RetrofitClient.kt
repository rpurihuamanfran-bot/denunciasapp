package com.rojasp.denunciasapp.api

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rojasp.denunciasapp.model.Denuncia
import com.rojasp.denunciasapp.model.DenunciaListDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://denunciaservice-i6io.onrender.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")

                if (original.body != null && original.header("Content-Type") == null) {
                    requestBuilder.header("Content-Type", "application/json")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    private val listType = object : TypeToken<List<Denuncia>>() {}.type

    private val gson = GsonBuilder()
        .registerTypeAdapter(listType, DenunciaListDeserializer())
        .setLenient()
        .create()

    val instance: DenunciaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DenunciaApiService::class.java)
    }
}
