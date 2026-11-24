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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.VioletAccent
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Patokan dari gaji Rp1.000.000: 200rb belanja, 100rb darurat, 700rb tabungan. Ubah sesuai kebutuhanmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DoitCard {
            SectionTitle("Pemakaian bulan ini")
            Spacer(modifier = Modifier.height(12.dp))
            UsageRow("Belanja harian", state.belanjaSpent, state.budgetBelanja)
            Spacer(modifier = Modifier.height(12.dp))
            UsageRow("Dana darurat", state.daruratSpent, state.budgetDarurat)
            Spacer(modifier = Modifier.height(12.dp))
            val savedThisMonth = (state.monthIncome - state.monthExpense).coerceAtLeast(0)
            UsageRow("Tabungan (terkumpul bulan ini)", savedThisMonth, state.budgetTabungan)
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
private fun UsageRow(label: String, spent: Long, budget: Long) {
    val ratio = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    val color: Color = when {
        ratio >= 1f -> ExpenseRed
        ratio >= 0.8f -> WarningAmber
        else -> VioletAccent
    }
    val pct = (ratio.coerceIn(0f, 1f) * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                formatRupiah(spent) + " / " + formatRupiah(budget) + " · $pct%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        LinearProgressIndicator(
            progress = ratio.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color
        )
        val remaining = budget - spent
        Text(
            text = when {
                remaining < 0 -> "Melewati anggaran " + formatRupiah(-remaining)
                ratio >= 0.8f -> "Hampir habis — sisa " + formatRupiah(remaining)
                else -> "Sisa " + formatRupiah(remaining)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
