package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun SavingsScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var targetText by remember(state.savingsTarget) { mutableStateOf(state.savingsTarget.toString()) }
    var savedMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tabungan laptop", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val progress = if (state.savingsTarget > 0) {
                    (state.saved.toFloat() / state.savingsTarget.toFloat()).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = VioletAccent
                )
                Text(
                    formatRupiah(state.saved),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "dari target " + formatRupiah(state.savingsTarget),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Sisa: " + formatRupiah((state.savingsTarget - state.saved).coerceAtLeast(0)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Rata-rata nabung bersih: " + formatRupiah(state.avgMonthlyNet) + " / bulan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val estimateText = when {
                    state.monthsToTarget == 0L -> "Target tercapai. Saatnya beli laptop!"
                    state.monthsToTarget != null -> "Perkiraan tercapai dalam " + state.monthsToTarget + " bulan lagi"
                    else -> "Belum bisa diperkirakan — catat pemasukan dan pengeluaran dulu"
                }
                Text(estimateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "Tabungan dihitung dari total pemasukan dikurangi total pengeluaran selama ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ubah target tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val value = targetText.toLongOrNull()
                        if (value != null && value > 0) {
                            viewModel.setSavingsTarget(value)
                            savedMessage = "Target disimpan: " + formatRupiah(value)
                        } else {
                            savedMessage = "Masukkan jumlah yang benar"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan target")
                }
                if (savedMessage.isNotBlank()) {
                    Text(savedMessage, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}
