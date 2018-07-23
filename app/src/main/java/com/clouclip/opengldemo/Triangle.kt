package com.clouclip.opengldemo

import java.nio.FloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.opengl.GLES30


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

    init{

        val vertexShaderCode =
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

        val fragmentShaderCode = "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        // create empty OpenGL ES Program
        mProgram = GLES30.glCreateProgram()

        // add the vertex shader to program
        GLES30.glAttachShader(mProgram, vertexShader)

        // add the fragment shader to program
        GLES30.glAttachShader(mProgram, fragmentShader)

        // creates OpenGL ES program executables
        GLES30.glLinkProgram(mProgram)

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

    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        val shader = GLES30.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        return shader
    }



    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member

        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor")

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0)

        val mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle)
    }
}