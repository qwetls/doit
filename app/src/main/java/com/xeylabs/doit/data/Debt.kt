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

enum class DebtKind(val label: String) {
    UTANG("Utang"),
    PIUTANG("Piutang"),
    PAYLATER("Paylater");

    companion object {
        fun fromName(name: String?): DebtKind =
            entries.firstOrNull { it.name == name } ?: UTANG
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
    val dateEpochDay: Long,
    // Jenis utang: "UTANG" / "PIUTANG" / "PAYLATER". Null = catatan lama,
    // diturunkan dari direction (LENT = PIUTANG, lainnya = UTANG).
    val kind: String? = null,
    // Nama penyedia, hanya untuk PAYLATER (mis. "GoPay"). Null/"" = tidak ada.
    val provider: String? = null
)

/** Jenis efektif sebuah utang; catatan lama tanpa kind diturunkan dari direction. */
fun Debt.effectiveKind(): DebtKind =
    kind?.let { DebtKind.fromName(it) }
        ?: if (direction == DebtDirection.LENT.name) DebtKind.PIUTANG else DebtKind.UTANG
