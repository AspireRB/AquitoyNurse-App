package com.aspire.aquitoy.nurse.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessagesViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is message Fragment"
    }
    val text: LiveData<String> = _text
}