package com.xeylabs.doit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WalletType(val label: String) {
    TUNAI("Tunai"),
    BANK("Bank"),
    EWALLET("E-wallet");

    companion object {
        fun fromName(name: String): WalletType =
            entries.firstOrNull { it.name == name } ?: TUNAI
    }
}

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val initialBalance: Long = 0,
    // Kode mata uang: "IDR" atau "USD". Null = dompet lama (v1.4.0 ke bawah) = IDR.
    val currency: String? = null,
    @ColumnInfo(defaultValue = "0") val isDefault: Boolean = false
)
