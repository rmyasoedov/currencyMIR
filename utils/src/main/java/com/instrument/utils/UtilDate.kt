package com.instrument.utils

import java.text.SimpleDateFormat
import java.util.Calendar

object UtilDate {
    fun tomorrowDate(): String {
        // Получение текущей даты
        val calendar = Calendar.getInstance()

        // Добавление одного дня
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        // Получение даты завтрашнего дня
        val tomorrow = calendar.time

        // Форматирование даты в нужном формате
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(tomorrow)
    }

    fun todayDate(): String{
        // Получение текущей даты
        val todayDate = Calendar.getInstance().time
        return SimpleDateFormat("yyyy-MM-dd").format(todayDate)
    }

    fun formatIntToDate(year: Int, month: Int, day: Int): String{
        return "$year-${"%02d".format(month + 1)}-${"%02d".format(day)}"
    }
}