package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY id ASC")
    fun getAll(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions ORDER BY id ASC")
    suspend fun getAllOnce(): List<RecurringTransaction>

    @Insert
    suspend fun insert(recurring: RecurringTransaction)

    @Update
    suspend fun update(recurring: RecurringTransaction)

    @Delete
    suspend fun delete(recurring: RecurringTransaction)
}
