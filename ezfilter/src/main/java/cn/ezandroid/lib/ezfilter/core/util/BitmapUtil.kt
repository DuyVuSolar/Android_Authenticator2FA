package cn.ezandroid.lib.ezfilter.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.io.IOException
import java.io.InputStream


/**
 * 图片工具类
 *
 * @author like
 * @date 2017-08-10
 */
object BitmapUtil {
    /**
     * 加载图片
     *
     * @param context
     * @param path
     * @return
     */
    @JvmStatic
    fun loadBitmap(context: Context, path: String): Bitmap? {
        var `in`: InputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inScaled = false
            options.inDither = false
            options.inInputShareable = true
            options.inPurgeable = true
            if (Path.ASSETS.belongsTo(path)) {
                `in` = context.assets.open(Path.ASSETS.crop(path))
                if (`in` != null) {
                    return BitmapFactory.decodeStream(`in`, null, options)
                }
            } else if (Path.FILE.belongsTo(path)) {
                return BitmapFactory.decodeFile(Path.FILE.crop(path), options)
            } else if (Path.DRAWABLE.belongsTo(path)) {
                return BitmapFactory.decodeResource(
                    context.resources,
                    Path.DRAWABLE.crop(path).toInt(), options
                )
            } else {
                return BitmapFactory.decodeFile(path, options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * 有大小限制的加载图片
     *
     * @param context
     * @param path
     * @param width   最大宽度
     * @param height  最大高度
     * @return
     */
    fun loadBitmap(context: Context, path: String, width: Int, height: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            if (Path.ASSETS.belongsTo(path)) {
                inputStream = context.assets.open(Path.ASSETS.crop(path))
                BitmapFactory.decodeStream(inputStream, null, options)
            } else if (Path.FILE.belongsTo(path)) {
                BitmapFactory.decodeFile(Path.FILE.crop(path), options)
            } else if (Path.DRAWABLE.belongsTo(path)) {
                BitmapFactory.decodeResource(
                    context.resources,
                    Path.DRAWABLE.crop(path).toInt(), options
                )
            } else {
                BitmapFactory.decodeFile(path, options)
            }
            val outWidth = options.outWidth
            val outHeight = options.outHeight
            var sampleSize = 1
            while (outWidth / (sampleSize * 2) > width || outHeight / (sampleSize * 2) > height) {
                sampleSize *= 2
            }
            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            if (Path.ASSETS.belongsTo(path)) {
                inputStream = context.assets.open(Path.ASSETS.crop(path))
                return BitmapFactory.decodeStream(inputStream, null, options)
            } else if (Path.FILE.belongsTo(path)) {
                return BitmapFactory.decodeFile(Path.FILE.crop(path), options)
            } else if (Path.DRAWABLE.belongsTo(path)) {
                return BitmapFactory.decodeResource(
                    context.resources,
                    Path.DRAWABLE.crop(path).toInt(), options
                )
            } else {
                return BitmapFactory.decodeFile(path, options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }


    /**
     * 将bitmap绑定为纹理
     *
     * @param bitmap
     * @return
     */
    @JvmStatic
    fun bindBitmap(bitmap: Bitmap?): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        return tex[0]
    }
}
