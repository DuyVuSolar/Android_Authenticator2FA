package com.beemdevelopment.aegis.data.db

import androidx.room.*
import com.beemdevelopment.aegis.model.MyGalleryModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MyCreationDao {
    @Query("Select * from my_design group by id order by date DESC")
    fun getAllVideosMyCreation(): Flow<List<MyGalleryModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideoMyCreation(morse : MyGalleryModel)

    @Query("DELETE FROM my_design WHERE id in (:ids)")
    fun deleteCreationById(ids: List<Int>)

    @Update(entity = MyGalleryModel::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateListCreation(detail: List<MyGalleryModel>)

}