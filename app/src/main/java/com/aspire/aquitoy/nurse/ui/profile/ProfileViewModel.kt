package com.aspire.aquitoy.nurse.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspire.aquitoy.nurse.data.AuthenticationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authenticationService:
                                           AuthenticationService): ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text

    fun logout(navigateToIntroduction: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationService.logout()
        }
        navigateToIntroduction()
    }

}