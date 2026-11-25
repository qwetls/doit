package com.xeylabs.doit.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.Receipts
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.walletCurrency
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    initialType: TxnType? = null
) {
    val lastCategoryName by viewModel.lastCategory.collectAsState()
    val recentCategories by viewModel.recentCategories.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val lastWalletId by viewModel.lastWalletId.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var receiptSaved by remember { mutableStateOf(false) }
    var receiptError by remember { mutableStateOf("") }
    val pickReceipt = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            if (receiptPath != null) Receipts.deleteReceipt(context, receiptPath)
            val saved = Receipts.saveReceipt(context, uri)
            if (saved != null) receiptPath = saved else receiptError = "Foto struk gagal disimpan"
        }
    }

    var type by remember { mutableStateOf(initialType ?: TxnType.EXPENSE) }
    var amount by remember { mutableStateOf(0L) }
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    val effectiveWalletId = selectedWalletId
        ?: wallets.firstOrNull { it.id == lastWalletId }?.id
        ?: wallets.firstOrNull()?.id
    val effectiveWallet = wallets.firstOrNull { it.id == effectiveWalletId }
    val walletCurrency = walletCurrency(effectiveWallet)
    val initialCategory = remember(lastCategoryName) {
        val last = lastCategoryName?.let { Category.fromName(it) }
        if (last != null && last.type == type) last
        else if (type == TxnType.INCOME) Category.GAJI else Category.BELANJA
    }
    var category by remember { mutableStateOf(initialCategory) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var fromWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 0L) }
    var toWalletId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: 0L) }
    var receivedText by remember { mutableStateOf("") }

    val typeCategories = Category.forType(type)
    val orderedCategories = (recentCategories.filter { it.type == type } + typeCategories).distinct()
    val fromWallet = wallets.firstOrNull { it.id == fromWalletId }
    val toWallet = wallets.firstOrNull { it.id == toWalletId }
    val fromCurrency = walletCurrency(fromWallet)
    val toCurrency = walletCurrency(toWallet)
    val crossCurrency = fromWallet != null && toWallet != null && fromCurrency != toCurrency
    val isTransfer = type == TxnType.TRANSFER
    val amountCurrency = if (isTransfer) fromCurrency else walletCurrency

    ModalBottomSheet(
        onDismissRequest = {
            if (!receiptSaved) Receipts.deleteReceipt(context, receiptPath)
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Catat Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == TxnType.EXPENSE,
                    onClick = {
                        type = TxnType.EXPENSE
                        category = Category.BELANJA
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    label = { Text("Pengeluaran") }
                )
                SegmentedButton(
                    selected = type == TxnType.INCOME,
                    onClick = {
                        type = TxnType.INCOME
                        category = Category.GAJI
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    label = { Text("Pemasukan") }
                )
                SegmentedButton(
                    selected = type == TxnType.TRANSFER,
                    onClick = { type = TxnType.TRANSFER },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    label = { Text("Transfer") }
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isTransfer) "Nominal Transfer" else "Nominal Transaksi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatMoney(amount, amountCurrency),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (type == TxnType.INCOME) IncomeGreen else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isTransfer && type == TxnType.EXPENSE) {
                    val totalBudget = state.budgetBelanja + state.budgetDarurat
                    val spent = state.belanjaSpent + state.daruratSpent
                    val daysLeft = YearMonth.now().lengthOfMonth() - LocalDate.now().dayOfMonth + 1
                    if (totalBudget > 0 && daysLeft > 0) {
                        val dailyRemaining = (totalBudget - spent).coerceAtLeast(0) / daysLeft
                        Text(
                            "Sisa anggaran per hari: " + formatRupiah(dailyRemaining),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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

            if (isTransfer) {
                Text("Dari dompet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets, key = { it.id }) { wallet ->
                        FilterChip(
                            selected = fromWalletId == wallet.id,
                            onClick = { fromWalletId = wallet.id },
                            label = { Text(wallet.name + " · " + walletCurrency(wallet).symbol) }
                        )
                    }
                }
                Text("Ke dompet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(wallets, key = { it.id }) { wallet ->
                        FilterChip(
                            selected = toWalletId == wallet.id,
                            onClick = { toWalletId = wallet.id },
                            label = { Text(wallet.name + " · " + walletCurrency(wallet).symbol) }
                        )
                    }
                }
                if (fromWallet != null && toWallet != null && fromWalletId != toWalletId) {
                    Text(
                        fromWallet.name + " → " + toWallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (crossCurrency) {
                    Text(
                        "Mata uang berbeda — tidak ada kurs otomatis. Isi sendiri jumlah yang diterima dompet tujuan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = receivedText,
                        onValueChange = { receivedText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah diterima (" + toCurrency.symbol + ")") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text(
                    if (type == TxnType.INCOME) "Kategori Pemasukan" else "Kategori Pengeluaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                orderedCategories.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { option ->
                            CategoryCell(
                                category = option,
                                selected = category == option,
                                onClick = { category = option },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (wallets.isNotEmpty()) {
                    Text("Dompet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(wallets, key = { it.id }) { wallet ->
                            FilterChip(
                                selected = effectiveWalletId == wallet.id,
                                onClick = { selectedWalletId = wallet.id },
                                label = {
                                    Text(
                                        wallet.name + " · " +
                                            walletCurrency(wallet).symbol
                                    )
                                }
                            )
                        }
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

            if (!isTransfer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lampiran Struk Fisik / QR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusPill(text = "Opsional", color = Emerald)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { pickReceipt.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unggah Foto Struk")
                    }
                    Box(
                        modifier = Modifier.size(56.dp, 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (receiptPath != null) {
                            ReceiptImage(
                                relativePath = receiptPath,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = {
                                    Receipts.deleteReceipt(context, receiptPath)
                                    receiptPath = null
                                },
                                modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Hapus struk",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (receiptError.isNotBlank()) {
                    Text(receiptError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!isTransfer && type == TxnType.EXPENSE && amount > 0) {
                val budgetPair = when (category) {
                    Category.BELANJA -> "Belanja harian" to (state.budgetBelanja - state.belanjaSpent)
                    Category.DARURAT -> "Dana darurat" to (state.budgetDarurat - state.daruratSpent)
                    else -> null
                }
                val pair = budgetPair
                if (pair != null && pair.second > 0) {
                    val pct = (amount * 100 / pair.second).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Insights,
                            contentDescription = null,
                            tint = Emerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Transaksi ini $pct% dari sisa anggaran ${pair.first}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    if (isTransfer) {
                        val received = if (crossCurrency) receivedText.toLongOrNull() else null
                        when {
                            fromWalletId == 0L || toWalletId == 0L -> error = "Pilih dompet asal dan tujuan"
                            fromWalletId == toWalletId -> error = "Dompet asal dan tujuan harus berbeda"
                            amount <= 0 -> error = "Masukkan jumlah lebih dari 0"
                            crossCurrency && (received == null || received <= 0) ->
                                error = "Isi jumlah yang diterima dompet tujuan"
                            else -> {
                                if (receiptPath != null) {
                                    Receipts.deleteReceipt(context, receiptPath)
                                    receiptPath = null
                                }
                                viewModel.addTransfer(fromWalletId, toWalletId, amount, received, date, note)
                                onDismiss()
                            }
                        }
                        return@Button
                    }
                    if (amount <= 0) {
                        error = "Masukkan jumlah lebih dari 0"
                        return@Button
                    }
                    viewModel.addTransaction(type, category, amount, date, note, walletId = effectiveWalletId, receiptPath = receiptPath)
                    receiptSaved = true
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald,
                    contentColor = Color(0xFF003824)
                )
            ) {
                Text(if (isTransfer) "Transfer" else "Simpan Transaksi")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryCell(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryIconTile(category = category, size = 40)
            Text(
                category.label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        if (selected) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    .size(16.dp).clip(CircleShape).background(Emerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF003824),
                    modifier = Modifier.size(10.dp)
                )
            }
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
