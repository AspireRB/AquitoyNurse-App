package com.aspire.aquitoy.nurse.data

import android.content.Context
import com.aspire.aquitoy.nurse.common.common
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseService @Inject constructor(private val firebaseClient: FirebaseClient,
                                          @ApplicationContext private val context: Context){

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
}