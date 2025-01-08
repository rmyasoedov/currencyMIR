package com.instrument.data.retrofit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Headers
import retrofit2.Call
import retrofit2.Response

class MyResponse <T>{
    var code: Int? = null
    var error: Boolean = false
    var e: Exception? = null
    var body: T?= null
    var errorBody: Any? = null
    var raw: Any? = null
    var headers: Headers? = null
    var message: Any?= null
    var isSuccessful: Boolean = false
    var errorMessage: Any ?= null
    var errorUtf8: String ?= null

    companion object{
        private suspend fun <T> callbackResponse(deferredResponse: Deferred<Response<T>>): MyResponse<T>{
            val myResponse = MyResponse<T>()
            myResponse.error = false

            try {
                val response = deferredResponse.await()

                myResponse.apply {
                    code = response.code()
                    body = response.body()
                    errorBody = response.errorBody()
                    raw = response.raw()
                    headers = response.headers()
                    message = response.message()
                    isSuccessful = response.isSuccessful
                    val errorMessage1 = response.errorBody()?.byteString()
                    errorMessage = errorMessage1
                    errorUtf8 = errorMessage1?.utf8()
                }

            }catch (e: Exception){
                myResponse.error = true
                myResponse.e = e
            }
            return myResponse
        }

        suspend fun <T> getResponse(response: Call<T>): MyResponse<T>{
            return callbackResponse(
                CoroutineScope(Dispatchers.IO).async {
                    response.execute()
                }
            )
        }
    }
}