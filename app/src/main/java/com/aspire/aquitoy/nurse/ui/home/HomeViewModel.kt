package com.aspire.aquitoy.nurse.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aspire.aquitoy.nurse.common.common
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.aspire.aquitoy.nurse.data.FirebaseClient
import com.google.android.gms.tasks.OnCompleteListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val databaseService: DatabaseService): ViewModel() {
    fun createToken() {
        val result = databaseService.insertToken()
        if (result.isComplete){
            Log.d("Token", "Agregado")
        } else {
            Log.d("Token", "Fallo")
        }
    }

}