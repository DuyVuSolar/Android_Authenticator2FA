package cn.ezandroid.lib.ezfilter.core

import android.opengl.GLES20
import android.text.TextUtils
import android.util.Log
import cn.ezandroid.lib.ezfilter.core.util.L
import cn.ezandroid.lib.ezfilter.core.util.ShaderHelper.compileShader
import cn.ezandroid.lib.ezfilter.core.util.ShaderHelper.linkProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.LinkedList
import java.util.Queue

/**
 * 滤镜核心类
 *
 *
 * 实现了OpenGL的渲染逻辑，是所有渲染器的父类
 *
 * @author like
 * @date 2017-09-15
 */
open class GLRender : OnTextureAcceptableListener {
    /**
     * 设置顶点着色器
     *
     * @param vertexShader
     */
    protected var vertexShader: String = DEFAULT_VERTEX_SHADER

    /**
     * 设置片元着色器
     *
     * @param fragmentShader
     */
    protected open var fragmentShader: String = DEFAULT_FRAGMENT_SHADER

    protected var mCurrentRotation: Int = 0

    protected var mWorldVertices: FloatBuffer? = null

    @JvmField
    protected var mTextureVertices: Array<FloatBuffer?> = emptyArray()

    protected var mVertexShaderHandle: Int = 0
    protected var mFragmentShaderHandle: Int = 0
    @JvmField
    protected var mTextureHandle: Int = 0
    protected var mPositionHandle: Int = 0
    protected var mTextureCoordHandle: Int = 0

    @JvmField
    var mProgramHandle: Int = 0

    @JvmField
    protected var mTextureIn: Int = 0

    @JvmField
    protected var mWidth: Int = 0
    @JvmField
    protected var mHeight: Int = 0

    private var mCustomSizeSet = false
    private var mInitialized = false
    @JvmField
    protected var mSizeChanged: Boolean = false

    @JvmField
    protected val mRunOnDraw: Queue<Runnable>
    @JvmField
    protected val mRunOnDrawEnd: Queue<Runnable>

    /**
     * 获取Fps
     *
     * @return
     */
    var fps: Int = 0
        protected set
    private var mLastTime: Long = 0
    private var mFrameCount = 0

    init {
        initWorldVertices()
        initTextureVertices()

        mRunOnDraw = LinkedList()
        mRunOnDrawEnd = LinkedList()
    }

    /**
     * 初始化世界坐标系顶点
     */
    protected fun initWorldVertices() {
        // (-1, 1) -------> (1,1)
        //      ^
        //       \\
        //         (0,0)
        //           \\
        //             \\
        // (-1,-1) -------> (1,-1)
        val vertices = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        mWorldVertices = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mWorldVertices?.put(vertices)?.position(0)
    }

    /**
     * 设置世界坐标系
     *
     * @param worldVertices
     */
    fun setWorldVertices(worldVertices: FloatBuffer?) {
        mWorldVertices = worldVertices
    }

