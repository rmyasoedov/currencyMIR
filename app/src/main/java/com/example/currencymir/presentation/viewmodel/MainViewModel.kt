package com.example.currencymir.presentation.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instrument.domain.model.ResponseModel
import com.instrument.domain.usecase.GetConvertInBlr
import com.instrument.domain.usecase.GetConvertInRus
import com.instrument.domain.usecase.GetConvertIncomeUseCase
import com.instrument.domain.usecase.LoadCourseBnbUseCase
import com.instrument.domain.usecase.LoadCourseMirUseCase
import com.instrument.domain.usecase.LoadNbrbCourseUseCase
import com.instrument.domain.usecase.LoadUsdUseCase
import com.instrument.domain.usecase.PackageVersionUseCase
import com.instrument.utils.Constant
import com.instrument.utils.UtilDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RequestState{
    data object Loading: RequestState()
    data class Success<T>(val data: T): RequestState()
    data class Error(val message: String) : RequestState()
}

enum class Course{
    USD, BNB, MIR, NBRB
}

class MainViewModel @Inject constructor(
    private val loadUsdUseCase: LoadUsdUseCase,
    private val loadCourseMirUseCase: LoadCourseMirUseCase,
    private val loadCourseBnbUseCase: LoadCourseBnbUseCase,
    private val loadNbrbCourseUseCase: LoadNbrbCourseUseCase,
    private val packageVersionUseCase: PackageVersionUseCase,
    private val getConvertIncomeUseCase: GetConvertIncomeUseCase,
    private val getConvertInRus: GetConvertInRus,
    private val getConvertInBlr: GetConvertInBlr
) : ViewModel() {

    private val _statusLoading = MutableLiveData<Map<Course, RequestState>>()
    val statusLoading: LiveData<Map<Course, RequestState>> get() = _statusLoading

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> get() = _appVersion

    private var dateCourseNbrb: String?=null
    private var courseNbrb: Float?=null
    private var courseMir: Float = 0f

    fun updateCourse(isStarted: Boolean = false){

        if(_statusLoading.value!=null && isStarted){
            _statusLoading.value = _statusLoading.value
            return
        }

        viewModelScope.launch {
            listOfNotNull(
                async { loadCourse(Course.USD, loadUsdUseCase::invoke) },
                async { loadCourse(Course.MIR, loadCourseMirUseCase::invoke) },
                if(Constant.BNB_LOADING){
                    async { loadCourse(Course.BNB, loadCourseBnbUseCase::invoke) }
                }else{
                    null
                },
                dateCourseNbrb?.let {
                    async { loadCourse(Course.NBRB){loadNbrbCourseUseCase.invoke(it)} }
                }
            ).awaitAll()
        }
    }

    fun loadCourseNbrb(date: String?=null){
        dateCourseNbrb = date ?: UtilDate.todayDate()
        viewModelScope.launch {
            async {
                loadCourse(Course.NBRB){
                    loadNbrbCourseUseCase.invoke(dateCourseNbrb!!)
                }
            }.await()
        }
    }

    private fun updateRequestState(course: Course, state: RequestState){
        _statusLoading.value = _statusLoading.value.orEmpty().toMutableMap().apply {
            this[course] = state
        }
    }

    private suspend fun <T>loadCourse(course: Course, useCase: suspend () -> ResponseModel<T>){
        updateRequestState(course, RequestState.Loading)
        val res = useCase()
        if(res.error){
            updateRequestState(course, RequestState.Error(res.errorMessage ?: "Undefined error"))
        }else{
            updateRequestState(course, RequestState.Success(res.body))
        }
    }

    fun setCourseNbrb(course: Float){
        courseNbrb = course
    }

    fun setCourseMir(course: Float?){
        courseMir = try {
            1 / (course ?: 0f)
        }catch (_:Exception){0f}
    }

    private val _convertZp = MutableLiveData<String>()
    val convertZp: LiveData<String> get() = _convertZp

    fun getConvertZp(inputValue: Float){
        _convertZp.value = getConvertIncomeUseCase.invoke(courseNbrb, inputValue)
    }

    private val _convertRus = MutableLiveData<String>()
    val convertRus: LiveData<String> get() = _convertRus

    fun getConvertRus(inputBlr: Float?){
        _convertRus.value = getConvertInRus.invoke(courseMir, inputBlr)
    }

    private val _convertBlr = MutableLiveData<String>()
    val convertBlr: LiveData<String> get() = _convertBlr

    fun getConvertBlr(inputRus: Float?){
        _convertBlr.value = getConvertInBlr.invoke(courseMir, inputRus)
    }

    fun getVersionNumber(){
        _appVersion.value = packageVersionUseCase.invoke()
    }

}