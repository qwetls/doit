package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun getAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(tx: Transaction)

    @Update
    suspend fun update(tx: Transaction)

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    suspend fun getAllOnce(): List<Transaction>

    @Delete
    suspend fun delete(tx: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("UPDATE transactions SET category = :newKey WHERE category = :oldKey")
    suspend fun updateCategory(oldKey: String, newKey: String)

    @Query("UPDATE transactions SET walletId = :newId WHERE walletId = :oldId")
    suspend fun updateWalletId(oldId: Long, newId: Long)

    @Query("UPDATE transactions SET transferToWalletId = :newId WHERE transferToWalletId = :oldId")
    suspend fun updateTransferToWalletId(oldId: Long, newId: Long)
}
