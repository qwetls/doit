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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Wallet
import com.xeylabs.doit.data.WalletType
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.LocalDate

/**
 * Dompet — satu-satunya tempat mengelola dompet dan transfer antar dompet.
 * Saldo tiap dompet = saldo awal + pemasukan − pengeluaran − transfer keluar + transfer masuk.
 */
@Composable
fun WalletsScreen(viewModel: MainViewModel) {
    val walletBalances by viewModel.walletBalances.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    var showWalletEdit by remember { mutableStateOf<Wallet?>(null) }
    var showAddWallet by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<Wallet?>(null) }
    var showTransfer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dompet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Dompet saya")
                Row {
                    IconButton(onClick = { showTransfer = true }, enabled = wallets.size >= 2) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Transfer antar dompet")
                    }
                    IconButton(onClick = { showAddWallet = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah dompet")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (walletBalances.isEmpty()) {
                Text(
                    "Belum ada dompet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                walletBalances.forEach { walletBalance ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                walletBalance.wallet.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                WalletType.fromName(walletBalance.wallet.type).label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            formatRupiah(walletBalance.balance),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showWalletEdit = walletBalance.wallet }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Ubah dompet", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (wallets.size > 1) {
                            IconButton(onClick = { walletToDelete = walletBalance.wallet }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Hapus dompet", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Text(
                "Saldo = saldo awal + pemasukan − pengeluaran − transfer keluar + transfer masuk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(72.dp))
    }

    if (showAddWallet) {
        WalletEditDialog(
            initial = null,
            onDismiss = { showAddWallet = false },
            onSave = { name, type, balance ->
                viewModel.addWallet(name, type, balance)
                showAddWallet = false
            }
        )
    }
    val walletToEdit = showWalletEdit
    if (walletToEdit != null) {
        WalletEditDialog(
            initial = walletToEdit,
            onDismiss = { showWalletEdit = null },
            onSave = { name, type, balance ->
                viewModel.updateWallet(walletToEdit.copy(name = name, type = type.name, initialBalance = balance))
                showWalletEdit = null
            }
        )
    }
    val walletDeleteTarget = walletToDelete
    if (walletDeleteTarget != null) {
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = { Text("Hapus dompet ${walletDeleteTarget.name}?") },
            text = { Text("Transaksi yang memakai dompet ini akan dihitung ke dompet pertama. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWallet(walletDeleteTarget)
                        walletToDelete = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) { Text("Batal") }
            }
        )
    }
    if (showTransfer) {
        TransferDialog(
            wallets = wallets,
            onDismiss = { showTransfer = false },
            onSave = { fromId, toId, amount, note ->
                viewModel.addTransfer(fromId, toId, amount, LocalDate.now(), note)
                showTransfer = false
            }
        )
    }
}
