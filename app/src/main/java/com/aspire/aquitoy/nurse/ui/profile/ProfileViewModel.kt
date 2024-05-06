package com.aspire.aquitoy.nurse.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aspire.aquitoy.nurse.data.AuthenticationService
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.aspire.aquitoy.nurse.ui.profile.model.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authenticationService:
                                           AuthenticationService, private val databaseService: DatabaseService
): ViewModel() {
    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: MutableLiveData<UserInfo?> = _userInfo

    fun logout(navigateToIntroduction: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationService.logout()
        }
        navigateToIntroduction()
    }

    fun getInfoUser() {
        databaseService.getInfoUser { userInfo, error ->
            if (error != null) {
                Log.d("REALTIMEDATABASE","ERROR OBTENER DATOS USUARIO")
            } else {
                _userInfo.postValue(userInfo)
            }
        }
    }

}