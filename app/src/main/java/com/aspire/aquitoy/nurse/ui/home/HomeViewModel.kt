package com.aspire.aquitoy.nurse.ui.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aspire.aquitoy.nurse.common.common
import com.aspire.aquitoy.nurse.data.AuthenticationService
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.aspire.aquitoy.nurse.data.FirebaseClient
import com.aspire.aquitoy.nurse.ui.home.model.ServiceInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val databaseService: DatabaseService, private val
authenticationService: AuthenticationService, private val context: Context): ViewModel() {

    private val _serviceInfoLiveData = MutableLiveData<ServiceInfo>()
    val serviceInfoLiveData: LiveData<ServiceInfo> = _serviceInfoLiveData


    fun createToken() {
        val result = databaseService.insertToken()
        if (result.isComplete){
            Log.d("Token", "Agregado")
        } else {
            Log.d("Token", "Fallo")
        }
    }

    fun getService() {
        val serviceReference = databaseService.getService()
        serviceReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Print the data structure for debugging
                    Log.d("Service", "DataSnapshot: ${dataSnapshot.value}")

                    var serviceFound = false // Flag to track if a "create" service is found
                    for (snapshot in dataSnapshot.children) {
                        // Access service data based on actual data structure
                        val service = snapshot.getValue<Map<String, String>>() // Assuming the service data is a map

                        if (service != null && service["state"] == "create") {
                            val serviceID = snapshot.key
                            val nurseID = service["nurseID"] ?: ""
                            val nurseLocationService = service["nurseLocationService"] ?: ""
                            val patientID = service["patientID"] ?: ""
                            val patientLocationService = service["patientLocationService"] ?: ""
                            val state = service["state"] ?: ""

                            val serviceInfo = ServiceInfo(
                                serviceID = serviceID,
                                nurseID = nurseID,
                                nurseLocationService = nurseLocationService,
                                patientID = patientID,
                                patientLocationService = patientLocationService,
                                state = state
                            )
                            _serviceInfoLiveData.value = serviceInfo
                            serviceFound = true
                            break
                        } else if (service != null && service["state"] == "accept") {
                            val serviceID = snapshot.key
                            val nurseID = service["nurseID"] ?: ""
                            val nurseLocationService = service["nurseLocationService"] ?: ""
                            val patientID = service["patientID"] ?: ""
                            val patientLocationService = service["patientLocationService"] ?: ""
                            val state = service["state"] ?: ""

                            val serviceInfo = ServiceInfo(
                                serviceID = serviceID,
                                nurseID = nurseID,
                                nurseLocationService = nurseLocationService,
                                patientID = patientID,
                                patientLocationService = patientLocationService,
                                state = state
                            )
                            _serviceInfoLiveData.value = serviceInfo
                            serviceFound = true
                            break
                        }
                    }

                    if (!serviceFound) {
                        Log.d("Service", "No service found")
                    }
                } else {
                    Log.d("Service", "No service data found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("RealtimeDatabase", "${databaseError.message}")
            }
        })
    }

    fun updateState(serviceID: String) {
        databaseService.updateStateService(serviceID, "finalized").addOnCompleteListener {
            if(it.isSuccessful) {
                databaseService.updateState(true).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Servicio finalizado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "No se pudo finalizar el servicio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun checkStateService(serviceID: String?) {
        val serviceInfoRef = databaseService.getService()
    }

}