package com.example.currencymir

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.android.volley.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.stream.Collectors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val tvVersion: TextView by lazy {
        findViewById(R.id.tvVersion)
    }
    private val tvCourseUsd: TextView by lazy {
        findViewById(R.id.tvCourseUsd)
    }
    private val tvCourseCommission: TextView by lazy {
        findViewById(R.id.tvCourseCommission)
    }
    private val tvCourseMir: TextView by lazy {
        findViewById(R.id.tvCourseMir)
    }
    private val tvUpdate: TextView by lazy {
        findViewById(R.id.tvUpdate)
    }
    private val tvCourse100: TextView by lazy {
        findViewById(R.id.tvCourse100)
    }
    private val tv500br: TextView by lazy {
        findViewById(R.id.tv500br)
    }
    private val tvBreakdown: TextView by lazy {
        findViewById(R.id.tvBreakdown)
    }
    private val tvResultRus: TextView by lazy {
        findViewById(R.id.tvResultRus)
    }
    private val tvResultBlr: TextView by lazy {
        findViewById(R.id.tvResultBlr)
    }
    private val progressBar: ProgressBar by lazy {
        findViewById(R.id.progressBar)
    }
    private val etInputBlr: EditText by lazy {
        findViewById(R.id.etInputBlr)
    }
    private val etInputRus: EditText by lazy {
        findViewById(R.id.etInputRus)
    }

    private var courseMir: Float = 0F
    private var courseCommission: Float = 0F

    private val limitMonth = 50000F

    private val limitBlr = 500F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvVersion.text = "v${packageManager.getPackageInfo(packageName,0).versionName}"
    }

    private fun update(){

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val jobUsd = async(Dispatchers.IO) { loadCourseUsd() }
            val jobMir = async(Dispatchers.IO) { loadCourseMir() }

            // Дождемся завершения обеих корутин
            jobUsd.await()
            jobMir.await()

            progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        update()

        tvUpdate.setOnClickListener {
            update()
        }

        etInputBlr.addTextChangedListener {
            tvResultRus.text = getConvertRus()
        }

        etInputRus.addTextChangedListener {
            tvResultBlr.text = getConvertBlr()
        }
    }


    private fun loadCourseUsd(){
            try {
                val url = URL("https://mediametrics.ru/quotes/top/currency_out.js")
                val br = BufferedReader(InputStreamReader(url.openStream()))
                val obj = br.lines().collect(Collectors.joining())
                val findStr="\"USD000UTSTOM\","
                val pos = obj.indexOf(findStr) + findStr.length
                var i=pos
                var rub = ""
                while (obj[i]!=']'){
                    rub+=obj[i]
                    i++
                }
                rub = rub.substring(0, rub.indexOf(","))
                tvCourseUsd.text = rub
            }catch (e :Exception){
                tvCourseUsd.text = "00.00"
                println(e.message)
            }
    }

    private fun tomorrowDate(): String {
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

    private fun getLoadMir(): String{
        val urlStr = "https://api-user.privetmir.ru/backend/api/v2/currencies/rates?cpd=${tomorrowDate()}"
        println("url MIR: $urlStr")
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        var responseString = ""
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            responseString = response.toString()

            reader.close()
            inputStream.close()
        } else {
            // Обработка ошибки
        }

        connection.disconnect()
        return responseString
    }

    private fun loadCourseMir(){
            try {

                val jsonString = getLoadMir()
                val jsonObject = JSONObject(jsonString)

                val contentArray = jsonObject.getJSONArray("content")

                var currentCourse: String? = null
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    val id = item.getJSONObject("currency").getString("id")
                    val valueSell = item.getDouble("valueSell")

                    if (id == "933") {
                        currentCourse = valueSell.toString()
                        // Теперь у вас есть значение valueSell для указанного id
                        break
                    }
                }

                // Выводим результат
                currentCourse?.let {
                    val exchangeRateValue = it
                    tvCourseMir.text = exchangeRateValue
                    courseMir = 1 / exchangeRateValue.toFloat()
                    val defaultSum = 50000

                    courseCommission = ((defaultSum * courseMir) / (defaultSum * 1.01) * 100).toFloat()

                    val fix500 = 500 / courseMir

                    val parts = arrayListOf<Float>()

                    var remains = limitMonth

                    while (true){
                        if(remains-fix500<=0){
                            val remain = remains * courseMir
                            parts.add(remain)
                            break
                        }
                        parts.add(limitBlr)
                        remains-=fix500
                    }

                    val course100 = courseMir * 100
                    runOnUiThread {

                        tvResultRus.text = getConvertRus()
                        tvResultBlr.text = getConvertBlr()
                        tvCourse100.text = "$course100"
                        tvCourseCommission.text = "$courseCommission"
                        tv500br.text = "$fix500 б.р"

                        tvBreakdown.text = ""
                        parts.forEach {part->
                            tvBreakdown.append("${String.format("%.2f", part)}\n")
                        }
                    }
                } ?: run {
                    tvCourseMir.text = "00.00"
                }

            }catch (e: Exception){
                println(e.message)
            }
    }

    private fun getConvertRus(): String{
        val inputBlr: Float? = try {
            etInputBlr.text.toString().toFloat()
        }catch (_:Exception){null}

        return if(inputBlr==null){
            "0.0 р.р"
        }else{
            "${((inputBlr/courseMir*100).roundToInt()/100.0)} р.р"
        }
    }

    private fun getConvertBlr(): String{
        val inputRus: Float? = try {
            etInputRus.text.toString().toFloat()
        }catch (_:Exception){null}

        return if(inputRus==null){
            "0.0 б.р"
        }else{
            "${((inputRus*courseMir*100).roundToInt()/100.0)} б.р"
        }
    }
}