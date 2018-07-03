package it.emperor.deviceusagestats.ui.network.gestures

import android.view.GestureDetector
import android.view.MotionEvent

class FlingGestureListener(val fling: (isDown: Boolean) -> Unit) : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (velocityY < -2000) {
            fling(false)
        } else if (velocityY > 2000) {
            fling(true)
        }
        return true
    }
}