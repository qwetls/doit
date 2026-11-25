package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY type ASC, name ASC")
    fun getAll(): Flow<List<CustomCategory>>

    @Query("SELECT * FROM custom_categories ORDER BY type ASC, name ASC")
    suspend fun getAllOnce(): List<CustomCategory>

    @Insert
    suspend fun insert(category: CustomCategory): Long

    @Update
    suspend fun update(category: CustomCategory)

    @Delete
    suspend fun delete(category: CustomCategory)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAll()
}
