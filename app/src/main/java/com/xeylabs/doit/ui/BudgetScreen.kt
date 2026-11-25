package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun BudgetSection(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var incomeText by remember(state.monthlyIncome) { mutableStateOf(state.monthlyIncome.toString()) }
    var belanjaText by remember(state.budgetBelanja) { mutableStateOf(state.budgetBelanja.toString()) }
    var daruratText by remember(state.budgetDarurat) { mutableStateOf(state.budgetDarurat.toString()) }
    var tabunganText by remember(state.budgetTabungan) { mutableStateOf(state.budgetTabungan.toString()) }
    var savedMessage by remember { mutableStateOf("") }

    val savedThisMonth = (state.monthIncome - state.monthExpense).coerceAtLeast(0)
    val totalBudget = state.budgetBelanja + state.budgetDarurat + state.budgetTabungan
    val totalUsed = state.belanjaSpent + state.daruratSpent + savedThisMonth
    val totalRemaining = (totalBudget - totalUsed).coerceAtLeast(0)
    val totalPct = if (totalBudget > 0) (totalUsed * 100 / totalBudget).toInt() else 0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Patokan dari gaji Rp1.000.000: 200rb belanja, 100rb darurat, 700rb tabungan. Ubah sesuai kebutuhanmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "SISA ANGGARAN BULANAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatRupiah(totalRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusPill(text = "$totalPct% Terpakai", color = Emerald)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (totalRemaining > 0) "Aman & Terkendali" else "Anggaran habis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (totalUsed.toFloat() / totalBudget.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Emerald
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Terpakai: " + formatRupiah(totalUsed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Total: " + formatRupiah(totalBudget),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DoitCard {
            SectionTitle("Pos Anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UsageRow(
                    label = "Belanja harian",
                    spent = state.belanjaSpent,
                    budget = state.budgetBelanja,
                    isSavings = false
                )
                UsageRow(
                    label = "Dana darurat",
                    spent = state.daruratSpent,
                    budget = state.budgetDarurat,
                    isSavings = false
                )
                UsageRow(
                    label = "Tabungan (terkumpul bulan ini)",
                    spent = savedThisMonth,
                    budget = state.budgetTabungan,
                    isSavings = true
                )
            }
        }

        DoitCard {
            SectionTitle("Ubah anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            BudgetField("Pemasukan bulanan (Rp)", incomeText) { incomeText = it }
            BudgetField("Belanja harian (Rp)", belanjaText) { belanjaText = it }
            BudgetField("Dana darurat (Rp)", daruratText) { daruratText = it }
            BudgetField("Jatah tabungan (Rp)", tabunganText) { tabunganText = it }
            Button(
                onClick = {
                    val income = incomeText.toLongOrNull()
                    val belanja = belanjaText.toLongOrNull()
                    val darurat = daruratText.toLongOrNull()
                    val tabungan = tabunganText.toLongOrNull()
                    if (income != null && income > 0 && belanja != null && belanja >= 0 &&
                        darurat != null && darurat >= 0 && tabungan != null && tabungan >= 0
                    ) {
                        viewModel.setMonthlyIncome(income)
                        viewModel.setBudgetBelanja(belanja)
                        viewModel.setBudgetDarurat(darurat)
                        viewModel.setBudgetTabungan(tabungan)
                        savedMessage = "Anggaran disimpan"
                    } else {
                        savedMessage = "Periksa lagi angkanya — pemasukan harus lebih dari 0"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan anggaran")
            }
            if (savedMessage.isNotBlank()) {
                Text(savedMessage, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun UsageRow(label: String, spent: Long, budget: Long, isSavings: Boolean) {
    val ratio = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    val pct = (ratio.coerceIn(0f, 1f) * 100).toInt()
    val status: Pair<String, Color> = when {
        ratio >= 1f && isSavings -> "Selesai" to Emerald
        ratio >= 1f -> "Melewati" to ExpenseRed
        ratio >= 0.8f -> "Hampir Habis" to WarningAmber
        isSavings -> "Menabung" to Emerald
        else -> "Stabil" to Emerald
    }
    val barColor: Color = when {
        ratio >= 1f && isSavings -> Emerald
        ratio >= 1f -> ExpenseRed
        ratio >= 0.8f -> WarningAmber
        else -> Emerald
    }
    val remaining = budget - spent
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconTile(
                    category = when (label) {
                        "Belanja harian" -> com.xeylabs.doit.data.Category.BELANJA
                        "Dana darurat" -> com.xeylabs.doit.data.Category.DARURAT
                        else -> com.xeylabs.doit.data.Category.LAIN_KELUAR
                    },
                    size = 40
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Sisa " + formatRupiah(remaining.coerceAtLeast(0)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatRupiah(spent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "dari " + formatRupiah(budget),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = barColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$pct% dialokasikan",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusPill(text = status.first, color = status.second)
        }
    }
}

@Composable
private fun BudgetField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
