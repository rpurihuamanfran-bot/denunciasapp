package com.rojasp.denunciasapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rojasp.denunciasapp.api.RetrofitClient
import com.rojasp.denunciasapp.databinding.ActivityRegistroBinding
import com.rojasp.denunciasapp.model.Denuncia
import com.rojasp.denunciasapp.model.DenunciaCache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdownTipo()
        setDefaultFecha()
        setupFechaPicker()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbarRegistro.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDropdownTipo() {
        val tipos = arrayOf("ROBO", "ASALTO", "VANDALISMO", "OTRO")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        binding.actvTipo.setAdapter(adapter)
        binding.actvTipo.setText(tipos[0], false)
    }

    private fun setDefaultFecha() {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        binding.etFecha.setText(sdf.format(Date()))
    }

    private fun setupFechaPicker() {
        binding.etFecha.isFocusable = false
        binding.etFecha.isClickable = true
        binding.etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    val fechaFormateada = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02dT%02d:%02d:00",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay,
                        selectedHour,
                        selectedMinute
                    )
                    binding.etFecha.setText(fechaFormateada)
                }, hour, minute, true).show()

            }, year, month, day).show()
        }
    }

    private fun setupButtons() {
        binding.btnRegistrar.setOnClickListener {
            guardarDenuncia()
        }
    }

    private fun guardarDenuncia() {
        val dni = binding.etDni.text.toString().trim()
        val tipo = binding.actvTipo.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val fechaInput = binding.etFecha.text.toString().trim()

        val fecha = if (fechaInput.contains("T")) {
            fechaInput
        } else if (fechaInput.contains(" ")) {
            fechaInput.replace(" ", "T")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        }

        if (dni.isBlank() || !dni.matches(Regex("^\\d{8}$"))) {
            binding.etDni.error = "Ingrese un DNI válido de 8 dígitos numéricos"
            return
        }

        if (direccion.isBlank()) {
            binding.etDireccion.error = "Ingrese la dirección del incidente"
            return
        }

        if (descripcion.isBlank()) {
            binding.etDescripcion.error = "Ingrese una descripción del incidente"
            return
        }

        if (tipo.isBlank()) {
            Toast.makeText(this, "Seleccione un tipo de incidente", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaDenuncia = Denuncia(
            id = null,
            dni = dni,
            fecha = fecha,
            tipo = tipo,
            direccion = direccion,
            descripcion = descripcion,
            estado = 1
        )

        binding.progressBarRegistro.visibility = View.VISIBLE
        binding.btnRegistrar.isEnabled = false

        Log.d("POST_RENDER", "Enviando denuncia a Render: $nuevaDenuncia")

        RetrofitClient.instance.crearDenuncia(nuevaDenuncia).enqueue(object : Callback<Denuncia> {
            override fun onResponse(call: Call<Denuncia>, response: Response<Denuncia>) {
                binding.progressBarRegistro.visibility = View.GONE
                binding.btnRegistrar.isEnabled = true

                if (response.isSuccessful) {
                    val creada = response.body() ?: nuevaDenuncia
                    Log.d("POST_SUCCESS", "Guardada con éxito en Render. HTTP ${response.code()}")
                    DenunciaCache.agregar(creada)

                    Toast.makeText(
                        this@RegistroActivity,
                        "¡Denuncia registrada con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    val errorDetail = response.errorBody()?.string() ?: ""
                    Log.e("POST_ERROR", "Error HTTP ${response.code()}: $errorDetail")

                    enviarRutaAlternativa(nuevaDenuncia)
                }
            }

            override fun onFailure(call: Call<Denuncia>, t: Throwable) {
                enviarRutaAlternativa(nuevaDenuncia)
            }
        })
    }

    private fun enviarRutaAlternativa(denuncia: Denuncia) {
        RetrofitClient.instance.crearDenunciaSinSlash(denuncia).enqueue(object : Callback<Denuncia> {
            override fun onResponse(call: Call<Denuncia>, response: Response<Denuncia>) {
                binding.progressBarRegistro.visibility = View.GONE
                binding.btnRegistrar.isEnabled = true

                if (response.isSuccessful) {
                    val creada = response.body() ?: denuncia
                    Log.d("POST_SUCCESS_ALT", "Guardada con éxito en Render. HTTP ${response.code()}")
                    DenunciaCache.agregar(creada)

                    Toast.makeText(
                        this@RegistroActivity,
                        "¡Denuncia registrada con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    val errorDetail = response.errorBody()?.string() ?: ""
                    Log.e("POST_ERROR_ALT", "Error HTTP ${response.code()}: $errorDetail")
                    DenunciaCache.agregar(denuncia)

                    Toast.makeText(
                        this@RegistroActivity,
                        "Respuesta servidor HTTP ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Denuncia>, t: Throwable) {
                binding.progressBarRegistro.visibility = View.GONE
                binding.btnRegistrar.isEnabled = true
                Log.e("POST_FAILURE_ALT", "Fallo de red", t)

                DenunciaCache.agregar(denuncia)
                Toast.makeText(
                    this@RegistroActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })
    }
}
