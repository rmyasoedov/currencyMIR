package com.instrument.data.repository

import com.instrument.data.mapper.NetworkMapper
import com.instrument.data.retrofit.MyResponse
import com.instrument.data.retrofit.NetBnbApi
import com.instrument.data.retrofit.NetClientApi
import com.instrument.data.retrofit.NetInvestingApi
import com.instrument.data.retrofit.NetMirApi
import com.instrument.data.retrofit.NetNbrbApi
import com.instrument.domain.model.CourseBnbModel
import com.instrument.domain.model.CourseMirModel
import com.instrument.domain.model.CourseNbrbModel
import com.instrument.domain.model.ResponseModel
import com.instrument.domain.model.UsdModel
import com.instrument.domain.repository.RetrofitRepository
import com.instrument.utils.UtilDate
import javax.inject.Inject

class RetrofitRepositoryImpl @Inject constructor(
    private val networkMapper: NetworkMapper,
    private val netClientApi: NetClientApi,
    private val netInvestingApi: NetInvestingApi,
    private val netMirApi: NetMirApi,
    private val netNbrbApi: NetNbrbApi,
    private val netBnbApi: NetBnbApi
) : RetrofitRepository {

    override suspend fun getCourseUsdData(): ResponseModel<UsdModel> {
        val response = ResponseModel<UsdModel>()
        val resultInvest = MyResponse.getResponse(netInvestingApi.getUsdFromInvesting())
        val mapInvesting = networkMapper.mapToInvestingData(resultInvest.body)
        if(mapInvesting==null){
            val resultRbc = MyResponse.getResponse(netClientApi.getUsdtFromRbc())
            response.code = resultRbc.code
            if(response.code!=200){
                response.error = true
                response.errorMessage = "code: ${resultRbc.code}. ${resultRbc.errorUtf8}"
            }
            response.body = networkMapper.mapToRbcData(resultRbc.body.toString())
        }else{
            response.code = resultInvest.code
            response.body = mapInvesting
        }

        return response
    }

    override suspend fun getCourseMirData(): ResponseModel<CourseMirModel> {
        val response = ResponseModel<CourseMirModel>()
        val result = MyResponse.getResponse(netMirApi.getCourseMir(UtilDate.tomorrowDate()))

        if(result.code!=200){
            response.error = true
            response.code = result.code
            response.errorMessage = "code: ${result.code}. ${result.errorUtf8}"
        }else{
            response.body = networkMapper.mapToCourseMir(result.body.toString())
        }
        return response
    }

    override suspend fun getCourseBnbData(): ResponseModel<CourseBnbModel> {
        val response = ResponseModel<CourseBnbModel>()
        val result = MyResponse.getResponse(netBnbApi.getCourseBnb())

        if(result.code!=200){
            println("error")
            response.error = true
            response.code = result.code
            response.errorMessage = "code: ${result.code}. ${result.errorUtf8}"
        }else{
            println("not")
            response.body = networkMapper.mapToCourseBnb(result.body.toString())
        }

        println(response.body.toString())
        return response
    }

    override suspend fun getNbrbCourseData(nbrbDate: String): ResponseModel<CourseNbrbModel> {
        val response = ResponseModel<CourseNbrbModel>()
        val result = MyResponse.getResponse(netNbrbApi.getCourseMir(
            periodicity = 0,
            ondate = nbrbDate
        ))
        if(result.code!=200 || result.error){
            response.error = true
            response.code = result.code
            response.errorMessage = "code: ${result.code}. ${result.errorUtf8}"
        }else{
            response.body = networkMapper.mapToCourseNbrb(result.body.toString(), nbrbDate)
        }
        return response
    }
}