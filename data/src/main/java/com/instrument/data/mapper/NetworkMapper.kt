package com.instrument.data.mapper

import com.instrument.domain.model.CourseBnbModel
import com.instrument.domain.model.CourseMirModel
import com.instrument.domain.model.CourseNbrbModel
import com.instrument.domain.model.UsdModel
import com.instrument.utils.Constant.comissionPrecent
import com.instrument.utils.fract
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NetworkMapper {

    fun mapToRbcData(jsonString: String): UsdModel?{

        try {
            val jsonObject = JSONObject(jsonString).getJSONObject("353727")
            val price = jsonObject.optString("price", null).toFloatOrNull()

            val exchangePrice =
                jsonObject.optString("exchange_price_percent", null).toFloatOrNull()?.fract(2)

            return UsdModel(
                price = price,
                exchangePrice = exchangePrice,
                source = "RBC"
            )
        }catch (e: Exception){
            return null
        }

    }

    fun mapToInvestingData(htmlString: String?): UsdModel? {
        try {
            val doc: Document = Jsoup.parse(htmlString)
            val priceElement = try {
                doc.select("div[data-test=instrument-price-last]").first().text().toFloat().fract(2)
            }catch (_:Exception){
                null
            }
            val exchangePrice = try {
                doc.select("span[data-test=instrument-price-change]").first().text().toFloat()
            }catch (_:Exception){
                null
            }

            if(priceElement==null && exchangePrice==null){
                throw Exception()
            }

            return UsdModel(
                price = priceElement,
                exchangePrice = exchangePrice,
                source = "Investing"
            )
        }catch (e: Exception){
            return null
        }
    }

    fun mapToCourseMir(jsonString: String): CourseMirModel{

        try {
            val contentArray = JSONObject(jsonString).getJSONArray("content")

            var currentCourse: Float? = null
            for (i in 0 until contentArray.length()) {
                val item = contentArray.getJSONObject(i)
                val id = item.getJSONObject("currency").getString("id")
                val valueSell = item.getDouble("valueSell")

                if (id == "933") {
                    currentCourse = valueSell.toFloat()
                    // Теперь у вас есть значение valueSell для указанного id
                    break
                }
            }

            if(currentCourse==null){
                return CourseMirModel()
            }

            val courseMir = 1 / currentCourse
            val defaultSum = 50000

            val courseCommission = ((defaultSum * courseMir) / (defaultSum * (1 + comissionPrecent)) * 100)
            val fix500 = (500 / courseMir).fract(2)

            val course100 = courseMir * 100

            return CourseMirModel(
                price = currentCourse,
                course100 = course100.fract(4),
                courseCommission = courseCommission.fract(4),
                fix500 = fix500
            )
        }catch (e: Exception){
            return CourseMirModel()
        }
    }

    fun mapToCourseNbrb(jsonString: String, date:String): CourseNbrbModel{
        try {
            val courseNbrb = JSONObject(jsonString).getString("Cur_OfficialRate").toFloat()

            return CourseNbrbModel(
                price = courseNbrb,
                date = date
            )
        }catch (e: Exception){
            return CourseNbrbModel()
        }

    }

    fun mapToCourseBnb(jsonString: String): CourseBnbModel{

        try {
            val courseObj = JSONObject(jsonString)
            val courseSale = courseObj.getString("bestCurr").toFloatOrNull()
            val courseBuy =  0f

            return CourseBnbModel(
                sale = courseSale,
                buy = courseBuy
            )
        }catch (e: Exception){
            println("err: ${e.message}")
            return CourseBnbModel()
        }
    }
}