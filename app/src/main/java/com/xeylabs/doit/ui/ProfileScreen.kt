package com.xeylabs.doit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.BuildConfig
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.ui.theme.HeroViolet
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel

/**
 * Profil & Pengaturan — dibuka dari avatar paling kiri di Beranda.
 * Tanpa login, tanpa foto: avatar inisial saja. Isinya nama tampilan,
 * mata uang utama, dompet utama, pemasukan bulanan, jalan pintas ke
 * Rencana/Laporan, versi aplikasi + cek update (self-update yang sama).
 */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    updateViewModel: UpdateViewModel,
    onBack: () -> Unit,
    onNavigate: (DoitTab) -> Unit
) {
    val displayName by viewModel.displayName.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val lastWalletId by viewModel.lastWalletId.collectAsState()
    val monthlyIncome by viewModel.uiState.collectAsState()

    var nameText by remember(displayName) { mutableStateOf(displayName) }
    var incomeText by remember(monthlyIncome.monthlyIncome) {
        mutableStateOf(monthlyIncome.monthlyIncome.toString())
    }
    var savedNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                "Profil & Pengaturan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        DoitCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(HeroViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.trim().firstOrNull()?.uppercase() ?: "A",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "do.it v" + BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DoitCard {
            SectionTitle("Nama tampilan")
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Nama") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                viewModel.setDisplayName(nameText)
                savedNote = "Nama disimpan."
            }) {
                Text("Simpan nama")
            }
            if (savedNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    savedNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DoitCard {
            SectionTitle("Mata uang utama")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Semua ringkasan, grafik, dan anggaran memakai mata uang ini. Dompet Dollar tetap dicatat terpisah.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Currency.entries) { option ->
                    FilterChip(
                        selected = defaultCurrency == option.code,
                        onClick = { viewModel.setDefaultCurrency(option.code) },
                        label = { Text(option.label) }
                    )
                }
            }
        }

        if (wallets.isNotEmpty()) {
            DoitCard {
                SectionTitle("Dompet utama")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Dompet yang dipakai kalau kamu mencatat tanpa memilih dompet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets, key = { it.id }) { wallet ->
                        FilterChip(
                            selected = lastWalletId == wallet.id,
                            onClick = { viewModel.setDefaultWallet(wallet.id) },
                            label = { Text(wallet.name) }
                        )
                    }
                }
            }
        }

        DoitCard {
            SectionTitle("Pemasukan bulanan")
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Pemasukan bulanan (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                viewModel.setMonthlyIncome(incomeText.toLongOrNull() ?: 0L)
                savedNote = "Pemasukan bulanan disimpan."
            }) {
                Text("Simpan")
            }
        }

        DoitCard {
            SectionTitle("Jalan pintas")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Anggaran & target tabungan", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { onNavigate(DoitTab.RENCANA) }) {
                    Text("Rencana")
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Laporan & ekspor CSV", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { onNavigate(DoitTab.LAPORAN) }) {
                    Text("Laporan")
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }

        UpdateCard(updateViewModel)

        Spacer(modifier = Modifier.height(72.dp))
    }
}
