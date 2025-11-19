package cn.ezandroid.lib.ezfilter.core.util

import android.opengl.GLES20

/**
 * ShaderHelper
 *
 * @author like
 * @date 2018-05-29
 */
object ShaderHelper {
    @JvmStatic
    fun compileShader(shaderSource: String?, shaderType: Int): Int {
        var errorInfo = "none"

        var shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource)
            GLES20.glCompileShader(shaderHandle)
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                errorInfo = GLES20.glGetShaderInfoLog(shaderHandle)
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }
        if (shaderHandle == 0) {
            throw RuntimeException("failed to compile shader. Reason: $errorInfo")
        }

        return shaderHandle
    }

    fun linkProgram(
        vertexShaderHandle: Int,
        fragmentShaderHandle: Int,
        attributes: Array<String>
    ): Int {
        var programHandle = GLES20.glCreateProgram()
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle)
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            for (i in attributes.indices) {
                GLES20.glBindAttribLocation(programHandle, i, attributes[i])
            }

            GLES20.glLinkProgram(programHandle)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }
        if (programHandle == 0) {
            throw RuntimeException("failed to link program.")
        }

        return programHandle
    }

    fun buildProgram(
        vertexShaderSource: String,
        fragmentShaderSource: String,
        attributes: Array<String>
    ): Int {
        val vertexShader = compileShader(vertexShaderSource, GLES20.GL_VERTEX_SHADER)
        val fragmentShader = compileShader(fragmentShaderSource, GLES20.GL_FRAGMENT_SHADER)
        return linkProgram(vertexShader, fragmentShader, attributes)
    }
}
