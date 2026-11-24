package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isDefault DESC, id ASC")
    fun getAll(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals ORDER BY isDefault DESC, id ASC")
    suspend fun getAllOnce(): List<SavingsGoal>

    @Insert
    suspend fun insert(goal: SavingsGoal)

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)
}
