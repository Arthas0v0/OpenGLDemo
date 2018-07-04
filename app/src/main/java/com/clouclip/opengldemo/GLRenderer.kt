package com.clouclip.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer: GLSurfaceView.Renderer {
    lateinit var mSquare:Triangle
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        mSquare = Triangle()
    }
    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {

            // 创建一个vertex shader类型(GLES20.GL_VERTEX_SHADER)
            // 或fragment shader类型(GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES20.glCreateShader(type)

            // 将源码添加到shader并编译之
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }

}