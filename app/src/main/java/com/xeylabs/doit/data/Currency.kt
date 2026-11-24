package com.xeylabs.doit.data

/**
 * Mata uang yang didukung do.it — KHUSUS Rp dan Dollar saja (keputusan Adrien,
 * 2026-11-25). Multi-currency penuh ditunda. Satu dompet = satu mata uang.
 * Nilai null di database = IDR (dompet lama sebelum v1.4.0).
 */
enum class Currency(val code: String, val symbol: String, val label: String) {
    IDR("IDR", "Rp", "Rupiah (Rp)"),
    USD("USD", "$", "Dollar ($)");

    companion object {
        fun fromCode(code: String?): Currency =
            entries.firstOrNull { it.code == code } ?: IDR
    }
}

/** Mata uang efektif sebuah dompet; null = dompet lama = IDR. */
fun walletCurrency(wallet: Wallet?): Currency = Currency.fromCode(wallet?.currency)
