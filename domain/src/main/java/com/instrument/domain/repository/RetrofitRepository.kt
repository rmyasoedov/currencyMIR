package com.instrument.domain.repository

import com.instrument.domain.model.CourseBnbModel
import com.instrument.domain.model.CourseMirModel
import com.instrument.domain.model.CourseNbrbModel
import com.instrument.domain.model.ResponseModel
import com.instrument.domain.model.UsdModel

interface RetrofitRepository {
    suspend fun getCourseUsdData(): ResponseModel<UsdModel>
    suspend fun getCourseMirData(): ResponseModel<CourseMirModel>
    suspend fun getCourseBnbData(): ResponseModel<CourseBnbModel>
    suspend fun getNbrbCourseData(nbrbDate: String): ResponseModel<CourseNbrbModel>
}