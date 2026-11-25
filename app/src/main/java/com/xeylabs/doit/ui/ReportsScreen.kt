package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.viewmodel.InsightKind
import com.xeylabs.doit.viewmodel.MainViewModel

/**
 * Laporan — satu-satunya tempat ringkasan arus kas, per kategori, per dompet,
 * per sumber pemasukan, grafik, dan ekspor CSV. Tidak ada pengelolaan data di sini.
 */
@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val month by viewModel.selectedMonth.collectAsState()
    val report by viewModel.reportState.collectAsState()
    val state by viewModel.uiState.collectAsState()
    var chartRange by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Laporan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        MonthStepper(
            month = month,
            onPrev = { viewModel.prevMonth() },
            onNext = { viewModel.nextMonth() }
        )

        DoitCard {
            SectionTitle("Arus kas " + formatMonth(month))
            Spacer(modifier = Modifier.height(12.dp))
            ReportRow("Pemasukan", report.monthIncome, IncomeGreen, Currency.fromCode(report.defaultCurrency))
            ReportRow("Pengeluaran", report.monthExpense, ExpenseRed, Currency.fromCode(report.defaultCurrency))
            ReportRow(
                "Bersih",
                report.monthNet,
                if (report.monthNet >= 0) IncomeGreen else ExpenseRed,
                Currency.fromCode(report.defaultCurrency)
            )
            val otherCodes = (report.monthIncomeByCurrency.keys + report.monthExpenseByCurrency.keys)
                .filter { it != report.defaultCurrency }
            otherCodes.forEach { code ->
                val currency = Currency.fromCode(code)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${currency.label}: masuk " + formatMoney(report.monthIncomeByCurrency[code] ?: 0L, currency) +
                        " · keluar " + formatMoney(report.monthExpenseByCurrency[code] ?: 0L, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.insights.isNotEmpty()) {
            DoitCard {
                SectionTitle("Insight")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.insights.forEach { insight ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (icon, tint) = when (insight.kind) {
                                InsightKind.GOOD -> Icons.Filled.CheckCircle to IncomeGreen
                                InsightKind.WARN -> Icons.Filled.Warning to WarningAmber
                                InsightKind.INFO -> Icons.Filled.TrendingUp to VioletAccent
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = insight.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(if (chartRange == 0) "Pengeluaran 7 hari terakhir" else "Pengeluaran 6 bulan terakhir")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chartRange == 0,
                        onClick = { chartRange = 0 },
                        label = { Text("7 hari") }
                    )
                    FilterChip(
                        selected = chartRange == 1,
                        onClick = { chartRange = 1 },
                        label = { Text("6 bulan") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (chartRange == 0) {
                BarChart(bars = state.last7Days.toDayBars())
            } else {
                BarChart(bars = state.monthlyBars.toMonthBars())
            }
            Spacer(modifier = Modifier.height(8.dp))
            val chartTotal = if (chartRange == 0) {
                state.last7Days.sumOf { it.amount }
            } else {
                state.monthlyBars.sumOf { it.amount }
            }
            Text(
                text = if (chartRange == 0) {
                    "Total 7 hari: " + formatMoney(chartTotal, Currency.fromCode(report.defaultCurrency))
                } else {
                    "Total 6 bulan: " + formatMoney(chartTotal, Currency.fromCode(report.defaultCurrency))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DoitCard {
            SectionTitle("Per kategori")
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perCategory.isEmpty()) {
                Text(
                    "Belum ada pengeluaran di bulan ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                DonutChart(
                    slices = report.perCategory.toDonutSlices(),
                    centerText = formatMoney(report.monthExpense, Currency.fromCode(report.defaultCurrency))
                )
                Spacer(modifier = Modifier.height(12.dp))
                report.perCategory.forEach { item ->
                    ReportRow(item.category.label, item.amount, MaterialTheme.colorScheme.onSurface, Currency.fromCode(report.defaultCurrency))
                }
            }
        }

        DoitCard {
            SectionTitle("Per sumber pemasukan")
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perSource.isEmpty()) {
                Text(
                    "Belum ada pemasukan di bulan ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                report.perSource.forEach { source ->
                    ReportRow(source.category.label, source.amount, IncomeGreen, Currency.fromCode(report.defaultCurrency))
                }
            }
        }

        DoitCard {
            SectionTitle("Per dompet")
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perWallet.isEmpty()) {
                Text(
                    "Belum ada dompet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                report.perWallet.forEach { walletMonth ->
                    ReportRow(
                        walletMonth.wallet.name + " (masuk − keluar)",
                        walletMonth.income - walletMonth.expense,
                        MaterialTheme.colorScheme.onSurface,
                        Currency.fromCode(walletMonth.wallet.currency)
                    )
                }
            }
        }

        DoitCard {
            SectionTitle("Ekspor")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Unduh semua transaksi bulan ini sebagai file CSV untuk diolah di spreadsheet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.exportCsv() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ekspor CSV bulan ini")
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun ReportRow(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    currency: Currency = Currency.IDR
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            formatMoney(amount, currency),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
