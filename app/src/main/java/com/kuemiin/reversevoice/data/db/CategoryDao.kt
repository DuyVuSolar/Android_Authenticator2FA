package com.kuemiin.reversevoice.data.db

//import androidx.room.*
//import kotlinx.coroutines.flow.Flow

//@Dao
//interface CategoryDao {
//
//    @Query("Select * from category ORDER BY id DESC")
//    fun getCategories(): Flow<List<CategoryFilter>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun addCategory(fileHistory : CategoryFilter)
//
//    @Query("Select * from category WHERE id = :id")
//    fun getCategoryByID(id: Int): CategoryFilter
//
//    @Query("DELETE FROM category WHERE id in (:ids)")
//    fun deleteCategoryById(ids: List<Int>)
//
//    @Update(entity = CategoryFilter::class, onConflict = OnConflictStrategy.REPLACE)
//    fun update(fileHistory: CategoryFilter)
//}