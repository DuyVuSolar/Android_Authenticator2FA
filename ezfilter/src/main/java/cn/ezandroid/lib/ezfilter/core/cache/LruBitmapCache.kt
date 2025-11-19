package cn.ezandroid.lib.ezfilter.core.cache

import android.graphics.Bitmap
import android.util.LruCache

/**
 * LRU image cache
 *
 * @author like
 * @date 2017-08-03
 */
class LruBitmapCache(size: Int) : IBitmapCache {
    private val mLruCache: LruCache<String, Bitmap>

    init {
        mLruCache = object : LruCache<String, Bitmap>(size) {
            override fun sizeOf(key: String?, value: Bitmap): Int {
                return value.rowBytes * value.height
            }
        }
    }

    override fun get(key: String): Bitmap? {
        return mLruCache[key]
    }

    override fun put(key: String, bitmap: Bitmap?) {
        mLruCache.put(key, bitmap)
    }

    override fun clear() {
        mLruCache.evictAll()
    }

    companion object {
        val singleInstance: LruBitmapCache =
            LruBitmapCache((Runtime.getRuntime().maxMemory() / 6).toInt())
    }
}
