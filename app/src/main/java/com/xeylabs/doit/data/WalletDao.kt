package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAll(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    suspend fun getAllOnce(): List<Wallet>

    @Insert
    suspend fun insert(wallet: Wallet): Long

    @Update
    suspend fun update(wallet: Wallet)

    @Delete
    suspend fun delete(wallet: Wallet)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()
}
