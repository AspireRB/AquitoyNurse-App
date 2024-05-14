package com.aspire.aquitoy.nurse.ui.home.model

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.aspire.aquitoy.nurse.R
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ClinicHistory (private val serviceID: String) : BottomSheetDialogFragment() {
    @Inject lateinit var databaseService: DatabaseService
    //Datos paciente
    private lateinit var namePatient: EditText
    private lateinit var age: EditText
    private lateinit var cedulaPatient: EditText
    private lateinit var fechaNacimiento: EditText
    //Datos enfermero
    private lateinit var nameNurse: EditText
    private lateinit var tarjetaNurse: EditText
    //General
    private lateinit var diagnostico: EditText
    private lateinit var medicamentos: EditText
    private lateinit var antecedentes: EditText
    private lateinit var btnSend: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.form_clinic_history, container, false)
        namePatient = view.findViewById(R.id.editTextName)
        age = view.findViewById(R.id.editTextAge)
        cedulaPatient = view.findViewById(R.id.editTextCedula)
        fechaNacimiento = view.findViewById(R.id.editTextFecha)
        nameNurse = view.findViewById(R.id.editTextNameNurse)
        tarjetaNurse = view.findViewById(R.id.editTextCedulaNurse)
        diagnostico = view.findViewById(R.id.editTextMedicalDiagnosis)
        antecedentes = view.findViewById(R.id.editTextMedicalHistory)
        medicamentos = view.findViewById(R.id.editTextCurrentMedications)
        btnSend = view.findViewById(R.id.buttonSubmit)

        var ageSuccess = false

        age.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val isValid = input.isNotEmpty() && input.toInt() in 18..100 // Validar si la edad ingresada está entre 18 y 100 años
                if (!isValid) {
                    age.error = "Edad invalida"
                    ageSuccess = false
                } else {
                    age.error = null // Borrar el mensaje de error si la entrada es válida
                    ageSuccess = true
                }
            }
        })

        fechaNacimiento.setOnClickListener {
            showDatePickerDialog(age)
        }

        btnSend.setOnClickListener {
            if (ageSuccess) {
                sendHistory(serviceID)
            } else {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendHistory(serviceID: String) {
        val patientName = namePatient.text.toString().trim()
        val patientAge = age.text.toString().trim()
        val patientCedula = cedulaPatient.text.toString().trim()
        val patientFechaNacimiento = fechaNacimiento.text.toString()
        val nurseName = nameNurse.text.toString().trim()
        val nurseTarjeta = tarjetaNurse.text.toString().trim()
        val medicalDiagnosis = diagnostico.text.toString().trim()
        val currentMedications = medicamentos.text.toString().trim()
        val medicalHistory = antecedentes.text.toString().trim()

        // Validar que todos los campos estén llenos
        if (patientName.isEmpty() || patientAge.isEmpty() || patientCedula.isEmpty() || patientFechaNacimiento.isEmpty() || nurseName.isEmpty() || nurseTarjeta.isEmpty() || medicalDiagnosis.isEmpty() || currentMedications.isEmpty() || medicalHistory.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamar al método de inserción de datos en la base de datos
        val success = databaseService.insertClinicHistory(
            serviceID,
            patientName,
            patientAge,
            patientCedula,
            patientFechaNacimiento,
            nurseName,
            nurseTarjeta,
            medicalDiagnosis,
            currentMedications,
            medicalHistory
        )

        if (success) {
            Toast.makeText(requireContext(), "Historia clínica guardada", Toast.LENGTH_SHORT).show()
            dismissBottomSheet()
        } else {
            Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dismissBottomSheet() {
        Handler(Looper.getMainLooper()).post {
            dismiss()
        }
    }

    private fun showDatePickerDialog(age: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Calcula la fecha de nacimiento basada en la edad actual ingresada por el usuario
        val edad = age.text.toString().toIntOrNull()
        if (edad != null && edad in 18..100) {
            val birthYear = year - edad
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
                // Aquí puedes hacer algo con la fecha seleccionada, como mostrarla en el EditText
                val selectedDate = "$day/${month + 1}/$year"
                fechaNacimiento.setText(selectedDate)
            }, birthYear, month, day)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis // Establece la fecha máxima como la fecha actual
            datePickerDialog.show()
        } else {
            Toast.makeText(requireContext(), "Por favor ingresa una edad válida", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "History"
    }
}