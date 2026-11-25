package com.xeylabs.doit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.OutflowStrong
import com.xeylabs.doit.ui.theme.SurfaceHighest
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.ui.theme.categoryColor
import com.xeylabs.doit.viewmodel.InsightKind
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.ReportRange

@Composable
fun ReportsScreen(viewModel: MainViewModel, onNavigate: (DoitTab) -> Unit) {
    val month by viewModel.selectedMonth.collectAsState()
    val range by viewModel.reportRange.collectAsState()
    val report by viewModel.reportState.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val currency = Currency.fromCode(report.defaultCurrency)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FluxHeader(subtitle = "Analisis", displayName = displayName) {
            onNavigate(DoitTab.AKUN)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReportRange.entries) { option ->
                FilterChip(
                    selected = range == option,
                    onClick = { viewModel.setReportRange(option) },
                    label = { Text(option.label) }
                )
            }
        }
        if (range == ReportRange.MONTH) {
            MonthStepper(
                month = month,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ARUS KAS BERSIH",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (report.monthIncome > 0) {
                    val surplusPct = (report.monthNet * 100 / report.monthIncome).toInt()
                    StatusPill(
                        text = if (report.monthNet >= 0) "Surplus $surplusPct%" else "Defisit ${-surplusPct}%",
                        color = if (report.monthNet >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (if (report.monthNet >= 0) "+" else "-") +
                    formatMoney(kotlin.math.abs(report.monthNet), currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (report.monthNet >= 0) IncomeGreen else ExpenseRed
            )
            Text(
                text = if (report.monthNet >= 0) "Surplus stabil dari total pemasukan periode ini"
                else "Pengeluaran melebihi pemasukan periode ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pemasukan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatMoney(report.monthIncome, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pengeluaran", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatMoney(report.monthExpense, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val totalFlow = report.monthIncome + report.monthExpense
            if (totalFlow > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                        .clip(RoundedCornerShape(999.dp)).background(SurfaceHighest)
                ) {
                    Box(
                        modifier = Modifier.weight((report.monthIncome.toFloat() / totalFlow.toFloat()).coerceAtLeast(0.001f))
                            .fillMaxHeight().background(Emerald)
                    )
                    Box(
                        modifier = Modifier.weight((report.monthExpense.toFloat() / totalFlow.toFloat()).coerceAtLeast(0.001f))
                            .fillMaxHeight().background(OutflowStrong)
                    )
                }
            }
            val otherCodes = (report.monthIncomeByCurrency.keys + report.monthExpenseByCurrency.keys)
                .filter { it != report.defaultCurrency }
            otherCodes.forEach { code ->
                val otherCurrency = Currency.fromCode(code)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${otherCurrency.label}: masuk " + formatMoney(report.monthIncomeByCurrency[code] ?: 0L, otherCurrency) +
                        " · keluar " + formatMoney(report.monthExpenseByCurrency[code] ?: 0L, otherCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DoitCard {
            SectionTitle("Distribusi Pengeluaran")
            Text(
                "Komposisi alokasi dana aktual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perCategory.isEmpty()) {
                Text(
                    "Belum ada pengeluaran di periode ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DonutChart(
                        slices = report.perCategory.toDonutSlices(),
                        centerText = formatCompact(report.monthExpense)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(report.perCategory) { item ->
                        val pct = if (report.monthExpense > 0) (item.amount * 100 / report.monthExpense).toInt() else 0
                        StatusPill(
                            text = item.category.label + " ($pct%)",
                            color = categoryColor(item.category.name),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        if (range == ReportRange.MONTH && state.insights.isNotEmpty()) {
            DoitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Emerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Insight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald
                        )
                        Text(
                            "Dari pola catatanmu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.insights.forEach { insight ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (icon, tint) = when (insight.kind) {
                                InsightKind.GOOD -> Icons.Filled.CheckCircle to IncomeGreen
                                InsightKind.WARN -> Icons.Filled.Warning to WarningAmber
                                InsightKind.INFO -> Icons.Filled.TrendingUp to Emerald
                            }
                            androidx.compose.material3.Icon(
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
            SectionTitle("Kategori Pengeluaran Terbesar")
            Text(
                "Diurutkan berdasarkan nominal tertinggi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perCategory.isEmpty()) {
                Text(
                    "Belum ada pengeluaran di periode ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    report.perCategory.forEachIndexed { index, item ->
                        val pct = if (report.monthExpense > 0) (item.amount * 100 / report.monthExpense).toInt() else 0
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconTile(category = item.category)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "${index + 1}. ${item.category.label}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${item.count} transaksi · $pct% dari total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    formatMoney(item.amount, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = categoryColor(item.category.name)
                            )
                        }
                    }
                }
            }
        }

        DoitCard {
            SectionTitle("Pengeluaran 6 bulan terakhir")
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(bars = state.monthlyBars.toMonthBars())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total 6 bulan: " + formatMoney(state.monthlyBars.sumOf { it.amount }, currency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DoitCard {
            SectionTitle("Per sumber pemasukan")
            Spacer(modifier = Modifier.height(12.dp))
            if (report.perSource.isEmpty()) {
                Text(
                    "Belum ada pemasukan di periode ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                report.perSource.forEach { source ->
                    ReportRow(source.category.label, source.amount, IncomeGreen, currency)
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
                "Unduh semua transaksi bulan terpilih sebagai file CSV untuk diolah di spreadsheet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.exportCsv() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ekspor CSV (bulan terpilih)")
            }
        }
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun ReportRow(
    label: String,
    amount: Long,
    color: Color,
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
