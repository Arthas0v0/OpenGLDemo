package com.clouclip.opengldemo

import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Triangle2 {
    private var vertexBuffer: FloatBuffer? = null
   private var mAPosition = 0
    private var mAColor = 0
    var mProgram = 0
    var vertices = floatArrayOf(
            // 位置              // 颜色
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, // 右下
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, // 左下
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f    // 顶部
    )
    val vertexShaderCode =
            "layout (location = 0) attribute vec3 a_Position;"+
                    "layout (location = 1) attribute vec3 aColor;"+
                    "varying vec3  vertexColor;"+
                    "void main() {" +
                    "    gl_Position =  vec4(a_Position, 1.0);" +
                    "   vertexColor = aColor;"+
                    "}"
    val fragmentShaderCode = "varying vec3 vertexColor;"+
            "void main() {" +
            "    gl_FragColor = vec4(vertexColor, 1.0);" +
            "}"

    init {
        mProgram = GLES30.glCreateProgram()
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES30.glAttachShader(mProgram, vertexShader)
        GLES30.glAttachShader(mProgram, fragmentShader)
        GLES30.glLinkProgram(mProgram)

        val bb = ByteBuffer.allocateDirect(
                // (坐标数 * 4)float占四字节
                vertices.size * 4)
        // 设用设备的本点字节序
        bb.order(ByteOrder.nativeOrder())

        // 从ByteBuffer创建一个浮点缓冲
        vertexBuffer = bb.asFloatBuffer()
        // 把坐标们加入FloatBuffer中
        vertexBuffer!!.put(vertices)
        // 设置buffer，从第一个坐标开始读
        vertexBuffer!!.position(0)

        val vb = IntArray(1)
        val va = IntArray(1)
        // GLES30.glGenVertexArrays(1, va, 0)
        GLES30.glGenBuffers(1, vb, 0)
        //    GLES30. glBindVertexArray(va[0])
        GLES30.glBindBuffer(GL_ARRAY_BUFFER, vb[0])
        GLES30.glBufferData(GL_ARRAY_BUFFER, 4 * vertexBuffer!!.limit(), vertexBuffer, GL_STATIC_DRAW)
        mAPosition = GLES30.glGetAttribLocation(mProgram, "a_Position")
        mAColor = GLES30.glGetAttribLocation(mProgram, "aColor")
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false,  6 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 12)
        GLES30.glEnableVertexAttribArray(1)
    }

    fun draw(){

        GLES30.glUseProgram(mProgram)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
//        GLES30.glDisableVertexAttribArray(mAPosition)
    }
    fun loadShader(type: Int, shaderCode: String): Int {

        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        return shader
    }

}