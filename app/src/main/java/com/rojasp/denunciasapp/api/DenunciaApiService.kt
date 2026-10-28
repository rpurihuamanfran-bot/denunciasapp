package com.rojasp.denunciasapp.api

import com.rojasp.denunciasapp.model.Denuncia
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DenunciaApiService {

    @GET("denuncias/")
    fun obtenerDenuncias(): Call<List<Denuncia>>

    @GET("denuncias")
    fun obtenerDenunciasSinSlash(): Call<List<Denuncia>>

    @GET("denuncias/")
    fun obtenerDenunciasPaginadas(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Call<List<Denuncia>>

    @POST("denuncias/")
    fun crearDenuncia(@Body denuncia: Denuncia): Call<Denuncia>

    @POST("denuncias")
    fun crearDenunciaSinSlash(@Body denuncia: Denuncia): Call<Denuncia>

    @GET("denuncias/{id}")
    fun obtenerDenunciaPorId(@Path("id") id: Int): Call<Denuncia>

    @GET("denuncias/ciudadano/{dni}")
    fun obtenerDenunciasPorDni(@Path("dni") dni: String): Call<List<Denuncia>>

    @PUT("denuncias/{id}")
    fun atenderDenuncia(@Path("id") id: Int): Call<Denuncia>
}
