package com.rojasp.denunciasapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojasp.denunciasapp.adapter.DenunciaAdapter
import com.rojasp.denunciasapp.api.RetrofitClient
import com.rojasp.denunciasapp.databinding.ActivityMainBinding
import com.rojasp.denunciasapp.model.Denuncia
import com.rojasp.denunciasapp.model.DenunciaCache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DenunciaAdapter
    private val listaDenuncias = mutableListOf<Denuncia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        cargarDenuncias()
    }

    override fun onResume() {
        super.onResume()
        cargarDenuncias()
    }

    private fun setupRecyclerView() {
        adapter = DenunciaAdapter(listaDenuncias) { denuncia ->
            val intent = Intent(this, DetalleActivity::class.java).apply {
                putExtra("DENUNCIA_EXTRA", denuncia)
            }
            startActivity(intent)
        }
        binding.rvDenuncias.layoutManager = LinearLayoutManager(this)
        binding.rvDenuncias.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnNavNuevaDenuncia.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.btnNavBuscarDni.setOnClickListener {
            startActivity(Intent(this, BuscarDniActivity::class.java))
        }

        binding.btnCargarMas.setOnClickListener {
            cargarDenuncias()
        }
    }

    private fun cargarDenuncias() {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.instance.obtenerDenuncias().enqueue(object : Callback<List<Denuncia>> {
            override fun onResponse(
                call: Call<List<Denuncia>>,
                response: Response<List<Denuncia>>
            ) {
                val remotas = response.body() ?: emptyList()
                Log.d("API_RES_SLASH", "HTTP ${response.code()}. Total remotas: ${remotas.size}")

                if (response.isSuccessful && remotas.isNotEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    mostrarListaDenuncias(remotas)
                } else {
                    // Si con slash vino vacío o error, probar sin slash
                    cargarSinSlash()
                }
            }

            override fun onFailure(call: Call<List<Denuncia>>, t: Throwable) {
                Log.w("API_WARNING", "Fallo con slash, probando sin slash: ${t.message}")
                cargarSinSlash()
            }
        })
    }

    private fun cargarSinSlash() {
        RetrofitClient.instance.obtenerDenunciasSinSlash().enqueue(object : Callback<List<Denuncia>> {
            override fun onResponse(
                call: Call<List<Denuncia>>,
                response: Response<List<Denuncia>>
            ) {
                binding.progressBar.visibility = View.GONE
                val remotas = response.body() ?: emptyList()
                Log.d("API_RES_NOSLASH", "HTTP ${response.code()}. Total remotas: ${remotas.size}")

                mostrarListaDenuncias(remotas)
            }

            override fun onFailure(call: Call<List<Denuncia>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("API_ERROR", "Error de red", t)
                mostrarListaDenuncias(emptyList())
            }
        })
    }

    private fun mostrarListaDenuncias(remotas: List<Denuncia>) {
        val combinadas = mutableListOf<Denuncia>()
        combinadas.addAll(remotas)

        for (local in DenunciaCache.denunciasLocales) {
            val yaExiste = combinadas.any {
                (it.id != null && it.id == local.id) ||
                (it.dni == local.dni && it.fecha == local.fecha)
            }
            if (!yaExiste) {
                combinadas.add(0, local)
            }
        }

        Log.d("MOSTRAR_LISTA", "Total final a mostrar en pantalla: ${combinadas.size}")

        adapter.actualizarLista(combinadas)

        if (combinadas.isEmpty()) {
            binding.rvDenuncias.visibility = View.GONE
            binding.tvEmpty.text = "No hay denuncias registradas actualmente."
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvDenuncias.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
        }
    }
}
