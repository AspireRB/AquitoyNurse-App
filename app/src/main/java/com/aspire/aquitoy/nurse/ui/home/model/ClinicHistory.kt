package com.aspire.aquitoy.nurse.ui.home.model

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private lateinit var fecha: EditText
    //Datos enfermero
    private lateinit var nameNurse: EditText
    private lateinit var cedulaNurse: EditText
    //General
    private lateinit var antecedentes: EditText
    private lateinit var medicamentos: EditText
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
        fecha = view.findViewById(R.id.editTextFecha)
        nameNurse = view.findViewById(R.id.editTextNameNurse)
        cedulaNurse = view.findViewById(R.id.editTextCedulaNurse)
        antecedentes = view.findViewById(R.id.editTextMedicalHistory)
        medicamentos = view.findViewById(R.id.editTextCurrentMedications)
        btnSend = view.findViewById(R.id.buttonSubmit)

        fecha.setOnClickListener {
            showDatePickerDialog()
        }
        btnSend.setOnClickListener { sendHistory(serviceID) }

        return view
    }

    private fun sendHistory(serviceID: String) {
        val patientName = namePatient.text.toString().trim()
        val patientAge = age.text.toString().trim()
        val patientCedula = cedulaPatient.text.toString().trim()
        val fecha = fecha.text.toString()
        val nurseName = nameNurse.text.toString().trim()
        val nurseCedula = cedulaNurse.text.toString().trim()
        val medicalHistory = antecedentes.text.toString().trim()
        val currentMedications = medicamentos.text.toString().trim()

        // Validar que todos los campos estén llenos
        if (patientName.isEmpty() || patientAge.isEmpty() || patientCedula.isEmpty() || fecha
            .isEmpty() || nurseName.isEmpty() || nurseCedula.isEmpty() || medicalHistory.isEmpty() || currentMedications.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamar al método de inserción de datos en la base de datos
        val success = databaseService.insertClinicHistory(
            serviceID,
            patientName,
            patientAge,
            patientCedula,
            fecha,
            nurseName,
            nurseCedula,
            medicalHistory,
            currentMedications
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

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
            // Aquí puedes hacer algo con la fecha seleccionada, como mostrarla en el EditText
            val selectedDate = "$day/${month + 1}/$year"
            fecha.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    companion object {
        const val TAG = "History"
    }
}