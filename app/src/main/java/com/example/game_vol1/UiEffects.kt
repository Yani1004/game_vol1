package com.example.game_vol1

import android.view.MotionEvent
import android.view.View

fun View.applyPressFeedback() {
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(90L)
                .start()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(120L)
                .start()
        }
        false
    }
}

fun View.fadeSlideIn(delayMs: Long = 0L) {
    alpha = 0f
    translationY = resources.displayMetrics.density * 12f
    animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delayMs)
        .setDuration(220L)
        .start()
}
