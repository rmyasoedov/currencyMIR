package com.example.currencymir.app

import android.app.Application
import com.example.currencymir.di.AppComponent
import com.example.currencymir.di.DaggerAppComponent
import com.example.currencymir.di.module.AppModule

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

    }
}