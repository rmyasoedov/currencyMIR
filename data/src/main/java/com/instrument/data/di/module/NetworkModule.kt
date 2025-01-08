package com.instrument.data.di.module

import com.instrument.data.mapper.NetworkMapper
import com.instrument.data.retrofit.HttpClient
import com.instrument.data.retrofit.NetBnbApi
import com.instrument.data.retrofit.NetClientApi
import com.instrument.data.retrofit.NetInvestingApi
import com.instrument.data.retrofit.NetMirApi
import com.instrument.data.retrofit.NetNbrbApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideRbcApi(@RBC retrofit: Retrofit): NetClientApi {
        return retrofit.create(NetClientApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInvestingApi(@Investing retrofit: Retrofit): NetInvestingApi {
        return retrofit.create(NetInvestingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMirApi(@Mir retrofit: Retrofit): NetMirApi {
        return retrofit.create(NetMirApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseNbrb(@NbrbCourse retrofit: Retrofit): NetNbrbApi {
        return retrofit.create(NetNbrbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseBnb(@BnbBank retrofit: Retrofit): NetBnbApi {
        return retrofit.create(NetBnbApi::class.java)
    }

    @Provides
    @Singleton
    @RBC
    fun provideSourceRbc(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://quote.rbc.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Mir
    fun provideSourceMir(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api-user.privetmir.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Investing
    fun provideSourceInvesting(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://www.investing.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @BnbBank
    fun provideSourceBnb(): Retrofit{
        return Retrofit.Builder()
            .client(HttpClient.getTrustClient)
            .baseUrl("https://bnb.by")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @NbrbCourse
    fun provideSourceNbrb(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.nbrb.by")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkMapper(): NetworkMapper = NetworkMapper()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RBC

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Investing

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Mir

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BnbBank

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NbrbCourse

