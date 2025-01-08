package com.example.currencymir.presentation.ui

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.currencymir.app.App
import com.example.currencymir.presentation.ui.custome.SkeletonView
import com.example.currencymir.databinding.ActivityMainBinding
import com.example.currencymir.presentation.factory.MainViewModelFactory
import com.example.currencymir.presentation.viewmodel.Course
import com.example.currencymir.presentation.viewmodel.MainViewModel
import com.example.currencymir.presentation.viewmodel.RequestState
import com.instrument.domain.model.CourseBnbModel
import com.instrument.domain.model.CourseMirModel
import com.instrument.domain.model.CourseNbrbModel
import com.instrument.domain.model.UsdModel
import com.instrument.utils.Constant
import com.instrument.utils.UtilDate
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory
    private val mainViewModel: MainViewModel by viewModels{ mainViewModelFactory }

    private val inputRusZp: Float
        get() = binding.etInputRusZp?.text?.toString()?.toFloatOrNull() ?: 0f

    private val inputBlr: Float
        get() = binding.etInputBlr?.text?.toString()?.toFloatOrNull() ?: 0f

    private val inputRus: Float
        get() = binding.etInputRus?.text?.toString()?.toFloatOrNull() ?: 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mainViewModel.getVersionNumber()

        if(!Constant.BNB_LOADING){
            binding.bnbBlock.visibility = View.GONE
        }

        mainViewModel.appVersion.observe(this){version->
            binding.tvVersion.text = "v$version"
        }

        mainViewModel.convertZp.observe(this){
            binding.tvResultBlrZp?.text = it
        }

        mainViewModel.convertRus.observe(this){
            binding.tvResultRus?.text = it
        }

        mainViewModel.convertBlr.observe(this){
            binding.tvResultBlr?.text = it
        }

        mainViewModel.statusLoading.observe(this){ states  ->

            states.forEach { (course, state) ->
                when(state){
                    is RequestState.Error -> {
                        errorLoading(course, state.message)
                    }
                    is RequestState.Success<*> -> {
                        onSuccess(course, state.data)
                    }

                    is RequestState.Loading -> {
                        startLoading(course)
                    }
                }
            }
        }

        mainViewModel.updateCourse(isStarted = true)
    }

    private fun errorLoading(course: Course, errorMessage: String){
        when(course){
            Course.USD -> {
                binding.tvCourseUsd.text = "00.00"
                loadedData(binding.skeletonUsd, binding.tvCourseUsd)
            }
            Course.BNB -> {
                binding.tvCourseBnb.text = "00.00"
                binding.tvCourseBnb.setOnClickListener {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
                loadedData(binding.skeletonBnb, binding.tvCourseBnb)
            }
            Course.MIR -> {
                binding.tvCourseMir.text = "00.00"
                loadedData(binding.skeletonMir, binding.tvCourseMir)
            }
            Course.NBRB -> {
                binding.tvCourseNbrb?.text = "00.00"
                binding.blockInputNbRb?.isVisible = false
                binding.tvDateNbrb?.setOnClickListener {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
                loadedData(binding.skeletonNbrb!!, binding.tvCourseNbrb!!)
            }
        }
    }

    private fun startLoading(course: Course){
        when(course){
            Course.USD -> {
                binding.tvCourseUsd.setOnClickListener(null)
                binding.tvCourseUsd.isVisible = false
                binding.skeletonUsd.isVisible = true
            }
            Course.BNB -> {
                binding.tvCourseBnb.isVisible = false
                binding.skeletonBnb.isVisible = true
            }
            Course.MIR -> {
                binding.tvCourseMir.isVisible = false
                binding.skeletonMir.isVisible = true
            }
            Course.NBRB -> {
                binding.tvCourseNbrb?.isVisible = false
                binding.skeletonNbrb?.isVisible = true
                binding.tvDateNbrb?.setOnClickListener(null)
            }
        }
    }

    private fun onSuccess(course: Course, data: Any?){
        when(course){
            Course.USD -> {
                val response = data as UsdModel
                binding.tvCourseUsd.text = response.price.toString()
                loadedData(binding.skeletonUsd, binding.tvCourseUsd)
                binding.tvUsdLabel.text = "Курс доллара (${response.source})"
                binding.tvCourseUsd.setOnClickListener {
                    Toast.makeText(this, "${response.exchangePrice}%", Toast.LENGTH_LONG).show()
                }
            }
            Course.BNB -> {
                val response = data as CourseBnbModel
                binding.tvCourseBnb.text = response.sale.toString()
                binding.tvCourseBnb.setOnClickListener {
                    Toast.makeText(this@MainActivity, response.buy.toString(), Toast.LENGTH_LONG).show()
                }
                loadedData(binding.skeletonBnb, binding.tvCourseBnb)
            }
            Course.MIR -> {
                val response = data as CourseMirModel
                binding.tvCourseMir.text = response.price.toString()
                loadedData(binding.skeletonMir, binding.tvCourseMir)
                mainViewModel.setCourseMir(response.price)

                response.course100?.let {
                    binding.tvCourse100.text = "$it"
                }
                binding.tvCourseCommission.text = response.courseCommission.toString()
                response.fix500?.let { binding.tv500br.text = "$it б.р" }

                mainViewModel.getConvertRus(inputBlr)
                mainViewModel.getConvertBlr(inputRus)
            }
            Course.NBRB -> {
                val response = data as CourseNbrbModel
                response.price?.let {
                    mainViewModel.setCourseNbrb(it)
                    binding.tvCourseNbrb?.text = it.toString()
                }
                binding.tvDateNbrb?.text = "Курс НБ РБ на ${response.date}"
                mainViewModel.getConvertZp(inputRusZp)

                binding.nbrbBlock?.visibility = View.VISIBLE
                binding.blockInputNbRb?.visibility = View.VISIBLE

                if(binding.tvCourseNbrb!=null && binding.tvCourseNbrb!=null){
                    loadedData(binding.skeletonNbrb!!, binding.tvCourseNbrb!!)
                }
            }
        }
    }

    private fun loadedData(skeletonView: SkeletonView, textView: TextView){
         skeletonView.isVisible = false
         textView.isVisible = true
    }

    override fun onResume() {
        super.onResume()

        binding.swRefresh.setOnRefreshListener {
            binding.swRefresh.isRefreshing = false
            mainViewModel.updateCourse()
        }

        binding.tvSetNbRb?.setOnClickListener {
            mainViewModel.loadCourseNbrb()
            it.isVisible = false
        }

        binding.etInputBlr?.addTextChangedListener {
            mainViewModel.getConvertRus(inputBlr)
        }

        binding.etInputRus?.addTextChangedListener {
            mainViewModel.getConvertBlr(inputRus)
        }

        binding.etInputRusZp?.addTextChangedListener {
            mainViewModel.getConvertZp(inputRusZp)
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
                    val selectedDate = UtilDate.formatIntToDate(selectedYear, selectedMonth, selectedDayOfMonth)
                    mainViewModel.loadCourseNbrb(selectedDate)
                },
                year, month, dayOfMonth
            )

            // Показать DatePickerDialog
            datePickerDialog.show()
        }
    }
}