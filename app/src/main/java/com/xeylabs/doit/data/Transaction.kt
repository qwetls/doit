package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxnType { INCOME, EXPENSE }

enum class Category(val type: TxnType, val label: String) {
    GAJI(TxnType.INCOME, "Gaji"),
    LAIN_MASUK(TxnType.INCOME, "Lain-lain"),
    BELANJA(TxnType.EXPENSE, "Belanja harian"),
    MAKAN(TxnType.EXPENSE, "Makan"),
    TRANSPORT(TxnType.EXPENSE, "Transport"),
    DARURAT(TxnType.EXPENSE, "Darurat"),
    LAIN_KELUAR(TxnType.EXPENSE, "Lain-lain");

    companion object {
        fun forType(type: TxnType): List<Category> = entries.filter { it.type == type }
        fun fromName(name: String): Category =
            entries.firstOrNull { it.name == name } ?: LAIN_KELUAR
    }
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val category: String,
    val amount: Long,
    val dateEpochDay: Long,
    val note: String = ""
)
