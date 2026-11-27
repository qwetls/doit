package com.xeylabs.doit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.Wallet
import com.xeylabs.doit.data.WalletType
import com.xeylabs.doit.data.walletCurrency
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun WalletsSection(viewModel: MainViewModel) {
    val walletBalances by viewModel.walletBalances.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    var showWalletEdit by remember { mutableStateOf<Wallet?>(null) }
    var showAddWallet by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<Wallet?>(null) }
    var showTransfer by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DoitCard {
            SectionTitle("Cash vs E-money")
            Spacer(modifier = Modifier.height(8.dp))
            val currencies = walletBalances.map { it.wallet.currency ?: "IDR" }.distinct()
            if (walletBalances.isEmpty()) {
                Text(
                    "Belum ada dompet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                currencies.forEach { code ->
                    val currency = Currency.fromCode(code)
                    fun totalOf(type: WalletType): Long =
                        walletBalances
                            .filter {
                                WalletType.fromName(it.wallet.type) == type &&
                                    (it.wallet.currency ?: "IDR") == code
                            }
                            .sumOf { it.balance }
                    val label = if (code == "IDR") "Rupiah" else "Dollar"
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tunai", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatMoney(totalOf(WalletType.TUNAI), currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("E-money", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatMoney(totalOf(WalletType.EWALLET), currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bank", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatMoney(totalOf(WalletType.BANK), currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionTitle("Akun & Sumber Dana")
                    val totalLiquid = walletBalances.sumOf { it.balance }
                    Text(
                        "Total saldo likuid: " + formatMoney(totalLiquid, Currency.IDR),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { showTransfer = true }, enabled = wallets.size >= 2) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Transfer antar dompet")
                    }
                    IconButton(onClick = { showAddWallet = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah dompet")
                    }
                }
            }
            if (walletBalances.isEmpty()) {
                DoitCard {
                    Text(
                        "Belum ada dompet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                walletBalances.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { walletBalance ->
                            WalletGridCell(
                                wallet = walletBalance.wallet,
                                balance = walletBalance.balance,
                                onClick = { showWalletEdit = walletBalance.wallet },
                                onEdit = { showWalletEdit = walletBalance.wallet },
                                onDelete = if (wallets.size > 1) {
                                    { walletToDelete = walletBalance.wallet }
                                } else null,
                                onSetDefault = { viewModel.setDefaultWallet(walletBalance.wallet) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(2 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
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
    }

    if (showAddWallet) {
        WalletEditDialog(
            initial = null,
            onDismiss = { showAddWallet = false },
            onSave = { name, type, currency, balance ->
                viewModel.addWallet(name, type, currency, balance)
                showAddWallet = false
            }
        )
    }
    val walletToEdit = showWalletEdit
    if (walletToEdit != null) {
        WalletEditDialog(
            initial = walletToEdit,
            onDismiss = { showWalletEdit = null },
            onSave = { name, type, currency, balance ->
                viewModel.updateWallet(
                    walletToEdit.copy(
                        name = name,
                        type = type.name,
                        currency = currency.code,
                        initialBalance = balance
                    )
                )
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
            onSave = { fromId, toId, amount, receivedAmount, note ->
                viewModel.addTransfer(fromId, toId, amount, receivedAmount, note = note)
                showTransfer = false
            }
        )
    }
}

@Composable
private fun WalletGridCell(
    wallet: Wallet,
    balance: Long,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = walletCurrency(wallet)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .height(128.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                        .background(Emerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = walletTypeIcon(WalletType.fromName(wallet.type)),
                        contentDescription = null,
                        tint = Emerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSetDefault,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (wallet.isDefault) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Jadikan dompet default",
                            tint = if (wallet.isDefault) Emerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Ubah dompet",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Hapus dompet",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    formatMoney(balance, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    WalletType.fromName(wallet.type).label + " · " + currency.symbol +
                        if (wallet.isDefault) " · Default" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
