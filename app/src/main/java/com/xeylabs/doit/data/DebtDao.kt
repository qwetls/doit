package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, dateEpochDay DESC, id DESC")
    fun getAll(): Flow<List<Debt>>

    @Query("SELECT * FROM debts ORDER BY isSettled ASC, dateEpochDay DESC, id DESC")
    suspend fun getAllOnce(): List<Debt>

    @Insert
    suspend fun insert(debt: Debt)

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)
}
