package com.example.currencymir.presentation.ui.custome

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.currencymir.databinding.SkeletonViewBinding

class SkeletonView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding: SkeletonViewBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = SkeletonViewBinding.inflate(inflater, this, true)

        // Создаем анимацию прозрачности
        val animator = ObjectAnimator.ofFloat(binding.root, "alpha", 1f, 0.7f, 1f)
        animator.duration = 1000 // Продолжительность одной итерации (мс)
        animator.repeatCount = ObjectAnimator.INFINITE // Бесконечное повторение
        animator.start()
    }
}