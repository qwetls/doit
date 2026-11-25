package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_positions")
data class BudgetPosition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val limitAmount: Long
)
