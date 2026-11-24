package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val lastCategoryName by viewModel.lastCategory.collectAsState()
    val recentCategories by viewModel.recentCategories.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val lastWalletId by viewModel.lastWalletId.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(TxnType.EXPENSE) }
    var amount by remember { mutableStateOf(0L) }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    val effectiveWalletId = selectedWalletId
        ?: wallets.firstOrNull { it.id == lastWalletId }?.id
        ?: wallets.firstOrNull()?.id
    val initialCategory = remember(lastCategoryName) {
        val last = lastCategoryName?.let { Category.fromName(it) }
        if (last != null && last.type == TxnType.EXPENSE) last else Category.BELANJA
    }
    var category by remember { mutableStateOf(initialCategory) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val typeCategories = Category.forType(type)
    val orderedCategories = (recentCategories.filter { it.type == type } + typeCategories).distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Catat cepat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == TxnType.EXPENSE,
                    onClick = {
                        type = TxnType.EXPENSE
                        category = Category.BELANJA
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("Pengeluaran") }
                )
                SegmentedButton(
                    selected = type == TxnType.INCOME,
                    onClick = {
                        type = TxnType.INCOME
                        category = Category.GAJI
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("Pemasukan") }
                )
            }

            Text(
                text = formatRupiah(amount),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (type == TxnType.INCOME) {
                    com.xeylabs.doit.ui.theme.IncomeGreen
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "0", "⌫")
            keys.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        KeyButton(
                            label = key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                amount = when (key) {
                                    "⌫" -> amount / 10
                                    "000" -> (amount * 1000).coerceAtMost(999_999_999_999L)
                                    else -> (amount * 10 + key.toLong()).coerceAtMost(999_999_999_999L)
                                }
                            }
                        )
                    }
                }
            }

            Text("Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orderedCategories) { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = { Text(option.label) }
                    )
                }
            }

            if (wallets.isNotEmpty()) {
                Text("Dompet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets, key = { it.id }) { wallet ->
                        FilterChip(
                            selected = effectiveWalletId == wallet.id,
                            onClick = { selectedWalletId = wallet.id },
                            label = { Text(wallet.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { date = date.minusDays(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Sehari sebelumnya")
                }
                Text(
                    text = formatDate(date.toEpochDay()),
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = { date = date.plusDays(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Sehari setelahnya")
                }
            }
            TextButton(onClick = { date = LocalDate.now() }) {
                Text("Hari ini")
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (opsional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    if (amount <= 0) {
                        error = "Masukkan jumlah lebih dari 0"
                        return@Button
                    }
                    viewModel.addTransaction(type, category, amount, date, note, walletId = effectiveWalletId)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KeyButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(52.dp)
    ) {
        if (label == "⌫") {
            Icon(Icons.Filled.Backspace, contentDescription = "Hapus angka")
        } else {
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        }
    }
}
