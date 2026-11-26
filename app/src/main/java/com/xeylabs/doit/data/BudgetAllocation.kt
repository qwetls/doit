package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val POT_BELANJA = "BELANJA"
const val POT_DARURAT = "DARURAT"
const val POT_TABUNGAN = "TABUNGAN"

fun customPotKey(positionId: Long): String = "POS_$positionId"

@Entity(tableName = "budget_allocations")
data class BudgetAllocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val potKey: String,
    val amount: Long,
    val source: String,
    val monthKey: String,
    val dateEpochDay: Long,
    val note: String = ""
)
