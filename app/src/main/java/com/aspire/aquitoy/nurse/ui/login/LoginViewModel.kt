package com.aspire.aquitoy.nurse.ui.login

import android.util.Log
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
class LoginViewModel @Inject constructor(private val authenticationService: AuthenticationService): ViewModel() {
    private var _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading:StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String, navigateToFragment: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                authenticationService.login(email, password)
            }

            if (result != null) {
                navigateToFragment()
            } else {
                Log.i("aspire", "error!!")
            }

            _isLoading.value = true
        }
    }
}