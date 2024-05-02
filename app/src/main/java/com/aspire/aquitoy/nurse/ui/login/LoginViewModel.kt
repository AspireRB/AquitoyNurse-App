package com.aspire.aquitoy.nurse.ui.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspire.aquitoy.nurse.data.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authenticationService:
                                         AuthenticationService, private val context: Context
): ViewModel() {
    private var _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading:StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String, navigateToFragment: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    authenticationService.login(email, password)
                }
                if (result != null) {
                    showToast("Ingreso exitoso")
                    navigateToFragment()
                } else {
                    showToast("Credenciales de rol incorrectas")
                    Log.d("Incio de sesión", "error")
                }
            } catch (e: Exception) {
                Log.d("Sesión", "error")
            }
            _isLoading.value = false
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}