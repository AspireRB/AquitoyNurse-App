package com.aspire.aquitoy.nurse.data

import android.content.Context
import com.aspire.aquitoy.nurse.common.common
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthenticationService @Inject constructor(private val firebase: FirebaseClient,
                                                @ApplicationContext private val context: Context) {
    suspend fun login(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = firebase.auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val role = checkUserRole(user.uid)
                if (role == "nurse") {
                    user
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    private suspend fun checkUserRole(uid: String): String? {
        return suspendCancellableCoroutine { continuation ->
            val userRef = firebase.db_rt.child(common.NURSE_INFO_REFERENCE).child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("rol").getValue(String::class.java)
                    continuation.resume(role)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(null)
                }
            })
        }
    }

    fun isUserLogged(): Boolean {
        return getCurrentuser() != null
    }

    fun logout() {
        firebase.auth.signOut()
    }

    fun currentUserId(): String {
        return firebase.auth.currentUser!!.uid
    }

    private fun getCurrentuser() = firebase.auth.currentUser
}