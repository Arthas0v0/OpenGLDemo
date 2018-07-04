package com.clouclip.opengldemo

import java.nio.FloatBuffer
import java.nio.ByteOrder.nativeOrder
import android.R.attr.order
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.ByteOrder
import android.opengl.GLES20
import com.clouclip.opengldemo.GLRenderer.Companion.loadShader


class Triangle {
    private var vertexBuffer: FloatBuffer? = null
    var mProgram = 0
    var mPositionHandle = 0
    val COORDS_PER_VERTEX = 3
    var mColorHandle = 0
    var triangleCoords = floatArrayOf(// 按逆时针方向顺序:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f    // bottom right
    )
    var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    fun Triangle() {
        val vertexShaderCode = "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "}"

        val fragmentShaderCode = "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram();             // 创建一个空的OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // 将vertex shader添加到program
        GLES20.glAttachShader(mProgram, fragmentShader); // 将fragment shader添加到program
        GLES20.glLinkProgram(mProgram);                  // 创建可执行的 OpenGL ES program
        // 为存放形状的坐标，初始化顶点字节缓冲

        val bb = ByteBuffer.allocateDirect(
                // (坐标数 * 4)float占四字节
                triangleCoords.size * 4)
        // 设用设备的本点字节序
        bb.order(ByteOrder.nativeOrder())

        // 从ByteBuffer创建一个浮点缓冲
        vertexBuffer = bb.asFloatBuffer()
        // 把坐标们加入FloatBuffer中
        vertexBuffer!!.put(triangleCoords)
        // 设置buffer，从第一个坐标开始读
        vertexBuffer!!.position(0)
    }

    fun draw() {
        // 将program加入OpenGL ES环境中
        GLES20.glUseProgram(mProgram)

        // 获取指向vertex shader的成员vPosition的 handle
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // 启用一个指向三角形的顶点数组的handle
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // 获取指向fragment shader的成员vColor的handle
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // 设置三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)

        // 画三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // 禁用指向三角形的顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }
}