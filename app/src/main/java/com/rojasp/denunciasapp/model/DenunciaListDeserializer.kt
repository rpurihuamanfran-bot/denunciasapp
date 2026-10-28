package com.rojasp.denunciasapp.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class DenunciaListDeserializer : JsonDeserializer<List<Denuncia>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<Denuncia> {
        val list = mutableListOf<Denuncia>()
        try {
            val jsonArray = when {
                json.isJsonArray -> json.asJsonArray
                json.isJsonObject && json.asJsonObject.has("content") && json.asJsonObject.get("content").isJsonArray -> json.asJsonObject.getAsJsonArray("content")
                json.isJsonObject && json.asJsonObject.has("data") && json.asJsonObject.get("data").isJsonArray -> json.asJsonObject.getAsJsonArray("data")
                else -> null
            }

            if (jsonArray != null) {
                for (element in jsonArray) {
                    try {
                        val denuncia = context.deserialize<Denuncia>(element, Denuncia::class.java)
                        if (denuncia != null) {
                            list.add(denuncia)
                        }
                    } catch (e: Exception) {
                        try {
                            if (element.isJsonObject) {
                                val obj = element.asJsonObject
                                val id = if (obj.has("id") && !obj.get("id").isJsonNull) {
                                    try { obj.get("id").asInt } catch (_: Exception) { null }
                                } else null

                                val dni = if (obj.has("dni") && !obj.get("dni").isJsonNull) obj.get("dni").asString else ""
                                val fecha = if (obj.has("fecha") && !obj.get("fecha").isJsonNull) obj.get("fecha").asString else ""
                                val tipo = if (obj.has("tipo") && !obj.get("tipo").isJsonNull) obj.get("tipo").asString else ""
                                val direccion = if (obj.has("direccion") && !obj.get("direccion").isJsonNull) obj.get("direccion").asString else ""
                                val descripcion = if (obj.has("descripcion") && !obj.get("descripcion").isJsonNull) obj.get("descripcion").asString else ""
                                val estado = if (obj.has("estado") && !obj.get("estado").isJsonNull) {
                                    try { obj.get("estado").asInt } catch (_: Exception) { 1 }
                                } else 1

                                list.add(Denuncia(id, dni, fecha, tipo, direccion, descripcion, estado))
                            }
                        } catch (e2: Exception) {
                            e2.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
