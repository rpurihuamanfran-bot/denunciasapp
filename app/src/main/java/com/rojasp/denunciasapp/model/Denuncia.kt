package com.rojasp.denunciasapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Denuncia(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("dni")
    val dni: String? = "",

    @SerializedName("fecha")
    val fecha: String? = "",

    @SerializedName("tipo")
    val tipo: String? = "",

    @SerializedName("direccion")
    val direccion: String? = "",

    @SerializedName("descripcion")
    val descripcion: String? = "",

    @SerializedName("estado")
    var estado: Int? = 1
) : Serializable
