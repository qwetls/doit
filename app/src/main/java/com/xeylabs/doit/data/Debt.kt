package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtDirection(val label: String) {
    BORROWED("Kamu meminjam"),
    LENT("Kamu meminjamkan");

    companion object {
        fun fromName(name: String): DebtDirection =
            entries.firstOrNull { it.name == name } ?: BORROWED
    }
}

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Long,
    val direction: String,
    val paid: Long = 0,
    val isSettled: Boolean = false,
    val dateEpochDay: Long
)
