package com.kuemiin.reversevoice.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kuemiin.reversevoice.model.MyGalleryModel

@Database(
    entities = [MyGalleryModel::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myCreationDao(): MyCreationDao
}
