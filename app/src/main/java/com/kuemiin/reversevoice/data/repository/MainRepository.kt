package com.kuemiin.reversevoice.data.repository

import com.kuemiin.reversevoice.data.db.MyCreationDao
import com.kuemiin.reversevoice.model.MyGalleryModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val myCreation: MyCreationDao,
) {
    //region mydessin
    fun insertVideoMyCreation(morse: MyGalleryModel) = myCreation.insertVideoMyCreation(morse)

    fun getAllVideosMyCreation(): Flow<List<MyGalleryModel>> = myCreation.getAllVideosMyCreation()

    fun deleteCreationsByIds(ids : List<Int>) = myCreation.deleteCreationById(ids)

    fun updateListCreation(ids: List<MyGalleryModel>) = myCreation.updateListCreation(ids)
    //endregion

}