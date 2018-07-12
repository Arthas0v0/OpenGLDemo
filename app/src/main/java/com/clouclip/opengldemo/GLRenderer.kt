package com.clouclip.opengldemo

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.os.SystemClock



class GLRenderer: GLSurfaceView.Renderer {
    private var mTriangle: Square3? = null
    private var mSquare: Square? = null
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)
    var mContext:Context? = null
    @Volatile
    var mAngle: Float = 0.toFloat()

    fun getAngle(): Float {
        return mAngle
    }

    fun setAngle(angle: Float) {
        mAngle = angle
    }

    override fun onDrawFrame(gl: GL10?) {
        val scratch = FloatArray(16)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -7f, 0f, 0f, 0f, 0f, -1.0f, 0.0f)


        // Calculate the projection and view transformation
     //   Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
    //    Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, -1.0f)
       // Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0f, 0f, -1.0f)
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
     //   Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)
        // Draw shape
        //Matrix.translateM(mMVPMatrix, 0, 1f, 1f, 0f)
        mTriangle!!.draw(mMVPMatrix)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        mTriangle = Square3(mContext!!)
        mSquare = Square()
    }

}