package com.rojasp.denunciasapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rojasp.denunciasapp.api.RetrofitClient
import com.rojasp.denunciasapp.databinding.ActivityDetalleBinding
import com.rojasp.denunciasapp.model.Denuncia
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleBinding
    private var denuncia: Denuncia? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        obtenerExtraData()
        mostrarDatos()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbarDetalle.setNavigationOnClickListener {
            finish()
        }
    }

    private fun obtenerExtraData() {
        denuncia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DENUNCIA_EXTRA", Denuncia::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("DENUNCIA_EXTRA") as? Denuncia
        }
    }

    private fun mostrarDatos() {
        val item = denuncia ?: return

        with(binding) {
            tvDetalleId.text = "Denuncia #${item.id ?: "N/A"}"
            tvDetalleTipo.text = item.tipo
            tvDetalleDni.text = item.dni
            tvDetalleFecha.text = item.fecha
            tvDetalleDireccion.text = item.direccion
            tvDetalleDescripcion.text = item.descripcion

            actualizarVistaEstado(item.estado ?: 1)
        }
    }

    private fun actualizarVistaEstado(estado: Int) {
        with(binding) {
            if (estado == 1) {
                tvDetalleEstado.text = "PENDIENTE"
                tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_pending)
                tvDetalleEstado.setTextColor(
                    ContextCompat.getColor(this@DetalleActivity, R.color.badge_pending_text)
                )
                btnAtender.visibility = View.VISIBLE
                btnAtender.isEnabled = true
            } else {
                tvDetalleEstado.text = "ATENDIDO"
                tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_attended)
                tvDetalleEstado.setTextColor(
                    ContextCompat.getColor(this@DetalleActivity, R.color.badge_attended_text)
                )
                btnAtender.visibility = View.GONE
            }
        }
    }

    private fun setupButtons() {
        binding.btnAtender.setOnClickListener {
            val id = denuncia?.id ?: return@setOnClickListener
            atenderDenuncia(id)
        }
    }

    private fun atenderDenuncia(id: Int) {
        binding.progressBarDetalle.visibility = View.VISIBLE
        binding.btnAtender.isEnabled = false

        RetrofitClient.instance.atenderDenuncia(id)
            .enqueue(object : Callback<Denuncia> {
                override fun onResponse(call: Call<Denuncia>, response: Response<Denuncia>) {
                    binding.progressBarDetalle.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val denunciaActualizada = response.body()!!
                        denuncia = denunciaActualizada
                        actualizarVistaEstado(denunciaActualizada.estado ?: 0)

                        Toast.makeText(
                            this@DetalleActivity,
                            "¡Denuncia marcada como atendida!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        binding.btnAtender.isEnabled = true
                        Toast.makeText(
                            this@DetalleActivity,
                            "Error al atender la denuncia: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Denuncia>, t: Throwable) {
                    binding.progressBarDetalle.visibility = View.GONE
                    binding.btnAtender.isEnabled = true
                    Toast.makeText(
                        this@DetalleActivity,
                        "Error de red: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
