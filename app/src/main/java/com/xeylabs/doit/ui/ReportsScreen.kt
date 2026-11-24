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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen
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

        // ---------- Arus kas ----------
        DoitCard {
            SectionTitle("Arus kas " + formatMonth(month))
            Spacer(modifier = Modifier.height(12.dp))
            ReportRow("Pemasukan", report.monthIncome, IncomeGreen)
            ReportRow("Pengeluaran", report.monthExpense, ExpenseRed)
            ReportRow(
                "Bersih",
                report.monthNet,
                if (report.monthNet >= 0) IncomeGreen else ExpenseRed
            )
        }

        // ---------- Grafik pengeluaran 6 bulan ----------
        DoitCard {
            SectionTitle("Pengeluaran 6 bulan terakhir")
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(bars = state.monthlyBars.toMonthBars())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total 6 bulan: " + formatRupiah(state.monthlyBars.sumOf { it.amount }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- Per kategori ----------
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
                    centerText = formatRupiah(report.monthExpense)
                )
                Spacer(modifier = Modifier.height(12.dp))
                report.perCategory.forEach { item ->
                    ReportRow(item.category.label, item.amount, MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // ---------- Per sumber pemasukan ----------
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
                    ReportRow(source.category.label, source.amount, IncomeGreen)
                }
            }
        }

        // ---------- Per dompet ----------
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
                        MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ---------- Ekspor ----------
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
private fun ReportRow(label: String, amount: Long, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            formatRupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
