package com.rojasp.denunciasapp.model

object DenunciaCache {
    val denunciasLocales = mutableListOf<Denuncia>()

    fun agregar(denuncia: Denuncia) {
        if (!denunciasLocales.contains(denuncia)) {
            denunciasLocales.add(0, denuncia)
        }
    }

    fun actualizarEstado(id: Int?, nuevoEstado: Int) {
        if (id == null) return
        denunciasLocales.find { it.id == id }?.let {
            it.estado = nuevoEstado
        }
    }
}
