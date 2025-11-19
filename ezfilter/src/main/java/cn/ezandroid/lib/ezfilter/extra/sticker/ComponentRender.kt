package cn.ezandroid.lib.ezfilter.extra.sticker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.GLES20
import cn.ezandroid.lib.ezfilter.core.cache.IBitmapCache
import cn.ezandroid.lib.ezfilter.core.util.BitmapUtil
import cn.ezandroid.lib.ezfilter.extra.sticker.model.Component
import cn.ezandroid.lib.ezfilter.extra.sticker.model.ScreenAnchor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 贴纸组件渲染器
 *
 * @author like
 * @date 2018-01-09
 */
class ComponentRender(private val mContext: Context, private val mComponent: Component) {
    private var mBitmapCache: IBitmapCache? = null

    private var mTexture = 0

    // 上一帧序号
    private var mLastIndex = -1

    private var mStartTime: Long = -1

    // 渲染顶点坐标

    // 4个顶点，每个顶点由x，y两个float变量组成，每个float占4字节，总共32字节
    private val mRenderVertices: FloatBuffer = ByteBuffer.allocateDirect(32)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    // 显示锚点
    private var mScreenAnchor: ScreenAnchor? = null

    /**
     * 设置图片缓存
     *
     * @param bitmapCache
     */
    fun setBitmapCache(bitmapCache: IBitmapCache?) {
        mBitmapCache = bitmapCache
    }

    /**
     * 设置显示锚点
     *
     * @param screenAnchor
     */
    fun setScreenAnchor(screenAnchor: ScreenAnchor?) {
        mScreenAnchor = screenAnchor
    }

    /**
     * 更新渲染顶点坐标
     *
     *
     * 在GL线程调用
     *
     * @param width
     * @param height
     */
    fun updateRenderVertices(width: Int, height: Int) {
        val screenLeftPoint = mScreenAnchor!!.leftAnchorPoint
        val screenRightPoint = mScreenAnchor!!.rightAnchorPoint
        val textureLeftPoint = mComponent.textureAnchor!!.leftAnchorPoint
        val textureRightPoint = mComponent.textureAnchor!!.rightAnchorPoint

        var w = mComponent.width.toFloat()
        var h = mComponent.height.toFloat()

        // 计算屏幕两点距离与贴纸对应的两点之间距离的比例，并等比缩放贴纸
        val rate = distanceOf(screenLeftPoint, screenRightPoint) / distanceOf(
            textureLeftPoint,
            textureRightPoint
        )
        textureLeftPoint.x *= rate
        textureLeftPoint.y *= rate
        textureRightPoint.x *= rate
        textureRightPoint.y *= rate
        w *= rate
        h *= rate

        //Xác định tọa độ của bốn đỉnh của nhãn dán
        var leftTop = PointF(screenLeftPoint.x - textureLeftPoint.x, screenLeftPoint.y + textureLeftPoint.y)
        var leftBottom = PointF(leftTop.x, leftTop.y - h)
        var rightTop = PointF(leftTop.x + w, leftTop.y)
        var rightBottom = PointF(rightTop.x, leftBottom.y)

        // Tính toán góc quay
        var angle: Double
        if (mScreenAnchor!!.roll == ScreenAnchor.INVALID_VALUE.toFloat()) {
            // Điểm quay này là tọa độ của screenRightPoint khi góc quay là 0
            val beforeRotatePoint = PointF(leftTop.x + textureRightPoint.x, leftTop.y - textureRightPoint.y)
            // Tính góc quay dựa trên ba điểm
            val a = distanceOf(screenLeftPoint, beforeRotatePoint)
            val b = distanceOf(screenLeftPoint, screenRightPoint)
            val c = distanceOf(beforeRotatePoint, screenRightPoint)
            // Định luật cosin để tìm góc quay
            angle = acos(((a * a + b * b - c * c) / (2 * a * b)).toDouble())

            // Sửa góc quay; điểm ở bên phải của nhãn dán đối xứng với điểm ở bên trái và đối xứng qua trục x
            if (screenRightPoint.x < beforeRotatePoint.x && screenRightPoint.y < 2 * screenLeftPoint.y - beforeRotatePoint.y) {
                angle = -angle
            }
        } else {
            angle = (180.0 - mScreenAnchor!!.roll) / 180.0 * 3.14
        }
        // Xoay bốn đỉnh đến vị trí mục tiêu
        leftTop = getRotateVertices(leftTop, screenLeftPoint, angle)
        leftBottom = getRotateVertices(leftBottom, screenLeftPoint, angle)
        rightTop = getRotateVertices(rightTop, screenLeftPoint, angle)
        rightBottom = getRotateVertices(rightBottom, screenLeftPoint, angle)

        // Chuyển đổi sang giá trị tọa độ hệ thống tọa độ OpenGL
        leftTop = transVerticesToOpenGL(leftTop, width.toFloat(), height.toFloat())
        leftBottom = transVerticesToOpenGL(leftBottom, width.toFloat(), height.toFloat())
        rightTop = transVerticesToOpenGL(rightTop, width.toFloat(), height.toFloat())
        rightBottom = transVerticesToOpenGL(rightBottom, width.toFloat(), height.toFloat())

        // Nếu gặp phải vấn đề bị phản chiếu trước đó, chỉ cần thay đổi sự tương ứng tọa độ sau
        val vertices = FloatArray(8)
        vertices[0] = rightBottom.x
        vertices[1] = rightBottom.y
        vertices[2] = leftBottom.x
        vertices[3] = leftBottom.y
        vertices[4] = rightTop.x
        vertices[5] = rightTop.y
        vertices[6] = leftTop.x
        vertices[7] = leftTop.y

        mRenderVertices.clear()
        mRenderVertices.put(vertices)
    }

