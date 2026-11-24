package com.xeylabs.doit.ui

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val indonesia = Locale("in", "ID")

fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(indonesia)
    return "Rp" + nf.format(amount)
}

fun formatSignedRupiah(amount: Long, isIncome: Boolean): String {
    return (if (isIncome) "+" else "-") + formatRupiah(amount)
}

fun formatDate(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", indonesia))
}

fun formatMonth(month: YearMonth): String {
    return month.format(DateTimeFormatter.ofPattern("MMMM yyyy", indonesia))
}
