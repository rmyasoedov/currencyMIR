package com.example.currencymir.presentation.ui

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.currencymir.R
import com.example.currencymir.presentation.ui.custome.SkeletonView
import com.example.currencymir.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
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
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var courseMir: Float = 0F
    private var courseCommission: Float = 0F
    private val comissionPrecent = 0.005F

    private val limitMonth = 50000F

    private val limitBlr = 500F

    private var exchangePrice: Float? = null
    private var courseNbrb: Float? = null
    private var dateCourseNbrb = todayDate()

    private val BNB_LOADING = true
    private var modeNbRb = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ignoreSSL()

        binding.tvVersion.text = "v${packageManager.getPackageInfo(packageName,0).versionName}"

        if(!BNB_LOADING){
            findViewById<ViewGroup>(R.id.bnbBlock).visibility = View.GONE
        }
    }

    private fun loadedData(skeletonView: SkeletonView, textView: TextView){
        runOnUiThread {
            skeletonView.isVisible = false
            textView.isVisible = true
        }
    }

    private fun update(){

        binding.tvCourseUsd.isVisible = false
        binding.skeletonUsd.isVisible = true

        binding.tvCourseMir.isVisible = false
        binding.skeletonMir.isVisible = true

        binding.tvCourseBnb.isVisible = false
        binding.skeletonBnb.isVisible = true

        binding.tvCourseNbrb?.isVisible = false
        binding.skeletonNbrb?.isVisible = true

        lifecycleScope.launch {

            val jobUsd = async(Dispatchers.IO) {
                if(!loadUsdInvesting()) loadCourseUsdtRBC()
                loadedData(binding.skeletonUsd, binding.tvCourseUsd)
            }
            val jobMir = async(Dispatchers.IO) {
                loadCourseMir()
                loadedData(binding.skeletonMir, binding.tvCourseMir)
            }

            val jobBnb = if(BNB_LOADING){
                async(Dispatchers.IO) {
                    loadUsdFromBnb()
                    loadedData(binding.skeletonBnb, binding.tvCourseBnb)
                }
            }else {
                null
            }
            val jobNbrb = if(modeNbRb){
                async(Dispatchers.IO) {
                    loadCourseNbrb(dateCourseNbrb)
                    loadedData(binding.skeletonNbrb!!, binding.tvCourseNbrb!!)
                }
            }else{
                null
            }
        }
    }

    override fun onResume() {
        super.onResume()

        update()

        binding.swRefresh.setOnRefreshListener {
            binding.swRefresh.isRefreshing = false
            update()
        }

        binding.tvSetNbRb?.setOnClickListener {
            if(modeNbRb) return@setOnClickListener

            binding.tvCourseNbrb?.isVisible = false
            binding.skeletonNbrb?.isVisible = true

            lifecycleScope.launch {
                async(Dispatchers.IO) {
                    loadCourseNbrb(dateCourseNbrb)
                    loadedData(binding.skeletonNbrb!!, binding.tvCourseNbrb!!)
                }.join()
                binding.nbrbBlock?.visibility = View.VISIBLE
                binding.blockInputNbRb?.visibility = View.VISIBLE
                it.visibility = View.GONE
                modeNbRb = true
            }

        }

        binding.etInputBlr?.addTextChangedListener {
            binding.tvResultRus?.text = getConvertRus()
        }

        binding.etInputRus?.addTextChangedListener {
            binding.tvResultBlr?.text = getConvertBlr()
        }

        binding.etInputRusZp?.addTextChangedListener {
            binding.tvResultBlrZp?.text = getConvertZp()
        }

        binding.tvCourseUsd.setOnClickListener {

            exchangePrice?.let {
                Toast.makeText(this, "${if(it>0) "+" else ""}$it%", Toast.LENGTH_LONG).show()
            }
        }

        binding.tvCourseNbrb?.setOnClickListener {
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
                    lifecycleScope.launch {
                        binding.tvCourseNbrb?.isVisible = false
                        binding.skeletonNbrb?.isVisible = true
                        async(Dispatchers.IO){
                            loadCourseNbrb(selectedDate)
                            loadedData(binding.skeletonNbrb!!, binding.tvCourseNbrb!!)
                        }

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

    fun ignoreSSL() {
        // Игнорирование SSL-ошибок
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
    }

    private fun loadUsdFromBnb(){
        try {
            //https://bnb.by/o-lichnom/obsluzhivanie/obmen-valyut/
            val doc: Document = Jsoup.connect("https://bnb.by/o-lichnom/obsluzhivanie/obmen-valyut/").get()
            // Поиск input с классом jsConfig
            val inputElement = doc.select("#jsRatesSettings").first()
            // Получение значения value
            val jsonStr = inputElement?.attr("data-date")
            val courseObj = JSONObject(jsonStr).getJSONObject("8").getJSONObject("32")
            val courseSale = courseObj.getString("UF_SALE")
            val courseBuy = courseObj.getString("UF_BUY")
            runOnUiThread {
                binding.tvCourseBnb.text = courseSale
                binding.tvCourseBnb.setOnClickListener {
                    Toast.makeText(this@MainActivity, courseBuy, Toast.LENGTH_LONG).show()
                }
            }
        }catch (e :Exception){
            runOnUiThread {
                binding.tvCourseBnb.text = "00.00"
            }
        }
    }

    private fun loadCourseNbrb(date: String){
        try {
            val jsonString = getJsonString("https://api.nbrb.by/exrates/rates/456?periodicity=0&ondate=$date")
            courseNbrb = JSONObject(jsonString).getString("Cur_OfficialRate").toFloat()
            runOnUiThread{
                binding.tvCourseNbrb?.text = courseNbrb.toString()
                binding.tvDateNbrb?.text = "Курс НБ РБ на $date"
                binding.tvResultBlrZp?.text = getConvertZp()
            }
        }catch (e: Exception){
            runOnUiThread {
                binding.tvDateNbrb?.text = "Курс НБ РБ на"
                binding.tvCourseNbrb?.text = "0.0"
            }
        }
    }

    private fun loadUsdInvesting(): Boolean{
        try {
            val doc: Document = Jsoup.connect("https://www.investing.com/currencies/usd-rub").get()
            val priceElement = doc.select("div[data-test=instrument-price-last]").first()
            exchangePrice = try {
                doc.select("span[data-test=instrument-price-change]").first().text().toFloat()
            }catch (_:Exception){
                null
            }
            runOnUiThread {
                val price = (priceElement.text().toFloat()*100).roundToInt()/100F
                binding.tvCourseUsd.text = price.toString()
                setUsdLabel("Investing")
            }
        }catch (e :Exception){
            return false
        }
        return true
    }


    private fun loadCourseUsdtRBC(): Boolean{
        try {
            val jsonString = getJsonString("https://quote.rbc.ru/v5/ajax/get-updated-finance-data-of-tickers/?tickersIds=353727&addSessionData=1")
            val jsonObject = JSONObject(jsonString).getJSONObject("353727")
            val price = jsonObject.getString("price")
            exchangePrice = try {
                (jsonObject.getString("exchange_price_percent").toFloat()*100).roundToInt()/100F
            }catch (_:Exception){
                null
            }

            runOnUiThread {
                binding.tvCourseUsd.text = price
                setUsdLabel("RBC")
            }
        }catch (e :Exception){
            exchangePrice = null
            runOnUiThread {
                binding.tvCourseUsd.text = "00.00"
            }
            return false
            println(e.message)
        }
        return true
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
                    binding.tvCourseMir.text = exchangeRateValue
                    courseMir = 1 / exchangeRateValue.toFloat()
                    val defaultSum = 50000

                    courseCommission = ((defaultSum * courseMir) / (defaultSum * (1 + comissionPrecent)) * 100)
                    val fix500 = (500 / courseMir).fract(2)

                    val course100 = courseMir * 100
                    runOnUiThread {
                        binding.tvResultRus?.text = getConvertRus()
                        binding.tvResultBlr?.text = getConvertBlr()
                        binding.tvCourse100.text = "${course100.fract(4)}"
                        binding.tvCourseCommission.text = "${courseCommission.fract(4)}"
                        binding.tv500br.text = "$fix500 б.р"
                    }
                } ?: run {
                    binding.tvCourseMir.text = "00.00"
                }

            }catch (e: Exception){
                println(e.message)
            }
    }

    private fun getConvertRus(): String{
        val inputBlr: Float? = try {
            binding.etInputBlr?.text.toString().toFloat()
        }catch (_:Exception){null}

        return if(inputBlr==null){
            "0.0 р.р"
        }else{
            "${((inputBlr/courseMir*100).roundToInt()/100.0)} р.р"
        }
    }

    fun Float.fract(n: Int): Float{
        if(n<=0) return this
        val factor = 10f.pow(n)
        return (this * factor).roundToInt() / factor
    }

    private fun setUsdLabel(source: String){
        binding.tvUsdLabel.text = "Курс доллара ($source)"
    }

    private fun getConvertBlr(): String{
        val inputRus: Float? = try {
            binding.etInputRus?.text.toString().toFloat()
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
                val inputValue = binding.etInputRusZp?.text.toString().toFloat()

                val convertBlr = (inputValue*(it/100)*100).roundToInt()/100F
                return "$convertBlr б.р"
            }catch (_:Exception){
                return "0.0 б.р"
            }
        }
        return "0.0 б.р"
    }
}