package com.clouclip.opengldemo

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.R.attr.y
import android.R.attr.x



class MyGLSurfaceView(context: Context?) : GLSurfaceView(context) {
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private var mPreviousX: Float = 0.toFloat()
    private var mPreviousY: Float = 0.toFloat()
      var mRenderer:GLRenderer = GLRenderer()

    init {
        setEGLContextClientVersion(3)
        mRenderer.mContext = context
        setRenderer(mRenderer)

     //   renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y
        when(event.action){
            MotionEvent.ACTION_MOVE->{
                // reverse direction of rotation above the mid-line
                var dx = x - mPreviousX
                var dy = y - mPreviousY
                if (y > height / 2) {
                    dx *= -1
                }

                // reverse direction of rotation to left of the mid-line
                if (x < width / 2) {
                    dy *= -1
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR))
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y
        return true
    }
}