package com.clouclip.opengldemo

import android.graphics.Camera
import android.graphics.Shader
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Light {
    private var vertexBuffer: FloatBuffer? = null
    var vertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,

            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f,

            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,

            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f
    )
    var mProgram = 0
    var mProgram2 = 0
    val va = IntArray(1)
    val lightVAO = IntArray(1)
    val vertexShaderCode =
                    "layout (location = 0) attribute vec3 a_Position;" +
                    "layout (location = 1) attribute vec3 aNormal;" +
                    "varying vec3 Normal;" +
                    "varying vec3 FragPos;" +
                    "uniform mat4 model;" +
                    "uniform mat4 view;" +
                    "uniform mat4 projection;" +
                    "void main() {" +
                    "gl_Position =  projection * view * model * vec4(a_Position,1.0);" +
                    "FragPos = vec3(model * vec4(a_Position, 1.0));" +
                    "Normal =  mat3(model) * aNormal;" +
                    "}"
    val fragmentShaderCode =

            "uniform vec3 objectColor;" +
                    "varying vec3 Normal;" +
                    "varying vec3 FragPos;" +
                    "uniform vec3 lightColor;" +
                    "uniform vec3 lightPos;" +
                    "uniform vec3 viewPos;"+
                    "void main() {" +
                    "  float ambientStrength = 0.1;" +

                    "    vec3 ambient = ambientStrength * lightColor;" +

                    "vec3 norm = normalize(Normal);" +
                    "vec3 lightDir = normalize(lightPos - FragPos);" +
                    "vec3 viewDir = normalize(viewPos - FragPos);" +
                    "vec3 reflectDir = reflect(-lightDir, norm);" +
                    "float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);" +
                    "float specularStrength = 0.5;" +
                    "vec3 specular = specularStrength * spec * lightColor;"+
                    "float diff = max(dot(norm, lightDir), 0.0);" +
                    "vec3 diffuse = diff * lightColor;" +
                    "vec3 result = (ambient + diffuse+ specular) * objectColor;" +
                    "gl_FragColor = vec4(result, 1.0);" +

                    "}"
    val fragmentShaderCode2 =

                    "void main() {" +
                    "    gl_FragColor = vec4(1.0f,1.0f,1.0f,1.0f);" +

                    "}"
    var camera = Camera()

    init {


        mProgram = GLES30.glCreateProgram()
        mProgram2 = GLES30.glCreateProgram()
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val fragmentShader2 = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode2)
        GLES30.glAttachShader(mProgram, vertexShader)
        GLES30.glAttachShader(mProgram, fragmentShader)

        GLES30.glLinkProgram(mProgram)
//
        GLES30.glAttachShader(mProgram2, vertexShader)

        GLES30.glAttachShader(mProgram2, fragmentShader2)
        GLES30.glLinkProgram(mProgram2)
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

        GLES30.glGenVertexArrays(1, va, 0)
        GLES30.glGenBuffers(1, vb, 0)
        GLES30.glBindVertexArray(va[0])
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vb[0])
        GLES30.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * vertexBuffer!!.limit(), vertexBuffer, GLES20.GL_STATIC_DRAW)
        Log.e("openglDemo",vertexBuffer!!.limit().toString())
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * 4, 12)
        GLES30.glEnableVertexAttribArray(1)


        GLES30.glGenVertexArrays(1, lightVAO, 0)
        GLES30.glBindVertexArray(lightVAO[0])
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vb[0])
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
    }

    fun draw() {
        GLES30.glEnable(GLES20.GL_DEPTH_TEST)
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(mProgram)

        var modelHandle = GLES30.glGetUniformLocation(mProgram, "model")
        var viewHandle = GLES30.glGetUniformLocation(mProgram, "view")
        var projectionHandle = GLES30.glGetUniformLocation(mProgram, "projection")

        var modelMatrix = FloatArray(16)
        var viewMatrix = FloatArray(16)
        var projectionMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)

        var time = SystemClock.uptimeMillis() % 4000L
        var angle = 0.090f * time.toInt()

        Matrix.translateM(viewMatrix, 0, -0.2f, 0f, -3f)
        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 1f, 50f)
        Matrix.invertM(modelMatrix,0,modelMatrix,0)
        Matrix.transposeM(modelMatrix,0,modelMatrix,0)
        Matrix.rotateM(modelMatrix, 0, 45f, 0.5f, 1f, 0f)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        GLES30.glUniformMatrix4fv(viewHandle, 1, false, viewMatrix, 0)

        GLES30.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0)

        val objectColorHandle = GLES30.glGetUniformLocation(mProgram, "objectColor")
        val lightColorHandle = GLES30.glGetUniformLocation(mProgram, "lightColor")
        val lightPosHandle = GLES30.glGetUniformLocation(mProgram, "lightPos")
        var viewPosHandle = GLES30.glGetUniformLocation(mProgram, "viewPos")
        GLES30.glUniform3f(objectColorHandle, 1.0f, 0.5f, 0.31f)
        GLES30.glUniform3f(lightColorHandle, 1.0f, 1.0f, 1.0f)
        GLES30.glUniform3f(lightPosHandle, 1.2f, 1.0f, 2.0f)
        GLES30.glUniform3f(viewPosHandle, 0f, 0f, 0f)
        GLES30.glBindVertexArray(va[0])
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        //      GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glUseProgram(mProgram2)
        modelHandle = GLES30.glGetUniformLocation(mProgram2, "model")
        viewHandle = GLES30.glGetUniformLocation(mProgram2, "view")
        projectionHandle = GLES30.glGetUniformLocation(mProgram2, "projection")
        modelMatrix = FloatArray(16)
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)

        time = SystemClock.uptimeMillis() % 4000L
        angle = 0.090f * time.toInt()
        Matrix.rotateM(modelMatrix, 0, 45f, 0.5f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        Matrix.translateM(viewMatrix, 0, 1f, 0f, -3f)
        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 1f, 50f)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        GLES30.glUniformMatrix4fv(viewHandle, 1, false, viewMatrix, 0)

        GLES30.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0)
        GLES30.glBindVertexArray(lightVAO[0])
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
//


//        GLES30.glDisableVertexAttribArray(mAPosition)
    }

    fun loadShader(type: Int, shaderCode: String): Int {

        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        return shader
    }
}