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
import kotlin.math.sin

class Light2 {
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
                    "gl_Position =   projection * view * model *vec4(a_Position,1.0);" +
                    "FragPos = vec3(model * vec4(a_Position, 1.0));" +
                    "Normal =  mat3(model) * aNormal;" +
                    "}"
    val fragmentShaderCode =

            "uniform vec3 objectColor;" +
                    "varying vec3 Normal;" +
                    "varying vec3 FragPos;" +
                    "uniform vec3 lightColor;" +
                    "uniform vec3 lightPos;" +
                    "uniform vec3 viewPos;" +
                    "struct Material {" +
                    "    vec3 ambient;" +
                    "    vec3 diffuse;" +
                    "    vec3 specular;" +
                    "    float shininess;" +
                    "}; " +
                    "uniform Material material;" +
                    "struct Light {" +
                    "    vec3 position;" +
                    "    vec3 ambient;" +
                    "    vec3 diffuse;" +
                    "    vec3 specular;" +
                    "};" +
                    "uniform Light light;" +
                    "void main() {" +
                    "  float ambientStrength = 0.1;" +
                    "  vec3 ambient = light.ambient  * material.ambient;" +

                    " vec3 norm = normalize(Normal);" +
                    "vec3 lightDir = normalize(light.position - FragPos);" +
                    "float diff = max(dot(norm, lightDir), 0.0);" +
                    "vec3 diffuse = light.diffuse * (diff * material.diffuse);" +

                    "vec3 viewDir = normalize(viewPos - FragPos);" +
                    "vec3 reflectDir = reflect(-lightDir, norm);" +
                    "float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);" +
                    "vec3 specular = light.specular * (spec * material.specular);  " +

                    "vec3 result = (ambient + diffuse+ specular) * objectColor;" +
                    "gl_FragColor = vec4(result, 1.0);" +

                    "}"
    val fragmentShaderCode2 =

            "void main() {" +
                    "    gl_FragColor = vec4(1.0f,1.0f,1.0f,1.0f);" +

                    "}"

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
        Log.e("openglDemo", vertexBuffer!!.limit().toString())
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

    fun draw(mvpMatrix: FloatArray) {
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


        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 1f, 50f)

        Matrix.transposeM(modelMatrix, 0, modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, 45f, 0.5f, 1f, 0f)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        GLES30.glUniformMatrix4fv(viewHandle, 1, false, mvpMatrix, 0)

        GLES30.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0)
        val lightPosHandle = GLES30.glGetUniformLocation(mProgram, "light.position")
        val viewPosHandle = GLES30.glGetUniformLocation(mProgram, "viewPos")


        val objectColorHandle = GLES30.glGetUniformLocation(mProgram, "objectColor")
        val lightColorHandle = GLES30.glGetUniformLocation(mProgram, "lightColor")

        val ambientHandle = GLES30.glGetUniformLocation(mProgram, "material.ambient")
        val diffuseHandle = GLES30.glGetUniformLocation(mProgram, "material.diffuse")
        val specularHandle = GLES30.glGetUniformLocation(mProgram, "material.specular")
        val shininessHandle = GLES30.glGetUniformLocation(mProgram, "material.shininess")

        val lightambientHandle = GLES30.glGetUniformLocation(mProgram, "light.ambient")
        val lightdiffuseHandle = GLES30.glGetUniformLocation(mProgram, "light.diffuse")
        val lightspecularHandle = GLES30.glGetUniformLocation(mProgram, "light.specular")

        GLES30.glUniform3f(objectColorHandle, 1.0f, 0.5f, 0.31f)
        GLES30.glUniform3f(lightColorHandle, 1.0f, 1.0f, 1.0f)
        GLES30.glUniform3f(lightPosHandle, 1.2f, 1.0f, 2.0f)
        GLES30.glUniform3f(viewPosHandle, 0f, 0f, 0f)
        GLES30.glUniform3f(ambientHandle, 1.0f, 0.5f, 0.31f)
        GLES30.glUniform3f(diffuseHandle, 1.0f, 0.5f, 0.31f)
        GLES30.glUniform3f(specularHandle, 0.5f, 0.5f, 0.5f)
        GLES30.glUniform1f(shininessHandle, 32f)

        GLES30.glUniform3f(lightambientHandle, sin(time%10000/4000f*2f)*0.5f ,  sin(time%10000/1000f*0.7f)*0.5f,  sin(time%10000/1000f*1.3f)*0.5f)
        GLES30.glUniform3f(lightdiffuseHandle,  sin(time%10000/4000f*2f)*0.5f*0.2f ,  sin(time%10000/1000f*0.7f)*0.5f*0.2f,  sin(time%10000/1000f*1.3f)*0.5f*0.2f)
        GLES30.glUniform3f(lightspecularHandle, 1.0f, 1.0f, 1.0f)

        GLES30.glBindVertexArray(va[0])
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        //      GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glUseProgram(mProgram2)
        modelHandle = GLES30.glGetUniformLocation(mProgram2, "model")
        viewHandle = GLES30.glGetUniformLocation(mProgram2, "view")
        projectionHandle = GLES30.glGetUniformLocation(mProgram2, "projection")

        modelMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)


        Matrix.translateM(modelMatrix, 0, -0.2f, 0f, -3f)
        Matrix.rotateM(modelMatrix, 0, 45f, 0.5f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, modelMatrix, 0, 0.2f, 0.2f, 0.2f)

        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 1f, 50f)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        GLES30.glUniformMatrix4fv(viewHandle, 1, false, mvpMatrix, 0)

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