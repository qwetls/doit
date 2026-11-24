package com.xeylabs.doit.data

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
    val initialBalance: Long = 0
)