    /**
     * 初始化纹理坐标系顶点，默认为填充模式
     */
    protected open fun initTextureVertices() {
        mTextureVertices = arrayOfNulls(4)

        // (0,1) -------> (1,1)
        //     ^
        //      \\
        //        \\
        //          \\
        //            \\
        // (0,0) -------> (1,0)
        // 正向纹理坐标
        val texData0 = floatArrayOf(
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f,
        )
        mTextureVertices[0] = ByteBuffer.allocateDirect(texData0.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[0]?.put(texData0)?.position(0)

        // 顺时针旋转90°的纹理坐标
        val texData1 = floatArrayOf(
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 1.0f,
        )
        mTextureVertices[1] = ByteBuffer.allocateDirect(texData1.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[1]?.put(texData1)?.position(0)

        // 顺时针旋转180°的纹理坐标
        val texData2 = floatArrayOf(
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 0.0f,
        )
        mTextureVertices[2] = ByteBuffer.allocateDirect(texData2.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[2]?.put(texData2)?.position(0)

        // 顺时针旋转270°的纹理坐标
        val texData3 = floatArrayOf(
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f,
        )
        mTextureVertices[3] = ByteBuffer.allocateDirect(texData3.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[3]?.put(texData3)?.position(0)
    }

    /**
     * 设置纹理坐标系
     *
     * @param textureVertices
     */
    fun setTextureVertices(textureVertices: Array<FloatBuffer?>) {
        mTextureVertices = textureVertices
    }

    var width: Int
        /**
         * 获取渲染高度
         *
         * @return
         */
        get() = mWidth
        /**
         * 设置渲染宽度
         *
         *
         * 调用setRenderSize后，再调用该方法无效
         *
         * @param width
         */
        protected set(width) {
            if (!mCustomSizeSet && this.mWidth != width) {
                this.mWidth = width
                mSizeChanged = true
            }
        }

    var height: Int
        /**
         * 获取渲染宽度
         *
         * @return
         */
        get() = mHeight
        /**
         * 设置渲染高度
         *
         *
         * 调用setRenderSize后，再调用该方法无效
         *
         * @param height
         */
        protected set(height) {
            if (!mCustomSizeSet && this.mHeight != height) {
                this.mHeight = height
                mSizeChanged = true
            }
        }

    var rotate90Degrees: Int
        /**
         * 获取顺时针旋转90度的次数
         *
         * @return
         */
        get() = mCurrentRotation
        /**
         * 设置顺时针旋转90度的次数
         *
         * @param numOfTimes
         */
        set(numOfTimes) {
            var numOfTimes = numOfTimes
            while (numOfTimes < 0) {
                numOfTimes = 4 + numOfTimes
            }
            mCurrentRotation += numOfTimes
            mCurrentRotation = mCurrentRotation % 4
        }

    /**
     * 重置旋转角为0
     *
     * @return
     */
    fun resetRotate(): Boolean {
        if (mCurrentRotation % 2 == 1) {
            mCurrentRotation = 0
            return true
        }
        mCurrentRotation = 0
        return false
    }

    /**
     * 设置渲染尺寸
     *
     *
     * 调用setRenderSize后，再调用setWidth或setHeight无效
     *
     * @param width
     * @param height
     */
    open fun setRenderSize(width: Int, height: Int) {
        mCustomSizeSet = true
        this.mWidth = width
        this.mHeight = height
        mSizeChanged = true
    }

    /**
     * 交换宽高
     */
    fun swapWidthAndHeight() {
        val temp = mWidth
        this.mWidth = mHeight
        this.mHeight = temp
        mSizeChanged = true
    }

    /**
     * 初始化参数句柄
     */
    protected open fun initShaderHandles() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_POSITION)
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_TEXTURE_COORD)

        mTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, UNIFORM_TEXTURE_0)
        Log.e("DUYVD initShaderHandles", "mTextureHandle:$mProgramHandle")
    }

