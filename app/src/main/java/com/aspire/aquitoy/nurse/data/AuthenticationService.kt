package com.aspire.aquitoy.nurse.data

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationService @Inject constructor(private val firebase: FirebaseClient,
                                                @ApplicationContext private val context: Context) {
    suspend fun login(email:String, password:String): FirebaseUser? {
        return firebase.auth.signInWithEmailAndPassword(email, password).await().user
    }

    fun isUserLogged(): Boolean {
        return getCurrentuser() != null
    }

    fun logout() {
        firebase.auth.signOut()
    }

    private fun getCurrentuser() = firebase.auth.currentUser
}