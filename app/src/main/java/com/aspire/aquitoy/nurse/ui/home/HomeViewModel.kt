package com.aspire.aquitoy.nurse.ui.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.aspire.aquitoy.nurse.ui.home.model.ServiceInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val databaseService: DatabaseService, private val context: Context): ViewModel() {

    private val _serviceInfoListLiveData = MutableLiveData<List<ServiceInfo>>()
    val serviceInfoListLiveData: LiveData<List<ServiceInfo>> = _serviceInfoListLiveData


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
                    Log.d("Service", "DataSnapshot: ${dataSnapshot.value}")

                    val serviceList = mutableListOf<ServiceInfo>()

                    for (snapshot in dataSnapshot.children) {
                        val service = snapshot.getValue<Map<String, String>>()
                        val serviceID = snapshot.key
                        val nurseID = service?.get("nurseID") ?: ""
                        val patientID = service?.get("patientID") ?: ""
                        val patientLocationService = service?.get("patientLocationService") ?: ""
                        val state = service?.get("state") ?: ""
                        val sendHistory = service?.get("sendHistory") ?: ""

                        if (service != null) {
                            val serviceInfoModel = ServiceInfo(
                                serviceID = serviceID,
                                nurseID = nurseID,
                                patientID = patientID,
                                patientLocationService = patientLocationService,
                                state = state,
                                sendHistory = sendHistory
                            )
                            serviceList.add(serviceInfoModel)
                        }
                    }

                    if (serviceList.isNotEmpty()) {
                        _serviceInfoListLiveData.value = serviceList
                    } else {
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
                databaseService.updateState("OK").addOnCompleteListener {
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
}