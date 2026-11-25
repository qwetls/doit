package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDayOfMonth ASC, id ASC")
    fun getAll(): Flow<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY dueDayOfMonth ASC, id ASC")
    suspend fun getAllOnce(): List<Bill>

    @Insert
    suspend fun insert(bill: Bill)

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("DELETE FROM bills")
    suspend fun deleteAll()
}
