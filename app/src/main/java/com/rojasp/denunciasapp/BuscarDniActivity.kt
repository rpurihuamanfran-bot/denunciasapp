package com.rojasp.denunciasapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojasp.denunciasapp.adapter.DenunciaAdapter
import com.rojasp.denunciasapp.api.RetrofitClient
import com.rojasp.denunciasapp.databinding.ActivityBuscarDniBinding
import com.rojasp.denunciasapp.model.Denuncia
import com.rojasp.denunciasapp.model.DenunciaCache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuscarDniActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscarDniBinding
    private lateinit var adapter: DenunciaAdapter
    private val listaResultados = mutableListOf<Denuncia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarDniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbarBuscar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = DenunciaAdapter(listaResultados) { denuncia ->
            val intent = Intent(this, DetalleActivity::class.java).apply {
                putExtra("DENUNCIA_EXTRA", denuncia)
            }
            startActivity(intent)
        }
        binding.rvBuscarResultados.layoutManager = LinearLayoutManager(this)
        binding.rvBuscarResultados.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnBuscar.setOnClickListener {
            buscarPorDni()
        }
    }

    private fun buscarPorDni() {
        val dniBuscado = binding.etBuscarDni.text.toString().trim()

        if (dniBuscado.isEmpty()) {
            Toast.makeText(this, "Ingrese un número de DNI", Toast.LENGTH_SHORT).show()
            binding.etBuscarDni.error = "Ingrese un número de DNI"
            return
        }

        binding.progressBarBuscar.visibility = View.VISIBLE
        binding.btnBuscar.isEnabled = false
        binding.tvBuscarEmpty.visibility = View.GONE

        // Intento 1: Consultar endpoint directo de ciudadano
        RetrofitClient.instance.obtenerDenunciasPorDni(dniBuscado)
            .enqueue(object : Callback<List<Denuncia>> {
                override fun onResponse(
                    call: Call<List<Denuncia>>,
                    response: Response<List<Denuncia>>
                ) {
                    val directas = response.body() ?: emptyList()
                    if (response.isSuccessful && directas.isNotEmpty()) {
                        binding.progressBarBuscar.visibility = View.GONE
                        binding.btnBuscar.isEnabled = true
                        mostrarResultados(directas, dniBuscado)
                    } else {
                        // Intento 2: Consultar lista general y filtrar flexiblemente
                        consultarListaGeneralYFiltrar(dniBuscado)
                    }
                }

                override fun onFailure(call: Call<List<Denuncia>>, t: Throwable) {
                    consultarListaGeneralYFiltrar(dniBuscado)
                }
            })
    }

    private fun consultarListaGeneralYFiltrar(dniBuscado: String) {
        RetrofitClient.instance.obtenerDenuncias().enqueue(object : Callback<List<Denuncia>> {
            override fun onResponse(
                call: Call<List<Denuncia>>,
                response: Response<List<Denuncia>>
            ) {
                binding.progressBarBuscar.visibility = View.GONE
                binding.btnBuscar.isEnabled = true

                val todas = mutableListOf<Denuncia>()
                if (response.isSuccessful && response.body() != null) {
                    todas.addAll(response.body()!!)
                }

                for (local in DenunciaCache.denunciasLocales) {
                    val yaExiste = todas.any {
                        (it.id != null && it.id == local.id) ||
                        (it.dni == local.dni && it.fecha == local.fecha)
                    }
                    if (!yaExiste) {
                        todas.add(0, local)
                    }
                }

                val dniLimpio = dniBuscado.replace(" ", "")
                val filtradas = todas.filter { denuncia ->
                    val dniDenuncia = (denuncia.dni ?: "").trim().replace(" ", "")
                    dniDenuncia == dniLimpio || dniDenuncia.contains(dniLimpio) || (dniDenuncia.isNotEmpty() && dniLimpio.contains(dniDenuncia))
                }

                Log.d("BUSQUEDA_DNI", "Total en sistema: ${todas.size}, Filtradas para DNI $dniBuscado: ${filtradas.size}")
                mostrarResultados(filtradas, dniBuscado)
            }

            override fun onFailure(call: Call<List<Denuncia>>, t: Throwable) {
                binding.progressBarBuscar.visibility = View.GONE
                binding.btnBuscar.isEnabled = true

                val dniLimpio = dniBuscado.replace(" ", "")
                val filtradasLocales = DenunciaCache.denunciasLocales.filter { denuncia ->
                    val dniDenuncia = (denuncia.dni ?: "").trim().replace(" ", "")
                    dniDenuncia == dniLimpio || dniDenuncia.contains(dniLimpio)
                }

                mostrarResultados(filtradasLocales, dniBuscado)
            }
        })
    }

    private fun mostrarResultados(lista: List<Denuncia>, dniBuscado: String) {
        listaResultados.clear()
        listaResultados.addAll(lista)
        adapter.actualizarLista(listaResultados)

        if (listaResultados.isEmpty()) {
            binding.tvBuscarEmpty.text = "No se encontraron denuncias para el DNI $dniBuscado"
            binding.tvBuscarEmpty.visibility = View.VISIBLE
            binding.rvBuscarResultados.visibility = View.GONE
        } else {
            binding.tvBuscarEmpty.visibility = View.GONE
            binding.rvBuscarResultados.visibility = View.VISIBLE
        }
    }
}