    // Attributes constants

    val A_PARTICLE_BIRTH_TIME: String = "a_BirthTime"

    val A_PARTICLE_DURATION: String = "a_Duration"

    val A_PARTICLE_FROM_SIZE: String = "a_FromSize"

    val A_PARTICLE_TO_SIZE: String = "a_ToSize"

    val A_PARTICLE_FROM_ANGLE: String = "a_FromRotation"

    val A_PARTICLE_TO_ANGLE: String = "a_ToRotation"

    val A_PARTICLE_POSITION: String = "a_BirthPosition"
    val A_PARTICLE_DIRECTION_VECTOR: String = "a_DirectionVector"

    val A_PARTICLE_FROM_COLOR: String = "a_FromColor"

    val A_PARTICLE_TO_COLOR: String = "a_ToColor"

    val A_PARTICLE_TEXTURE_INDEX: String = "a_TextureIndex"
    val ATTRIBUTES: Array<String> = arrayOf(
        A_PARTICLE_BIRTH_TIME,
        A_PARTICLE_DURATION,
        A_PARTICLE_FROM_SIZE,
        A_PARTICLE_TO_SIZE,
        A_PARTICLE_FROM_ANGLE,
        A_PARTICLE_TO_ANGLE,
        A_PARTICLE_POSITION,
        A_PARTICLE_DIRECTION_VECTOR,
        A_PARTICLE_FROM_COLOR,
        A_PARTICLE_TO_COLOR,
        A_PARTICLE_TEXTURE_INDEX
    )

    var typeRender = TYPE_RENDER_NORMAL

    companion object{
        val TYPE_RENDER_NORMAL = "NORMAL"
        val TYPE_RENDER_SCORE = "SCORE"
        val TYPE_RENDER_ROTATE = "ROTATE"
    }

