package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Bill
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.Debt
import com.xeylabs.doit.data.DebtDirection
import com.xeylabs.doit.data.RecurringFrequency
import com.xeylabs.doit.data.RecurringTransaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.Wallet
import com.xeylabs.doit.data.WalletType
import java.time.LocalDate

// ---------- Dompet ----------

@Composable
fun WalletEditDialog(
    initial: Wallet?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: WalletType, initialBalance: Long) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.let { WalletType.fromName(it.type) } ?: WalletType.TUNAI) }
    var balanceText by remember { mutableStateOf(if (initial != null) initial.initialBalance.toString() else "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Dompet baru" else "Ubah dompet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama dompet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipe", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WalletType.entries) { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Saldo awal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val balance = balanceText.toLongOrNull() ?: 0L
                if (name.isBlank()) {
                    error = "Isi nama dompet"
                    return@Button
                }
                onSave(name.trim(), type, balance)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onSave: (fromId: Long, toId: Long, amount: Long, note: String) -> Unit
) {
    var fromId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun walletName(id: Long): String = wallets.firstOrNull { it.id == id }?.name ?: "-"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer antar dompet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Transfer hanya memindahkan uang — bukan pemasukan atau pengeluaran.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = walletName(fromId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dari dompet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        wallets.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = { fromId = option.id; fromExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = walletName(toId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ke dompet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        wallets.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = { toId = option.id; toExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                when {
                    fromId == 0L || toId == 0L -> error = "Pilih dompet asal dan tujuan"
                    fromId == toId -> error = "Dompet asal dan tujuan harus berbeda"
                    amount == null || amount <= 0 -> error = "Masukkan jumlah lebih dari 0"
                    else -> onSave(fromId, toId, amount, note.trim())
                }
            }) { Text("Transfer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ---------- Transaksi berulang ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringEditDialog(
    initial: RecurringTransaction?,
    wallets: List<Wallet>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: Long,
        type: TxnType,
        category: Category,
        walletId: Long,
        frequency: RecurringFrequency,
        dayOfMonth: Int,
        dayOfWeek: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(if (initial != null) initial.amount.toString() else "") }
    var type by remember {
        mutableStateOf(initial?.let { runCatching { TxnType.valueOf(it.type) }.getOrDefault(TxnType.EXPENSE) } ?: TxnType.EXPENSE)
    }
    var category by remember {
        mutableStateOf(initial?.let { Category.fromName(it.category) } ?: Category.BELANJA)
    }
    var walletId by remember { mutableStateOf(initial?.walletId ?: wallets.firstOrNull()?.id ?: 0L) }
    var frequency by remember {
        mutableStateOf(initial?.let { RecurringFrequency.fromName(it.frequency) } ?: RecurringFrequency.MONTHLY)
    }
    var dayOfMonthText by remember { mutableStateOf(initial?.dayOfMonth?.toString() ?: "1") }
    var dayOfWeek by remember { mutableStateOf(initial?.dayOfWeek ?: 1) }
    var walletExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val categories = Category.forType(type)
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val walletName = wallets.firstOrNull { it.id == walletId }?.name ?: "-"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Transaksi rutin baru" else "Ubah transaksi rutin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama (mis. Gaji, Listrik)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = type == TxnType.EXPENSE,
                        onClick = { type = TxnType.EXPENSE; category = Category.BELANJA },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("Pengeluaran") }
                    )
                    SegmentedButton(
                        selected = type == TxnType.INCOME,
                        onClick = { type = TxnType.INCOME; category = Category.GAJI },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("Pemasukan") }
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = walletExpanded,
                    onExpandedChange = { walletExpanded = !walletExpanded }
                ) {
                    OutlinedTextField(
                        value = walletName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dompet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = walletExpanded,
                        onDismissRequest = { walletExpanded = false }
                    ) {
                        wallets.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = { walletId = option.id; walletExpanded = false }
                            )
                        }
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = frequency == RecurringFrequency.MONTHLY,
                        onClick = { frequency = RecurringFrequency.MONTHLY },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("Bulanan") }
                    )
                    SegmentedButton(
                        selected = frequency == RecurringFrequency.WEEKLY,
                        onClick = { frequency = RecurringFrequency.WEEKLY },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("Mingguan") }
                    )
                }
                if (frequency == RecurringFrequency.MONTHLY) {
                    OutlinedTextField(
                        value = dayOfMonthText,
                        onValueChange = { dayOfMonthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tanggal tiap bulan (1–31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Hari", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dayNames.size) { index ->
                            FilterChip(
                                selected = dayOfWeek == index + 1,
                                onClick = { dayOfWeek = index + 1 },
                                label = { Text(dayNames[index]) }
                            )
                        }
                    }
                }
                Text(
                    "Dicatat otomatis saat aplikasi dibuka dan tanggalnya sudah lewat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                val dayOfMonth = dayOfMonthText.toIntOrNull()
                when {
                    name.isBlank() -> error = "Isi nama transaksi rutin"
                    amount == null || amount <= 0 -> error = "Masukkan jumlah lebih dari 0"
                    walletId == 0L -> error = "Pilih dompet"
                    frequency == RecurringFrequency.MONTHLY && (dayOfMonth == null || dayOfMonth !in 1..31) ->
                        error = "Tanggal harus 1–31"
                    else -> onSave(
                        name.trim(), amount, type, category, walletId, frequency,
                        dayOfMonth ?: 1, dayOfWeek
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ---------- Utang & piutang ----------

@Composable
fun DebtEditDialog(
    onDismiss: () -> Unit,
    onSave: (personName: String, amount: Long, direction: DebtDirection, date: LocalDate) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(DebtDirection.BORROWED) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Utang / piutang baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Nama orang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = direction == DebtDirection.BORROWED,
                        onClick = { direction = DebtDirection.BORROWED },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("Kamu meminjam") }
                    )
                    SegmentedButton(
                        selected = direction == DebtDirection.LENT,
                        onClick = { direction = DebtDirection.LENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("Kamu meminjamkan") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { date = date.minusDays(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Sehari sebelumnya")
                    }
                    Text(text = formatDate(date.toEpochDay()), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { date = date.plusDays(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Sehari setelahnya")
                    }
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                when {
                    personName.isBlank() -> error = "Isi nama orangnya"
                    amount == null || amount <= 0 -> error = "Masukkan jumlah lebih dari 0"
                    else -> onSave(personName.trim(), amount, direction, date)
                }
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun DebtPayDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val remaining = (debt.amount - debt.paid).coerceAtLeast(0)
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat pembayaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${debt.personName} · sisa " + formatRupiah(remaining),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah dibayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                if (amount == null || amount <= 0) {
                    error = "Masukkan jumlah lebih dari 0"
                    return@Button
                }
                onSave(amount)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// ---------- Tagihan ----------

@Composable
fun BillEditDialog(
    initial: Bill?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, dueDayOfMonth: Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(if (initial != null) initial.amount.toString() else "") }
    var dueDayText by remember { mutableStateOf(initial?.dueDayOfMonth?.toString() ?: "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tagihan baru" else "Ubah tagihan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama tagihan (mis. Listrik)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jatuh tempo tiap bulan, tanggal (1–31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                val dueDay = dueDayText.toIntOrNull()
                when {
                    name.isBlank() -> error = "Isi nama tagihan"
                    amount == null || amount <= 0 -> error = "Masukkan jumlah lebih dari 0"
                    dueDay == null || dueDay !in 1..31 -> error = "Tanggal jatuh tempo harus 1–31"
                    else -> onSave(name.trim(), amount, dueDay)
                }
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
