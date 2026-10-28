package com.rojasp.denunciasapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rojasp.denunciasapp.R
import com.rojasp.denunciasapp.databinding.ItemDenunciaBinding
import com.rojasp.denunciasapp.model.Denuncia

class DenunciaAdapter(
    private var listaDenuncias: MutableList<Denuncia>,
    private val onItemClick: (Denuncia) -> Unit
) : RecyclerView.Adapter<DenunciaAdapter.DenunciaViewHolder>() {

    class DenunciaViewHolder(val binding: ItemDenunciaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenunciaViewHolder {
        val binding = ItemDenunciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DenunciaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DenunciaViewHolder, position: Int) {
        val denuncia = listaDenuncias[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvTipo.text = denuncia.tipo ?: "INCIDENTE"
            tvDni.text = "👤 DNI: ${denuncia.dni ?: "N/A"}"
            tvFecha.text = "📅 ${denuncia.fecha ?: ""}"
            tvDireccion.text = "📍 ${denuncia.direccion ?: ""}"
            tvDescripcion.text = denuncia.descripcion ?: ""

            val estadoVal = denuncia.estado ?: 1
            if (estadoVal == 0) {
                tvEstado.text = "ATENDIDO"
                tvEstado.setBackgroundResource(R.drawable.bg_badge_attended)
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.badge_attended_text))
            } else {
                tvEstado.text = "PENDIENTE"
                tvEstado.setBackgroundResource(R.drawable.bg_badge_pending)
                tvEstado.setTextColor(ContextCompat.getColor(context, R.color.badge_pending_text))
            }

            root.setOnClickListener {
                onItemClick(denuncia)
            }
        }
    }

    override fun getItemCount(): Int = listaDenuncias.size

    fun actualizarLista(nuevasDenuncias: List<Denuncia>) {
        if (this.listaDenuncias !== nuevasDenuncias) {
            this.listaDenuncias.clear()
            this.listaDenuncias.addAll(nuevasDenuncias)
        }
        notifyDataSetChanged()
    }
}
