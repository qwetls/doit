package com.xeylabs.doit.ui

import com.xeylabs.doit.data.Currency
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val indonesia = Locale("in", "ID")
private val us = Locale.US

fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(indonesia)
    return "Rp" + nf.format(amount)
}

/** Format jumlah sesuai mata uang. USD memakai satuan dolar utuh (tanpa sen). */
fun formatMoney(amount: Long, currency: Currency): String {
    return when (currency) {
        Currency.IDR -> formatRupiah(amount)
        Currency.USD -> "$" + NumberFormat.getNumberInstance(us).format(amount)
    }
}

fun formatSignedRupiah(amount: Long, isIncome: Boolean): String {
    return (if (isIncome) "+" else "-") + formatRupiah(amount)
}

fun formatSignedMoney(amount: Long, isIncome: Boolean, currency: Currency): String {
    return (if (isIncome) "+" else "-") + formatMoney(amount, currency)
}

/** Angka ringkas untuk badge grafik: Rp 3.5jt, Rp 420rb, Rp 58. */
fun formatCompact(amount: Long): String {
    val abs = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        abs >= 1_000_000L -> {
            val v = abs / 1_000_000.0
            val text = if (v == kotlin.math.floor(v)) v.toInt().toString() else "%.1f".format(v)
            sign + "Rp " + text + "jt"
        }
        abs >= 1_000L -> sign + "Rp " + (abs / 1_000L) + "rb"
        else -> sign + "Rp " + abs
    }
}

fun formatDate(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", indonesia))
}

fun formatMonth(month: YearMonth): String {
    return month.format(DateTimeFormatter.ofPattern("MMMM yyyy", indonesia))
}
