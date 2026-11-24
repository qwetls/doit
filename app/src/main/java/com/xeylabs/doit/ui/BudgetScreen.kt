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
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun BudgetScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var belanjaText by remember(state.budgetBelanja) { mutableStateOf(state.budgetBelanja.toString()) }
    var daruratText by remember(state.budgetDarurat) { mutableStateOf(state.budgetDarurat.toString()) }
    var tabunganText by remember(state.budgetTabungan) { mutableStateOf(state.budgetTabungan.toString()) }
    var savedMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Anggaran bulanan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Patokan dari gaji Rp1.000.000: 200rb belanja, 100rb darurat, 700rb tabungan. Ubah sesuai kebutuhanmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BudgetField("Belanja harian (Rp)", belanjaText) { belanjaText = it }
                BudgetField("Dana darurat (Rp)", daruratText) { daruratText = it }
                BudgetField("Tabungan laptop (Rp)", tabunganText) { tabunganText = it }
                Button(
                    onClick = {
                        val belanja = belanjaText.toLongOrNull()
                        val darurat = daruratText.toLongOrNull()
                        val tabungan = tabunganText.toLongOrNull()
                        if (belanja != null && belanja >= 0 && darurat != null && darurat >= 0 && tabungan != null && tabungan >= 0) {
                            viewModel.setBudgetBelanja(belanja)
                            viewModel.setBudgetDarurat(darurat)
                            viewModel.setBudgetTabungan(tabungan)
                            savedMessage = "Anggaran disimpan"
                        } else {
                            savedMessage = "Periksa lagi angkanya — harus bilangan bulat 0 atau lebih"
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
        Spacer(modifier = Modifier.height(72.dp))
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
