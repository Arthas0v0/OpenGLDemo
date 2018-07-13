package com.clouclip.opengldemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.*
import android.opengl.GLES20.*
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


class Square3(context: Context) {
    private var vertexBuffer: FloatBuffer? = null
    private var indiceBuffer: IntBuffer? = null
    private var mAPosition = 0
    private var mAColor = 0
    var mProgram = 0
    private val mProjectionMatrix = FloatArray(16)
    val va = IntArray(1)
    val texture = IntArray(1)
    val texture2 = IntArray(1)
    private val mRotationMatrix = FloatArray(16)
    var vertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
            0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

            -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
    )
    var indice = intArrayOf(
            0, 1, 3, // 第一个三角形
            1, 2, 3  // 第二个三角形
    )// 注意索引从0开始!

    val vertexShaderCode =
            "layout (location = 0) attribute vec3 a_Position;" +

                    "layout (location = 1) attribute vec2 aTexCoord;" +

                    "varying vec2 TexCoord;" +
                    "uniform mat4 model;" +
                    "uniform mat4 view;" +
                    "uniform mat4 projection;"+
                    "uniform mat4 ortho;"+
                    "void main() {" +
                    "    gl_Position =  projection * view * model * vec4(a_Position,1.0);" +

                    "    TexCoord = aTexCoord;" +
                    "}"
    val fragmentShaderCode =

                    "varying vec2 TexCoord;" +
                    "uniform sampler2D texture1;"+
                    "uniform sampler2D texture2;"+
                    "void main() {" +
                    "    gl_FragColor =  mix(texture2D(texture1, TexCoord), texture2D(texture2, TexCoord), 0.2);" +
                    "}"

    init {
        GLES30.glEnable(GL_DEPTH_TEST)
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
//        val aa = ByteBuffer.allocateDirect(
//                // (坐标数 * 4)float占四字节
//                indice.size * 4)
//        // 设用设备的本点字节序
//        aa.order(ByteOrder.nativeOrder())
//
//        // 从ByteBuffer创建一个浮点缓冲
//        indiceBuffer = aa.asIntBuffer()
//        // 把坐标们加入FloatBuffer中
//        indiceBuffer!!.put(indice)
//        // 设置buffer，从第一个坐标开始读
//        indiceBuffer!!.position(0)

        val vb = IntArray(1)
        val EBO = IntArray(1)

        GLES30.glGenVertexArrays(1, va, 0)
        GLES30.glGenBuffers(1, vb, 0)
        GLES30.glGenBuffers(1, EBO, 0)



        GLES30.glBindVertexArray(va[0])

        GLES30.glBindBuffer(GL_ARRAY_BUFFER, vb[0])
        GLES30.glBufferData(GL_ARRAY_BUFFER, 4 * vertexBuffer!!.limit(), vertexBuffer, GL_STATIC_DRAW)

     //   GLES30.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO[0])
      //  GLES30.glBufferData(GL_ELEMENT_ARRAY_BUFFER, 4 * indiceBuffer!!.limit(), indiceBuffer, GL_STATIC_DRAW)

        mAPosition = GLES30.glGetAttribLocation(mProgram, "a_Position")

        GLES30.glVertexAttribPointer(mAPosition, 3, GLES30.GL_FLOAT, false, 5 * 4, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * 4, 12)
        GLES30.glEnableVertexAttribArray(1)



        GLES30.glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D,texture[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        GLES30.glGenTextures(1, texture2, 0)
        val bitmap1 = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap1, 0)
        GLES30.glGenerateMipmap(GL_TEXTURE_2D)


        glBindTexture(GL_TEXTURE_2D,texture2[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        val bitmap2: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.face)
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap2, 0)
        GLES30.glGenerateMipmap(GL_TEXTURE_2D)


    }

    fun draw(mvpMatrix: FloatArray) {
        GLES30.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(mProgram)

        glActiveTexture(GL_TEXTURE0)
        GLES30.glBindTexture(GL_TEXTURE_2D, texture[0])
        GLES30.glUniform1i(glGetUniformLocation(mProgram, "texture1"), 0)

        glActiveTexture(GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, texture2[0])
        GLES30.glUniform1i(glGetUniformLocation(mProgram, "texture2"), 1)
        GLES30.glBindVertexArray(va[0])
        val modelHandle = GLES30.glGetUniformLocation(mProgram, "model")
        val viewHandle = GLES30.glGetUniformLocation(mProgram, "view")
        val projectionHandle = GLES30.glGetUniformLocation(mProgram, "projection")
        val orthoHandle = GLES30.glGetUniformLocation(mProgram, "ortho")
        var modelMatrix  = FloatArray(16)
        var orthoMatrix  = FloatArray(16)
        var viewMatrix  = FloatArray(16)
        var projectionMatrix  = FloatArray(16)
        var ans  = FloatArray(16)
        Matrix.setIdentityM(modelMatrix,0)
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setIdentityM(projectionMatrix,0)

        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.rotateM(modelMatrix, 0,angle, 0.5f, 1f, 0f)
        Matrix.translateM(viewMatrix,0,0f,0f,-3f)
        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f,0f, 3f)


        GLES30.glUniformMatrix4fv(orthoHandle, 1, false, orthoMatrix, 0)
        GLES30.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0)

        GLES30.glUniformMatrix4fv(viewHandle, 1, false, viewMatrix, 0)

        GLES30.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0)


        glDrawArrays(GL_TRIANGLES, 0, 36)

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)

//        GLES30.glDisableVertexAttribArray(mAPosition)
    }

    fun loadShader(type: Int, shaderCode: String): Int {

        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        return shader
    }

}