    /**
     * 绑定顶点
     */
    protected fun bindShaderVertices() {
        mWorldVertices?.position(0)
        GLES20.glVertexAttribPointer(
            mPositionHandle, 2, GLES20.GL_FLOAT, false,
            8, mWorldVertices
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mTextureVertices[mCurrentRotation]?.position(0)
        GLES20.glVertexAttribPointer(
            mTextureCoordHandle, 2, GLES20.GL_FLOAT, false,
            8, mTextureVertices[mCurrentRotation]
        )
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
    }

    /**
     * 绑定纹理
     */
    protected open fun bindShaderTextures() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIn)
        GLES20.glUniform1i(mTextureHandle, 0)
    }

    /**
     * 绑定顶点、纹理等
     */
    protected open fun bindShaderValues() {
        bindShaderVertices()
        bindShaderTextures()
    }

    /**
     * 重置渲染器
     */
    open fun reInit() {
        mInitialized = false
    }

    protected fun logDraw() {
        Log.e("RenderDraw", toString() + " Fps:" + fps)
    }

    override fun toString(): String {
        return super.toString() + "[" + mWidth + "x" + mHeight + "]"
    }

    /**
     * 必须在GL线程执行
     */
    open fun onDrawFrame() {
        if (!mInitialized) {
            initGLContext()
            mInitialized = true
        }
        if (mSizeChanged) {
            onRenderSizeChanged()
        }
        runAll(mRunOnDraw)
        drawFrame()
        runAll(mRunOnDrawEnd)

        mSizeChanged = false // 在drawFrame执行后再重置状态，因为drawFrame中可能用到该状态

        if (L.LOG_RENDER_DRAW) {
            logDraw()
        }

        calculateFps()
    }

    /**
     * 计算FPS
     */
    private fun calculateFps() {
        if (mLastTime == 0L) {
            mLastTime = System.currentTimeMillis()
        }
        mFrameCount++
        if (System.currentTimeMillis() - mLastTime >= 1000) {
            mLastTime = System.currentTimeMillis()
            fps = mFrameCount
            mFrameCount = 0
        }
    }

    protected open fun drawFrame() {
        if (mTextureIn == 0) {
            return
        }
        if (mWidth != 0 && mHeight != 0) {
            GLES20.glViewport(0, 0, mWidth, mHeight)
        }

        GLES20.glUseProgram(mProgramHandle)

        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        bindShaderValues()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    protected val shaderAttributes: Array<String>
        /**
         * 获取Attribute参数数组
         */
        get() = arrayOf(
            ATTRIBUTE_POSITION,
            ATTRIBUTE_TEXTURE_COORD
        )

    /**
     * 初始化OpenGL上下文
     */
    protected open fun initGLContext() {
        val vertexShader = vertexShader
        val fragmentShader = fragmentShader

        if (!TextUtils.isEmpty(vertexShader) && !TextUtils.isEmpty(fragmentShader)) {
            // 初始化顶点着色器
            mVertexShaderHandle = compileShader(vertexShader, GLES20.GL_VERTEX_SHADER)
            // 初始化片元着色器
            mFragmentShaderHandle = compileShader(fragmentShader, GLES20.GL_FRAGMENT_SHADER)

            // 将顶点着色器和片元着色器链接到OpenGL渲染程序
            mProgramHandle = linkProgram(
                mVertexShaderHandle, mFragmentShaderHandle,
                shaderAttributes
            )
        }

        initShaderHandles()
    }

    /**
     * 当渲染尺寸改变时调用
     */
    protected open fun onRenderSizeChanged() {
    }

    protected fun logDestroy() {
        Log.e("RenderDestroy", toString() + " Thread:" + Thread.currentThread().name)
    }

    /**
     * 必须在GL线程执行，释放纹理等OpenGL资源
     */
    open fun destroy() {
        mInitialized = false
        if (mProgramHandle != 0) {
            GLES20.glDeleteProgram(mProgramHandle)
            mProgramHandle = 0
        }
        if (mVertexShaderHandle != 0) {
            GLES20.glDeleteShader(mVertexShaderHandle)
            mVertexShaderHandle = 0
        }
        if (mFragmentShaderHandle != 0) {
            GLES20.glDeleteShader(mFragmentShaderHandle)
            mFragmentShaderHandle = 0
        }

        if (L.LOG_RENDER_DESTROY) {
            logDestroy()
        }
    }

    protected fun runAll(queue: Queue<Runnable>) {
        synchronized(queue) {
            while (!queue.isEmpty()) {
                queue.poll().run()
            }
        }
    }

    fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) {
            mRunOnDraw.add(runnable)
        }
    }

    fun runOnDrawEnd(runnable: Runnable) {
        synchronized(mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable)
        }
    }

    override fun onTextureAcceptable(texture: Int, source: GLRender) {
        mTextureIn = texture
        width = source.width
        height = source.height
        onDrawFrame()
    }

    companion object {


        const val ATTRIBUTE_POSITION: String = "position"
        const val ATTRIBUTE_TEXTURE_COORD: String = "inputTextureCoordinate"//aTexCoord
        const val VARYING_TEXTURE_COORD: String = "textureCoordinate"//vTexCoord
        const val UNIFORM_TEXTURE: String = "inputImageTexture"//uTexture
        const val UNIFORM_TEXTURE_0: String = UNIFORM_TEXTURE

        const val DEFAULT_VERTEX_SHADER: String = ("uniform mat4 u_Matrix" + ";\n" +
                "attribute vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "attribute vec2 " + ATTRIBUTE_TEXTURE_COORD + ";\n"
                + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
                + "void main() {\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
//                + "   gl_Position = " + "u_Matrix"  + " * " + ATTRIBUTE_POSITION + ";\n"

                + "  " + VARYING_TEXTURE_COORD + " = " + ATTRIBUTE_TEXTURE_COORD + ";\n"

                + "}\n")

        const val DEFAULT_FRAGMENT_SHADER: String = ("""precision mediump float;
uniform sampler2D $UNIFORM_TEXTURE_0;
varying vec2 $VARYING_TEXTURE_COORD;
void main(){
   gl_FragColor = texture2D($UNIFORM_TEXTURE_0,$VARYING_TEXTURE_COORD);
}
""")
    }
}
