package com.example.currencymir.di

import com.example.currencymir.di.module.AppModule
import com.example.currencymir.di.module.DomainModule
import com.example.currencymir.presentation.ui.MainActivity
import com.instrument.data.di.module.NetworkModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DomainModule::class, NetworkModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}