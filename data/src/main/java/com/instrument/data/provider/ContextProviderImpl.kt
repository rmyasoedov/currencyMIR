package com.instrument.data.provider

import android.content.Context
import com.instrument.domain.provider.ContextProvider
import javax.inject.Inject

class ContextProviderImpl @Inject constructor(
    private val context: Context
) : ContextProvider {
    override fun getVersionName(): String {
        return context
            .packageManager
            .getPackageInfo(context.packageName,0)
            .versionName ?: "0.0"
    }
}