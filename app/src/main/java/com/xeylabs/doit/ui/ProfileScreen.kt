package com.xeylabs.doit.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.BuildConfig
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.HeroEmerald
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
    onNavigate: (DoitTab) -> Unit
) {
    val displayName by viewModel.displayName.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val lastWalletId by viewModel.lastWalletId.collectAsState()
    val monthlyIncome by viewModel.uiState.collectAsState()
    val transactionCount by viewModel.transactionCount.collectAsState()
    val recurrings by viewModel.recurrings.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val budgetPositions by viewModel.budgetPositions.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val pendingRestore by viewModel.pendingRestore.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val context = LocalContext.current

    var nameText by remember(displayName) { mutableStateOf(displayName) }
    var incomeText by remember(monthlyIncome.monthlyIncome) {
        mutableStateOf(monthlyIncome.monthlyIncome.toString())
    }
    var savedNote by remember { mutableStateOf("") }
    var showResetStep1 by remember { mutableStateOf(false) }
    var showResetStep2 by remember { mutableStateOf(false) }
    var resetDoneNote by remember { mutableStateOf("") }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val text = try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
            viewModel.prepareRestore(text)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FluxHeader(subtitle = "Akun", displayName = displayName) {
        }

        DoitCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(HeroEmerald),
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
                TextButton(onClick = { onNavigate(DoitTab.ANGGARAN) }) {
                    Text("Anggaran")
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Analisis & ekspor CSV", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { onNavigate(DoitTab.ANALISIS) }) {
                    Text("Analisis")
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }

        UpdateCard(updateViewModel)

        CategoryManagerSection(viewModel)

        DoitCard {
            SectionTitle("Cadangan & pulihkan")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Simpan semua data (transaksi, dompet, anggaran, target, utang, tagihan, kategori) ke satu file JSON, lalu pulihkan kapan saja — misalnya sebelum reset atau pindah HP.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.exportBackup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buat cadangan (JSON)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { pickBackup.launch("application/json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pulihkan dari cadangan")
            }
            val message = backupMessage
            if (message != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { viewModel.clearBackupMessage() }) {
                    Text("Tutup")
                }
            }
        }

        DoitCard(
            modifier = Modifier.border(1.dp, ExpenseRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SectionTitle("Zona berbahaya")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Hapus SEMUA data di aplikasi ini dan kembali seperti baru: transaksi, dompet, anggaran, target tabungan, utang, tagihan, kategori kustom, dan foto struk. Tindakan ini tidak bisa dibatalkan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showResetStep1 = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
            ) {
                Text("Reset Semua Data")
            }
            if (resetDoneNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    resetDoneNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showResetStep1) {
        AlertDialog(
            onDismissRequest = { showResetStep1 = false },
            title = { Text("Reset semua data?") },
            text = {
                Text(
                    "Yang akan dihapus:\n" +
                        "· $transactionCount transaksi\n" +
                        "· ${wallets.size} dompet\n" +
                        "· ${monthlyIncome.goals.size} target tabungan\n" +
                        "· ${recurrings.size} transaksi berulang\n" +
                        "· ${debts.size} utang/piutang\n" +
                        "· ${bills.size} tagihan\n" +
                        "· ${budgetPositions.size} pos anggaran\n" +
                        "· ${customCategories.size} kategori kustom\n" +
                        "· Semua foto struk\n" +
                        "· Semua pengaturan kembali ke awal\n\n" +
                        "Disarankan ekspor cadangan dulu sebelum menghapus."
                )
            },
            confirmButton = {
                TextButton(onClick = { showResetStep1 = false; showResetStep2 = true }) {
                    Text("Lanjut hapus", color = ExpenseRed)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.exportBackup(); showResetStep1 = false }) {
                        Text("Ekspor cadangan dulu")
                    }
                    TextButton(onClick = { showResetStep1 = false }) { Text("Batal") }
                }
            }
        )
    }
    if (showResetStep2) {
        AlertDialog(
            onDismissRequest = { showResetStep2 = false },
            title = { Text("Hapus SEMUA data?") },
            text = { Text("Ini tidak bisa dibatalkan. Semua catatan, dompet, anggaran, target, utang, tagihan, dan foto struk akan hilang permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetStep2 = false
                        resetDoneNote = "Semua data sudah dihapus — aplikasi kembali seperti baru."
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color.White
                    )
                ) { Text("Ya, hapus semuanya") }
            },
            dismissButton = {
                TextButton(onClick = { showResetStep2 = false }) { Text("Batal") }
            }
        )
    }
    val restore = pendingRestore
    if (restore != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelRestore() },
            title = { Text("Pulihkan cadangan?") },
            text = {
                Text(
                    restore.summary + "\n\nSemua data saat ini akan DIGANTI dengan isi cadangan. Foto struk lama tidak ikut dipulihkan — hanya catatannya."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRestore() }) { Text("Pulihkan") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelRestore() }) { Text("Batal") }
            }
        )
    }
}
