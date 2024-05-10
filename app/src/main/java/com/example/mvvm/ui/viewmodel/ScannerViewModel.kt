package com.example.mvvm.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {
    val textInputEditTextValue = MutableLiveData<String>()

    fun onTextInputEditTextValueChanged(newValue: String) {
        textInputEditTextValue.postValue(newValue)
    }
}