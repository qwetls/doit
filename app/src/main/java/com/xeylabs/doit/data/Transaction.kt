package com.xeylabs.doit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxnType { INCOME, EXPENSE, TRANSFER }

enum class Category(val type: TxnType, val label: String) {
    GAJI(TxnType.INCOME, "Gaji pokok"),
    LEMBUR(TxnType.INCOME, "Lembur"),
    SAMPINGAN(TxnType.INCOME, "Sampingan"),
    LAIN_MASUK(TxnType.INCOME, "Lain-lain"),
    BELANJA(TxnType.EXPENSE, "Belanja harian"),
    MAKAN(TxnType.EXPENSE, "Makan"),
    TRANSPORT(TxnType.EXPENSE, "Transport"),
    DARURAT(TxnType.EXPENSE, "Darurat"),
    LAIN_KELUAR(TxnType.EXPENSE, "Lain-lain"),
    TRANSFER(TxnType.TRANSFER, "Transfer");

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
    val note: String = "",
    // Null = dompet default (dompet pertama). Nullable supaya migrasi
    // ALTER TABLE tanpa DEFAULT tetap lolos validasi skema Room.
    val walletId: Long? = null,
    // Terisi hanya untuk TRANSFER: dompet tujuan.
    val transferToWalletId: Long? = null,
    // Terisi hanya untuk TRANSFER beda mata uang: jumlah yang diterima dompet
    // tujuan (diisi manual pengguna, tanpa kurs otomatis). Null = sama dengan amount.
    val transferReceivedAmount: Long? = null,
    val receiptPath: String? = null
)
