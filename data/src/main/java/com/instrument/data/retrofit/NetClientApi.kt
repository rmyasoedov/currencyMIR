package com.instrument.data.retrofit

import com.google.gson.JsonElement
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NetClientApi {
    @Headers(
        "Cache-Control: no-cache",
    )
    @GET("/v5/ajax/get-updated-finance-data-of-tickers/?tickersIds=353727&addSessionData=1")
    fun getUsdtFromRbc(): Call<JsonElement>
}

interface NetInvestingApi {
    @Headers(
        "Cache-Control: no-cache",
    )
    @GET("/currencies/usd-rub")
    fun getUsdFromInvesting(): Call<String>
}

interface NetBnbApi {
    @Headers(
        "Cache-Control: no-cache",
    )
    @GET("/local/templates/itachMain/includes/ajax/getNewMoney.php?currentValue=1&currentCurrency=usd&saleCurrencyCode=BYN&buyCurrencyCode=USD&currentType=8")
    fun getCourseBnb(): Call<String>
}

interface NetMirApi {
    @Headers(
        "Cache-Control: no-cache",
    )
    @GET("/backend/api/v2/currencies/rates")
    fun getCourseMir(
        @Query("cpd") tomorrowDate: String
    ): Call<JsonElement>
}

interface NetNbrbApi {
    @Headers(
        "Cache-Control: no-cache",
    )
    @GET("/exrates/rates/456")
    fun getCourseMir(
        @Query("periodicity") periodicity: Int,
        @Query("ondate") ondate: String
    ): Call<JsonElement>
}
