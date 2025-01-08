package com.instrument.domain.usecase

import com.instrument.domain.model.CourseBnbModel
import com.instrument.domain.model.CourseMirModel
import com.instrument.domain.model.CourseNbrbModel
import com.instrument.domain.model.ResponseModel
import com.instrument.domain.model.UsdModel
import com.instrument.domain.repository.RetrofitRepository
import javax.inject.Inject


class LoadUsdUseCase @Inject constructor(
    private val retrofitRepository: RetrofitRepository
) {

    suspend fun invoke(): ResponseModel<UsdModel>{
        return retrofitRepository.getCourseUsdData()
    }
}

class LoadCourseMirUseCase @Inject constructor(
    private val retrofitRepository: RetrofitRepository
){
    suspend fun invoke(): ResponseModel<CourseMirModel>{
        return retrofitRepository.getCourseMirData()
    }
}

class LoadCourseBnbUseCase @Inject constructor(
    private val retrofitRepository: RetrofitRepository
){
    suspend fun invoke(): ResponseModel<CourseBnbModel>{
        return retrofitRepository.getCourseBnbData()
    }
}

class LoadNbrbCourseUseCase @Inject constructor(
    private val retrofitRepository: RetrofitRepository
){
    suspend fun invoke(nbrbDate: String): ResponseModel<CourseNbrbModel>{
        return retrofitRepository.getNbrbCourseData(nbrbDate)
    }
}