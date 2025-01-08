package com.example.currencymir.di.module

import android.content.Context
import com.instrument.data.mapper.NetworkMapper
import com.instrument.data.provider.ContextProviderImpl
import com.instrument.data.repository.RetrofitRepositoryImpl
import com.instrument.data.retrofit.NetBnbApi
import com.instrument.data.retrofit.NetClientApi
import com.instrument.data.retrofit.NetInvestingApi
import com.instrument.data.retrofit.NetMirApi
import com.instrument.data.retrofit.NetNbrbApi
import com.instrument.domain.provider.ContextProvider
import com.instrument.domain.repository.RetrofitRepository
import dagger.Module
import dagger.Provides

@Module
class DomainModule {

    @Provides
    fun provideRetrofitRepository(
        networkMapper: NetworkMapper,
        netClientApi: NetClientApi,
        netInvestingApi: NetInvestingApi,
        netMirApi: NetMirApi,
        netNbrbApi: NetNbrbApi,
        netBnbApi: NetBnbApi
    ): RetrofitRepository{
        return RetrofitRepositoryImpl(
            networkMapper,
            netClientApi,
            netInvestingApi,
            netMirApi,
            netNbrbApi,
            netBnbApi
        )
    }

    @Provides
    fun provideContextProvider(context: Context): ContextProvider{
        return ContextProviderImpl(context)
    }
}