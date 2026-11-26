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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Bill
import com.xeylabs.doit.data.Debt
import com.xeylabs.doit.data.DebtDirection
import com.xeylabs.doit.data.DebtKind
import com.xeylabs.doit.data.RecurringFrequency
import com.xeylabs.doit.data.RecurringTransaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.effectiveKind
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Rencana — satu-satunya tempat perencanaan & kewajiban: anggaran, target tabungan,
 * transaksi berulang, utang & piutang, tagihan. Tiap bagian punya judul sendiri dan
 * membuka dialog pengelolaannya masing-masing. Tidak ada ringkasan laporan di sini.
 */
@Composable
fun PlansScreen(viewModel: MainViewModel, onNavigate: (DoitTab) -> Unit) {
    val month by viewModel.selectedMonth.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val today = LocalDate.now()
    val isCurrentMonth = month == YearMonth.now()
    val daysLeft = YearMonth.now().lengthOfMonth() - today.dayOfMonth

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FluxHeader(subtitle = "Anggaran", displayName = displayName) {
            onNavigate(DoitTab.AKUN)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonthStepper(
                month = month,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() },
                modifier = Modifier.weight(1f)
            )
            if (isCurrentMonth) {
                StatusPill(text = "Tersisa $daysLeft hari", color = Emerald)
            }
        }

        BudgetSection(viewModel)

        WalletsSection(viewModel)

        SectionTitle("Target tabungan")
        SavingsSection(viewModel)

        SectionTitle("Transaksi berulang")
        RecurringSection(viewModel)

        SectionTitle("Paylater & Utang Piutang")
        DebtsSection(viewModel)

        SectionTitle("Tagihan & pengingat")
        BillsSection(viewModel)

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun RecurringSection(viewModel: MainViewModel) {
    val wallets by viewModel.wallets.collectAsState()
    val recurrings by viewModel.recurrings.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()

    var showRecurringEdit by remember { mutableStateOf<RecurringTransaction?>(null) }
    var showAddRecurring by remember { mutableStateOf(false) }
    var recurringToDelete by remember { mutableStateOf<RecurringTransaction?>(null) }

    DoitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Transaksi berulang")
            IconButton(onClick = { showAddRecurring = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah transaksi rutin")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (recurrings.isEmpty()) {
            Text(
                "Belum ada transaksi rutin. Contoh: gaji tiap tanggal 25, atau iuran tiap Senin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            recurrings.forEach { recurring ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            recurring.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            formatRupiah(recurring.amount) + " · " +
                                RecurringFrequency.fromName(recurring.frequency).label + " · " +
                                recurringScheduleText(recurring) + " · " +
                                viewModel.walletName(recurring.walletId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            viewModel.categoryLabel(recurring.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (recurring.type == TxnType.INCOME.name) IncomeGreen else ExpenseRed
                        )
                    }
                    Switch(
                        checked = recurring.isActive,
                        onCheckedChange = { viewModel.setRecurringActive(recurring, it) }
                    )
                    IconButton(onClick = { showRecurringEdit = recurring }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah rutin", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { recurringToDelete = recurring }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus rutin", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddRecurring) {
        RecurringEditDialog(
            initial = null,
            wallets = wallets,
            customCategories = customCategories,
            onDismiss = { showAddRecurring = false },
            onSave = { name, amount, type, categoryKey, walletId, frequency, dayOfMonth, dayOfWeek ->
                viewModel.addRecurring(
                    RecurringTransaction(
                        name = name,
                        amount = amount,
                        type = type.name,
                        category = categoryKey,
                        walletId = walletId,
                        frequency = frequency.name,
                        dayOfMonth = dayOfMonth,
                        dayOfWeek = dayOfWeek,
                        isActive = true,
                        createdEpochDay = LocalDate.now().toEpochDay()
                    )
                )
                showAddRecurring = false
            }
        )
    }
    val recurringToEdit = showRecurringEdit
    if (recurringToEdit != null) {
        RecurringEditDialog(
            initial = recurringToEdit,
            wallets = wallets,
            customCategories = customCategories,
            onDismiss = { showRecurringEdit = null },
            onSave = { name, amount, type, categoryKey, walletId, frequency, dayOfMonth, dayOfWeek ->
                viewModel.updateRecurring(
                    recurringToEdit.copy(
                        name = name,
                        amount = amount,
                        type = type.name,
                        category = categoryKey,
                        walletId = walletId,
                        frequency = frequency.name,
                        dayOfMonth = dayOfMonth,
                        dayOfWeek = dayOfWeek
                    )
                )
                showRecurringEdit = null
            }
        )
    }
    val recurringDeleteTarget = recurringToDelete
    if (recurringDeleteTarget != null) {
        AlertDialog(
            onDismissRequest = { recurringToDelete = null },
            title = { Text("Hapus \"${recurringDeleteTarget.name}\"?") },
            text = { Text("Transaksi yang sudah dibuat dari jadwal ini tetap ada di riwayat.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecurring(recurringDeleteTarget)
                        recurringToDelete = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { recurringToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun DebtsSection(viewModel: MainViewModel) {
    val debts by viewModel.debts.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    var showAddDebt by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<Debt?>(null) }
    var debtToPay by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }

    DoitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Paylater & Utang Piutang")
            IconButton(onClick = { showAddDebt = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah paylater/utang/piutang")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (debts.isEmpty()) {
            Text(
                "Belum ada paylater, utang, atau piutang.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            debts.forEach { debt ->
                val remaining = (debt.amount - debt.paid).coerceAtLeast(0)
                val direction = DebtDirection.fromName(debt.direction)
                val kind = debt.effectiveKind()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            debt.personName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            kind.label +
                                (if (!debt.provider.isNullOrBlank()) " · " + debt.provider else "") +
                                (if (kind == DebtKind.PAYLATER) "" else " · " + direction.label) +
                                " · " + formatDate(debt.dateEpochDay),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (debt.isSettled) {
                                "Lunas · " + formatRupiah(debt.amount)
                            } else {
                                "Sisa " + formatRupiah(remaining) + " dari " + formatRupiah(debt.amount)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (debt.isSettled) IncomeGreen else ExpenseRed
                        )
                    }
                    if (!debt.isSettled) {
                        TextButton(onClick = { debtToPay = debt }) { Text("Bayar") }
                    }
                    IconButton(onClick = { debtToEdit = debt }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { debtToDelete = debt }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddDebt) {
        DebtEditDialog(
            onDismiss = { showAddDebt = false },
            onSave = { personName, amount, direction, kind, provider, date ->
                viewModel.addDebt(personName, amount, direction, kind, provider, date)
                showAddDebt = false
            }
        )
    }
    val debtEditTarget = debtToEdit
    if (debtEditTarget != null) {
        DebtEditDialog(
            initial = debtEditTarget,
            onDismiss = { debtToEdit = null },
            onSave = { personName, amount, direction, kind, provider, date ->
                viewModel.updateDebt(
                    debtEditTarget.copy(
                        personName = personName,
                        amount = amount,
                        direction = direction.name,
                        kind = kind.name,
                        provider = provider.ifBlank { null },
                        dateEpochDay = date.toEpochDay()
                    )
                )
                debtToEdit = null
            }
        )
    }
    val debtPayTarget = debtToPay
    if (debtPayTarget != null) {
        DebtPayDialog(
            debt = debtPayTarget,
            wallets = wallets,
            onDismiss = { debtToPay = null },
            onSave = { amount, walletId ->
                viewModel.recordDebtPayment(debtPayTarget, amount, walletId)
                debtToPay = null
            }
        )
    }
    val debtDeleteTarget = debtToDelete
    if (debtDeleteTarget != null) {
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Hapus catatan utang ${debtDeleteTarget.personName}?") },
            text = { Text("Catatan utang/piutang ini akan hilang.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDebt(debtDeleteTarget)
                        debtToDelete = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun BillsSection(viewModel: MainViewModel) {
    val bills by viewModel.bills.collectAsState()

    var showBillEdit by remember { mutableStateOf<Bill?>(null) }
    var showAddBill by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    DoitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Tagihan & pengingat")
            IconButton(onClick = { showAddBill = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah tagihan")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (bills.isEmpty()) {
            Text(
                "Belum ada tagihan. Tambahkan tagihan bulanan seperti listrik atau air.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val currentMonthKey = YearMonth.now().toString()
            bills.forEach { bill ->
                val paidThisMonth = bill.paidMonth == currentMonthKey
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            bill.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            formatRupiah(bill.amount) + " · jatuh tempo tiap tanggal " + bill.dueDayOfMonth,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (paidThisMonth) "Lunas bulan ini" else "Belum dibayar bulan ini",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (paidThisMonth) IncomeGreen else ExpenseRed
                        )
                    }
                    if (!paidThisMonth) {
                        TextButton(onClick = { viewModel.markBillPaid(bill) }) { Text("Bayar") }
                    }
                    IconButton(onClick = { showBillEdit = bill }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah tagihan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { billToDelete = bill }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus tagihan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddBill) {
        BillEditDialog(
            initial = null,
            onDismiss = { showAddBill = false },
            onSave = { name, amount, dueDay ->
                viewModel.addBill(name, amount, dueDay)
                showAddBill = false
            }
        )
    }
    val billToEdit = showBillEdit
    if (billToEdit != null) {
        BillEditDialog(
            initial = billToEdit,
            onDismiss = { showBillEdit = null },
            onSave = { name, amount, dueDay ->
                viewModel.updateBill(billToEdit.copy(name = name, amount = amount, dueDayOfMonth = dueDay))
                showBillEdit = null
            }
        )
    }
    val billDeleteTarget = billToDelete
    if (billDeleteTarget != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Hapus tagihan ${billDeleteTarget.name}?") },
            text = { Text("Pengingat tagihan ini akan hilang.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBill(billDeleteTarget)
                        billToDelete = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) { Text("Batal") }
            }
        )
    }
}

private fun recurringScheduleText(recurring: RecurringTransaction): String {
    return when (RecurringFrequency.fromName(recurring.frequency)) {
        RecurringFrequency.MONTHLY -> "tgl ${recurring.dayOfMonth}"
        RecurringFrequency.WEEKLY -> listOf(
            "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        ).getOrNull(recurring.dayOfWeek - 1) ?: ""
    }
}
