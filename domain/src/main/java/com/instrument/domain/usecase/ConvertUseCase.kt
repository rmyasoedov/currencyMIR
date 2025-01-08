package com.instrument.domain.usecase

import com.instrument.domain.provider.ContextProvider
import javax.inject.Inject
import kotlin.math.roundToInt

class PackageVersionUseCase @Inject constructor(
    private val contextProvider: ContextProvider
){
    fun invoke(): String = contextProvider.getVersionName()
}

class GetConvertIncomeUseCase @Inject constructor(){
    fun invoke(courseNbrb: Float?, inputValue: Float): String{
        courseNbrb?.let {
            try {
                val convertBlr = (inputValue*(it/100)*100).roundToInt()/100F
                return "$convertBlr б.р"
            }catch (_:Exception){
                return "0.0 б.р"
            }
        }
        return "0.0 б.р"
    }
}

class GetConvertInRus @Inject constructor(){
    fun invoke(courseMir: Float, inputBlr: Float?): String{
        return if(inputBlr==null){
            "0.0 р.р"
        }else{
            "${((inputBlr/courseMir*100).roundToInt()/100.0)} р.р"
        }
    }
}

class GetConvertInBlr @Inject constructor(){
    fun invoke(courseMir: Float, inputRus: Float?): String{
        return if(inputRus==null){
            "0.0 б.р"
        }else{
            "${((inputRus*courseMir*100).roundToInt()/100.0)} б.р"
        }
    }
}