package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurringFrequency(val label: String) {
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan");

    companion object {
        fun fromName(name: String): RecurringFrequency =
            entries.firstOrNull { it.name == name } ?: MONTHLY
    }
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val type: String,
    val category: String,
    val walletId: Long,
    val frequency: String,
    val dayOfMonth: Int = 1,
    val dayOfWeek: Int = 1,
    val isActive: Boolean = true,
    val createdEpochDay: Long,
    val lastRunEpochDay: Long? = null
)
