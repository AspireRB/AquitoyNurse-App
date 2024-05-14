package com.aspire.aquitoy.nurse.data

import android.content.Context
import android.util.Log
import com.aspire.aquitoy.nurse.common.common
import com.aspire.aquitoy.nurse.ui.profile.model.UserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseService @Inject constructor(private val firebaseClient: FirebaseClient, private val
context: Context){

    fun insertToken(): Task<String> {
        return firebaseClient.messaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                if (token != null) {
                    val nurseInfoRef = firebaseClient.db_rt.child(common.NURSE_INFO_REFERENCE)
                    nurseInfoRef.child("token").setValue(token)
                }
            }
        }
    }

    fun getNurseInfoRef (): DatabaseReference {
        return firebaseClient.db_rt.child(common.NURSE_INFO_REFERENCE)
    }

    fun getService(): Query {
        val nurseID = firebaseClient.auth.currentUser!!.uid
        val serviceInfoRef  = firebaseClient.db_rt.child(common.SERVICE_INFO_REFERENCE)
        val query = serviceInfoRef.orderByChild("nurseID").equalTo(nurseID)

        return query
    }

    fun updateStateService(serviceID : String, state : String): Task<Void> {
        val serviceInfoRef = firebaseClient.db_rt.child(common.SERVICE_INFO_REFERENCE)
        return serviceInfoRef.child(serviceID).child("state").setValue(state).addOnFailureListener {
            exception ->
            Log.d("REALTIMEDATABASE", "ERROR: ${exception.message}")
        }
    }

    fun updateState(asset : Boolean): Task<Void> {
        val nurseID = firebaseClient.auth.currentUser!!.uid
        val nurseInfoRef = getNurseInfoRef()
        return nurseInfoRef.child(nurseID).child("state").setValue(asset).addOnFailureListener {
                exception ->
            Log.d("REALTIMEDATABASE", "ERROR: ${exception.message}")
        }
    }

    fun insertClinicHistory(
        serviceID: String,
        patientName: String,
        patientAge: String,
        patientCedula: String,
        patientFechaNacimiento: String,
        nurseName: String,
        nurseTarjeta: String,
        medicalDiagnosis: String,
        currentMedications: String,
        medicalHistory: String
    ): Boolean {
        try {
            val serviceInfoRef = firebaseClient.db_rt.child(common.SERVICE_INFO_REFERENCE).child(serviceID)

            val calendarioActual = Calendar.getInstance()
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy")
            val fechaFormateada = formatoFecha.format(calendarioActual.time)
            val fechaActual: String = fechaFormateada

            // Crear un HashMap para almacenar los datos de la historia clínica
            val clinicHistoryMap = HashMap<String, Any>()
            clinicHistoryMap["fecha"] = fechaActual
            clinicHistoryMap["patientName"] = patientName
            clinicHistoryMap["patientAge"] = patientAge
            clinicHistoryMap["patientCedula"] = patientCedula
            clinicHistoryMap["patientFechaNacimiento"] = patientFechaNacimiento
            clinicHistoryMap["nurseName"] = nurseName
            clinicHistoryMap["nurseTarjeta"] = nurseTarjeta
            clinicHistoryMap["medicalDiagnosis"] = medicalDiagnosis
            clinicHistoryMap["currentMedications"] = currentMedications
            clinicHistoryMap["medicalHistory"] = medicalHistory

            // Insertar los datos en la base de datos
            serviceInfoRef.updateChildren(clinicHistoryMap)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getInfoUser(callback: (UserInfo?, Throwable?) -> Unit) {
        val currentUser = firebaseClient.auth.currentUser!!.uid
        val currentUserInfoRef = firebaseClient.db_rt.child(common.NURSE_INFO_REFERENCE)
        currentUserInfoRef.child(currentUser).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    // Obtener los valores de la base de datos
                    val reaName = dataSnapshot.child("realName").getValue(String::class.java)
                    val email = dataSnapshot.child("email").getValue(String::class.java)
                    val rol = dataSnapshot.child("rol").getValue(String::class.java)
                    // Otros campos...

                    // Crear un objeto de modelo de datos con la información obtenida
                    val userInfo = UserInfo(reaName!!, email!!, rol!!)

                    // Devolver la información al llamador
                    callback(userInfo, null)
                } else {
                    // Manejar el caso donde los datos no existen
                    callback(null, Throwable("Data not found"))
                }
            } else {
                // Manejar el error
                callback(null, task.exception)
            }
        }
    }
}