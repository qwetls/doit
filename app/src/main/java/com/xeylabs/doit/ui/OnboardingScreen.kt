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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun OnboardingScreen(viewModel: MainViewModel) {
    var incomeText by remember { mutableStateOf("1000000") }
    var belanjaText by remember { mutableStateOf("200000") }
    var daruratText by remember { mutableStateOf("100000") }
    var tabunganText by remember { mutableStateOf("700000") }
    var targetText by remember { mutableStateOf("3000000") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Selamat datang di do.it",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Atur angka dasar dulu supaya do.it bisa menghitung anggaran dan tabunganmu. Semua bisa diubah nanti.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionTitle("Pemasukan bulanan")
        OutlinedTextField(
            value = incomeText,
            onValueChange = { incomeText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Gaji / pemasukan (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SectionTitle("Anggaran bulanan")
        OutlinedTextField(
            value = belanjaText,
            onValueChange = { belanjaText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Belanja harian (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = daruratText,
            onValueChange = { daruratText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Dana darurat (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = tabunganText,
            onValueChange = { tabunganText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Jatah tabungan (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SectionTitle("Target tabungan")
        OutlinedTextField(
            value = targetText,
            onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Target laptop (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                val income = incomeText.toLongOrNull()
                val belanja = belanjaText.toLongOrNull()
                val darurat = daruratText.toLongOrNull()
                val tabungan = tabunganText.toLongOrNull()
                val target = targetText.toLongOrNull()
                if (income == null || income <= 0 || belanja == null || belanja < 0 ||
                    darurat == null || darurat < 0 || tabungan == null || tabungan < 0 ||
                    target == null || target <= 0
                ) {
                    error = "Periksa lagi angkanya — pemasukan dan target harus lebih dari 0"
                    return@Button
                }
                viewModel.completeOnboarding(income, belanja, darurat, tabungan, target)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mulai pakai do.it")
        }
        TextButton(
            onClick = { viewModel.skipOnboarding() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lewati dulu — pakai angka standar")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
