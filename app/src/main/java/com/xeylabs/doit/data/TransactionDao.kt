package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun getAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(tx: Transaction)

    @Delete
    suspend fun delete(tx: Transaction)
}
