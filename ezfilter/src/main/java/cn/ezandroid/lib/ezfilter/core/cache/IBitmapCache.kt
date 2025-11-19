package cn.ezandroid.lib.ezfilter.core.cache

import android.graphics.Bitmap

/**
 * 图片缓存接口
 *
 *
 * 用来缓存滤镜用到的纹理图片，加快切换滤镜时的渲染速度
 *
 * @author like
 * @date 2017-09-18
 */
interface IBitmapCache {
    fun get(key: String): Bitmap?

    fun put(key: String, bitmap: Bitmap?)

    fun clear()
}
