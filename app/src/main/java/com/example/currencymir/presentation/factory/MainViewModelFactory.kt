package com.example.currencymir.presentation.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.currencymir.presentation.viewmodel.MainViewModel
import javax.inject.Inject
import javax.inject.Provider

class MainViewModelFactory @Inject constructor(
    private val viewModelProvider: Provider<MainViewModel>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModelProvider.get() as T
    }
}