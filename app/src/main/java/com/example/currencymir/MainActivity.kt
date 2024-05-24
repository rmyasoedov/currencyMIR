package com.example.currencymir

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
    private val tvSetNbRb: TextView by lazy {
        findViewById(R.id.tvSetNbRb)
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
    private val tvCourseBnb: TextView by lazy {
        findViewById(R.id.tvCourseBnb)
    }
    private val tvCourseNbrb: TextView by lazy {
        findViewById(R.id.tvCourseNbrb)
    }
    private val tvDateNbrb: TextView by lazy {
        findViewById(R.id.tvDateNbrb)
    }
    private val tvResultBlrZp: TextView by lazy {
        findViewById(R.id.tvResultBlrZp)
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
    private val etInputRusZp: EditText by lazy {
        findViewById(R.id.etInputRusZp)
    }
    private val swRefresh: SwipeRefreshLayout by lazy {
        findViewById(R.id.swRefresh)
    }
    private val nbrbBlock: ViewGroup by lazy {
        findViewById(R.id.nbrbBlock)
    }
    private val blockInputNbRb: ViewGroup by lazy {
        findViewById(R.id.blockInputNbRb)
    }

    private var courseMir: Float = 0F
    private var courseCommission: Float = 0F

    private val limitMonth = 50000F

    private val limitBlr = 500F

    private var exchangePrice: Float? = null
    private var courseNbrb: Float? = null
    private var dateCourseNbrb = todayDate()

    private val BNB_LOADING = true
    private var modeNbRb = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvVersion.text = "v${packageManager.getPackageInfo(packageName,0).versionName}"

        if(!BNB_LOADING){
            findViewById<ViewGroup>(R.id.bnbBlock).visibility = View.GONE
        }
    }

    private fun update(){

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val jobUsd = async(Dispatchers.IO) { loadCourseUsdRBC() }
            val jobMir = async(Dispatchers.IO) { loadCourseMir() }

            val jobBnb = if(BNB_LOADING){
                async(Dispatchers.IO) { loadUsdFromBnb() }
            }else {
                null
            }
            val jobNbrb = if(modeNbRb){
                async(Dispatchers.IO) { loadCourseNbrb(dateCourseNbrb) }
            }else{
                null
            }

            // Дождемся завершения обеих корутин
            jobUsd.await()
            jobMir.await()
            jobBnb?.await()
            jobNbrb?.await()

            progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        update()

        swRefresh.setOnRefreshListener {
            swRefresh.isRefreshing = false
            update()
        }

        tvSetNbRb.setOnClickListener {
            if(modeNbRb) return@setOnClickListener

            GlobalScope.launch(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                async(Dispatchers.IO) {loadCourseNbrb(dateCourseNbrb)}
                nbrbBlock.visibility = View.VISIBLE
                blockInputNbRb.visibility = View.VISIBLE
                it.visibility = View.GONE
                modeNbRb = true
                progressBar.visibility = View.GONE
            }

        }

        etInputBlr.addTextChangedListener {
            tvResultRus.text = getConvertRus()
        }

        etInputRus.addTextChangedListener {
            tvResultBlr.text = getConvertBlr()
        }

        etInputRusZp.addTextChangedListener {
            tvResultBlrZp.text = getConvertZp()
        }

        tvCourseUsd.setOnClickListener {

            exchangePrice?.let {
                Toast.makeText(this, "${if(it>0) "+" else ""}$it%", Toast.LENGTH_LONG).show()
            }
        }

        tvCourseNbrb.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Создание DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    // Обработка выбранной даты
                    val selectedDate = "$selectedYear-${"%02d".format(selectedMonth + 1)}-${"%02d".format(selectedDayOfMonth)}"
                    dateCourseNbrb = selectedDate
                    GlobalScope.launch(Dispatchers.Main) {
                        progressBar.visibility = View.VISIBLE
                        async(Dispatchers.IO){
                            loadCourseNbrb(selectedDate)
                        }
                        progressBar.visibility = View.GONE
                    }
                },
                year,
                month,
                dayOfMonth
            )

            // Показать DatePickerDialog
            datePickerDialog.show()
        }
    }

    private fun loadUsdFromBnb(){
        try {
            val doc: Document = Jsoup.connect("https://bnb.by/kursy-valyut/imbank/").get()
            // Поиск input с классом jsConfig
            val inputElement = doc.select("input.jsConfig").first()
            // Получение значения value
            val jsonStr = inputElement?.attr("value")
            val course = JSONObject(jsonStr).getJSONObject("USD").getJSONObject("BYN").getString("SALE")

            tvCourseBnb.text = course
        }catch (e :Exception){
            tvCourseBnb.text = "00.00"
        }
    }

    private fun loadCourseNbrb(date: String){
        try {
            val jsonString = getJsonString("https://api.nbrb.by/exrates/rates/456?periodicity=0&ondate=$date")
            courseNbrb = JSONObject(jsonString).getString("Cur_OfficialRate").toFloat()
            tvCourseNbrb.text = courseNbrb.toString()
            tvDateNbrb.text = "Курс НБ РБ на $date"
            runOnUiThread{
                tvResultBlrZp.text = getConvertZp()
            }
        }catch (e: Exception){
            tvDateNbrb.text = "Курс НБ РБ на"
            tvCourseNbrb.text = "0.0"
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadCourseUsdMediametrics(){
        exchangePrice = null
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

    private fun loadCourseUsdRBC(){
        try {
            val jsonString = getJsonString("https://quote.rbc.ru/v5/ajax/get-updated-finance-data-of-tickers/?tickersIds=59111&addSessionData=1")
            val jsonObject = JSONObject(jsonString).getJSONObject("59111")
            val price = jsonObject.getString("price")
            exchangePrice = try {
                (jsonObject.getString("exchange_price_percent").toFloat()*100).roundToInt()/100F
            }catch (_:Exception){
                null
            }

            tvCourseUsd.text = price
        }catch (e :Exception){
            exchangePrice = null
            tvCourseUsd.text = "00.00"
            println(e.message)
        }
    }

    private fun todayDate(): String{
        // Получение текущей даты
        val todayDate = Calendar.getInstance().time
        return SimpleDateFormat("yyyy-MM-dd").format(todayDate)
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

    private fun getJsonString(urlStr: String): String{
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

                val jsonString = getJsonString("https://api-user.privetmir.ru/backend/api/v2/currencies/rates?cpd=${tomorrowDate()}")
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

    private fun getConvertZp(): String{
        courseNbrb?.let {
            try {
                val inputValue = etInputRusZp.text.toString().toFloat()

                val convertBlr = (inputValue*(it/100)*100).roundToInt()/100F
                return "$convertBlr б.р"
            }catch (_:Exception){
                return "0.0 б.р"
            }
        }
        return "0.0 б.р"
    }
}