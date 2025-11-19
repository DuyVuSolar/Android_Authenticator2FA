package com.kuemiin.base

import android.app.Application
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

abstract class BaseApplication : Application() {
    companion object {
        @JvmStatic
        protected lateinit var simpleCache: SimpleCache
        protected lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        protected lateinit var standaloneDatabaseProvider: StandaloneDatabaseProvider
    }

    override fun onCreate() {
        super.onCreate()
        val exoCacheSize: Long = 300 * 1024 * 1024 // Setting cache size to be ~ 100 MB
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoCacheSize)
        standaloneDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(
            File(this.cacheDir, "media"),
            leastRecentlyUsedCacheEvictor,
            standaloneDatabaseProvider
        )
    }
}