    /**
     * 绘制组件
     *
     *
     * 在GL线程调用
     *
     * @param textureHandle      纹理指针
     * @param positionHandle     渲染顶点坐标指针
     * @param textureCoordHandle 纹理顶点坐标指针
     * @param textureVertices    纹理顶点坐标
     */
    fun onDraw(textureHandle: Int, positionHandle: Int, textureCoordHandle: Int, textureVertices: FloatBuffer,
               rotate : Int, quizIndex : Int = 0, isRecording : Boolean, isSuccess : Boolean, totalScore : Int) {
        mRenderVertices.position(0)
        textureVertices.position(0)
        if(isSuccess) return

        if (mStartTime == -1L) {
            mStartTime = System.currentTimeMillis()
        }
        val currentTime = System.currentTimeMillis()
        val position = (currentTime - mStartTime) % mComponent.duration
        // 如mComponent.duration=3000，mComponent.length=60，position=1000，则currentIndex=20
        val currentIndex = Math.round((mComponent.length - 1) * 1.0f / mComponent.duration * position)

//        val path = mComponent.resources!![currentIndex]
        if(mComponent.resources.isNullOrEmpty()) return
        val path = if(typeRender == TYPE_RENDER_NORMAL) {
            mComponent.resources!![quizIndex]
        }else if(typeRender == TYPE_RENDER_SCORE) {
            getImageFileNameFromScore(mComponent.resources!!, totalScore, mComponent.fileName)
        }else {
            getImageFileNameFromDegree(mComponent.resources!!, rotate, isRecording, quizIndex, mComponent.fileName)
        }

        var bitmap = mBitmapCache?.get(path)

        if (bitmap == null || bitmap.isRecycled) {
            bitmap = BitmapUtil.loadBitmap(mContext, path, mComponent.width, mComponent.height)
            if (bitmap != null && !bitmap.isRecycled) {
//                // 按照mComponent.width和mComponent.height尺寸对图片进行缩放
//                if (bitmap.getWidth() != mComponent.width || bitmap.getHeight() != mComponent.height) {
//                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mComponent.width, mComponent.height, true);
//                    if (scaledBitmap != bitmap) {
//                        bitmap.recycle();
//                    }
//                    bitmap = scaledBitmap;
//                }
                mBitmapCache!!.put(path, bitmap)
            } else {
                return
            }
        }

        // Khi khung hiện tại không thay đổi, không liên kết lại Bitmap và trực tiếp hiển thị kết cấu đã liên kết
        if (mLastIndex != currentIndex) {
            if (mTexture != 0) {
                val tex = IntArray(1)
                tex[0] = mTexture
                GLES20.glDeleteTextures(1, tex, 0)
                mTexture = 0
            }
            mTexture = BitmapUtil.bindBitmap(bitmap)
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture)
        GLES20.glUniform1i(textureHandle, 2)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, mRenderVertices)
        GLES20.glVertexAttribPointer(
            textureCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureVertices
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        mLastIndex = currentIndex
    }

    @SuppressLint("DefaultLocale")
    private fun getImageFileNameFromScore(resource : List<String>, score: Int, nameFile : String): String {
        // Format the degree to a three-digit number with leading zeros
        try {
            val formattedScore = String.format("%01d", score)//khi chưa record, sẽ k hiện correct hay wrong
            return resource.first { it.contains("${nameFile}_${formattedScore}.") }
        }catch (e : Exception){
            return ""
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getImageFileNameFromDegree(resource : List<String>, degree: Int, isRecording: Boolean, quizIndex: Int, nameFile : String): String {
        // Format the degree to a three-digit number with leading zeros
        try {
            val formattedDegree = String.format("%02d", if(!isRecording && abs(degree) == 24) abs(degree) - 1 else abs(degree))//khi chưa record, sẽ k hiện correct hay wrong
            return resource.first { it.contains("${nameFile}_${quizIndex}_${formattedDegree}.") }
        }catch (e : Exception){
            return ""
        }
    }

    /**
     * 销毁组件资源
     *
     *
     * 在GL线程调用
     */
    fun destroy() {
        if (mTexture != 0) {
            val tex = IntArray(1)
            tex[0] = mTexture
            GLES20.glDeleteTextures(1, tex, 0)
            mTexture = 0
        }
    }

    private fun getRotateVertices(point: PointF, anchorPoint: PointF, angle: Double): PointF {
        return PointF(
            ((point.x - anchorPoint.x) * cos(angle) -
                    (point.y - anchorPoint.y) * sin(angle) + anchorPoint.x).toFloat(),
            ((point.x - anchorPoint.x) * sin(angle) + (point.y - anchorPoint.y) * cos(angle) + anchorPoint.y).toFloat()
        )
    }

    private fun transVerticesToOpenGL(point: PointF, width: Float, height: Float): PointF {
        return PointF((point.x - width / 2) / (width / 2),
            (point.y - height / 2) / (height / 2)
        )
    }

    private fun distanceOf(p0: PointF, p1: PointF): Float {
        return sqrt(((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y)))
    }
}
