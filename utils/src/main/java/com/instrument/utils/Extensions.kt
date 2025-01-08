package com.instrument.utils

import kotlin.math.pow
import kotlin.math.roundToInt

fun Float.fract(n: Int): Float{
    if(n<=0) return this
    val factor = 10f.pow(n)
    return (this * factor).roundToInt() / factor
}