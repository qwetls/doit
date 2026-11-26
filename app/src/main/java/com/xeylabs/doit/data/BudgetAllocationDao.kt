package com.xeylabs.doit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetAllocationDao {
    @Query("SELECT * FROM budget_allocations ORDER BY id ASC")
    fun getAll(): Flow<List<BudgetAllocation>>

    @Query("SELECT * FROM budget_allocations ORDER BY id ASC")
    suspend fun getAllOnce(): List<BudgetAllocation>

    @Insert
    suspend fun insert(allocation: BudgetAllocation)

    @Insert
    suspend fun insertAll(allocations: List<BudgetAllocation>)

    @Query("DELETE FROM budget_allocations WHERE potKey = :potKey")
    suspend fun deleteByPotKey(potKey: String)

    @Query("DELETE FROM budget_allocations")
    suspend fun deleteAll()
}
