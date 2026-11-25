package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetPositionDao {
    @Query("SELECT * FROM budget_positions ORDER BY id ASC")
    fun getAll(): Flow<List<BudgetPosition>>

    @Query("SELECT * FROM budget_positions ORDER BY id ASC")
    suspend fun getAllOnce(): List<BudgetPosition>

    @Insert
    suspend fun insert(position: BudgetPosition)

    @Update
    suspend fun update(position: BudgetPosition)

    @Delete
    suspend fun delete(position: BudgetPosition)

    @Query("DELETE FROM budget_positions")
    suspend fun deleteAll()

    @Query("UPDATE budget_positions SET category = :newKey WHERE category = :oldKey")
    suspend fun updateCategory(oldKey: String, newKey: String)
}
