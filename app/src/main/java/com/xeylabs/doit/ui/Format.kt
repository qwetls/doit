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

fun formatDate(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", indonesia))
}

fun formatMonth(month: YearMonth): String {
    return month.format(DateTimeFormatter.ofPattern("MMMM yyyy", indonesia))
}
