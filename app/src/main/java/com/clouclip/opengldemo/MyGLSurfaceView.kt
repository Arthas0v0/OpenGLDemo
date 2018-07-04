package com.clouclip.opengldemo

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context?) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)
        setRenderer(GLRenderer())
    }

}