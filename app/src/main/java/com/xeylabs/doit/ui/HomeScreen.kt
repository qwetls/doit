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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.YearMonth

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthStepper(
            month = month,
            onPrev = { viewModel.prevMonth() },
            onNext = { viewModel.nextMonth() }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn("Masuk", state.monthIncome, IncomeGreen)
                SummaryColumn("Keluar", state.monthExpense, ExpenseRed)
                SummaryColumn("Sisa", state.monthIncome - state.monthExpense, MaterialTheme.colorScheme.onSurface)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Anggaran bulan ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                BudgetRow("Belanja harian", state.belanjaSpent, state.budgetBelanja)
                BudgetRow("Dana darurat", state.daruratSpent, state.budgetDarurat)
                Text(
                    "Jatah tabungan bulan ini: " + formatRupiah(state.budgetTabungan),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tabungan laptop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val progress = if (state.savingsTarget > 0) {
                    (state.saved.toFloat() / state.savingsTarget.toFloat()).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = VioletAccent
                )
                Text(
                    formatRupiah(state.saved) + " dari " + formatRupiah(state.savingsTarget),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                val estimateText = when {
                    state.monthsToTarget == 0L -> "Target tercapai!"
                    state.monthsToTarget != null -> "Perkiraan tercapai dalam " + state.monthsToTarget + " bulan lagi"
                    else -> "Belum bisa diperkirakan — catat pemasukan dan pengeluaran dulu"
                }
                Text(
                    "Sisa " + formatRupiah((state.savingsTarget - state.saved).coerceAtLeast(0)) + " · " + estimateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text("Terakhir dicatat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (state.recent.isEmpty()) {
            Text(
                "Belum ada catatan. Ketuk + untuk mencatat pemasukan atau pengeluaran pertama.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    state.recent.forEach { tx -> TransactionRow(tx = tx) }
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatRupiah(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun BudgetRow(label: String, spent: Long, budget: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                formatRupiah(spent) + " / " + formatRupiah(budget),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        val progress = if (budget > 0) (spent.toFloat() / budget.toFloat()).coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (spent > budget) ExpenseRed else VioletAccent
        )
        val remaining = budget - spent
        Text(
            if (remaining >= 0) "Sisa " + formatRupiah(remaining) else "Lebih " + formatRupiah(-remaining) + " dari anggaran",
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed
        )
    }
}
