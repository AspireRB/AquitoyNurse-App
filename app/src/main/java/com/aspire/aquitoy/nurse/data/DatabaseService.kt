package com.aspire.aquitoy.nurse.data

import android.content.Context
import android.util.Log
import com.aspire.aquitoy.nurse.common.common
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
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

    fun getService(): Query {
        val nurseID = firebaseClient.auth.currentUser!!.uid
        val serviceInfoRef  = firebaseClient.db_rt.child(common.SERVICE_INFO_REFERENCES)
        val query = serviceInfoRef.orderByChild("nurseID").equalTo(nurseID)

        return query
    }
}