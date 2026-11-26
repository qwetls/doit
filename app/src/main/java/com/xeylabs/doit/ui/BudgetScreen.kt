package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.xeylabs.doit.data.BudgetPosition
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.CategoryRef
import com.xeylabs.doit.data.CustomCategory
import com.xeylabs.doit.data.POT_BELANJA
import com.xeylabs.doit.data.POT_DARURAT
import com.xeylabs.doit.data.POT_TABUNGAN
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.builtinCategoryRef
import com.xeylabs.doit.data.categoryRefsForType
import com.xeylabs.doit.data.customPotKey
import com.xeylabs.doit.data.resolveCategoryRef
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun BudgetSection(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val positions by viewModel.budgetPositions.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val monthSpent by viewModel.monthCategorySpent.collectAsState()
    val monthAllocations by viewModel.monthAllocations.collectAsState()
    val autoAllocate by viewModel.autoAllocate.collectAsState()
    val allocPercents by viewModel.allocationPercents.collectAsState()
    val hiddenPots by viewModel.hiddenFixedPots.collectAsState()
    var incomeText by remember(state.monthlyIncome) { mutableStateOf(state.monthlyIncome.toString()) }
    var savedMessage by remember { mutableStateOf("") }
    var showAddPosition by remember { mutableStateOf(false) }
    var showAutoManage by remember { mutableStateOf(false) }
    var allocateTarget by remember { mutableStateOf<AllocateTarget?>(null) }
    var fixedBudgetToEdit by remember { mutableStateOf<FixedBudgetKind?>(null) }
    var fixedPotToDelete by remember { mutableStateOf<FixedBudgetKind?>(null) }
    var positionToEdit by remember { mutableStateOf<BudgetPosition?>(null) }
    var positionToDelete by remember { mutableStateOf<BudgetPosition?>(null) }

    val belanjaVisible = POT_BELANJA !in hiddenPots
    val daruratVisible = POT_DARURAT !in hiddenPots
    val tabunganVisible = POT_TABUNGAN !in hiddenPots
    val customTotal = positions.sumOf { it.limitAmount }
    val customSpent = positions
        .filter { it.category != Category.BELANJA.name && it.category != Category.DARURAT.name }
        .sumOf { monthSpent[it.category] ?: 0L }
    val totalBudget = (if (belanjaVisible) state.budgetBelanja else 0L) +
        (if (daruratVisible) state.budgetDarurat else 0L) + customTotal
    val totalUsed = (if (belanjaVisible) state.belanjaSpent else 0L) +
        (if (daruratVisible) state.daruratSpent else 0L) + customSpent
    val totalRemaining = (totalBudget - totalUsed).coerceAtLeast(0)
    val totalPct = if (totalBudget > 0) (totalUsed * 100 / totalBudget).toInt() else 0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Patokan dari gaji " + formatRupiah(state.monthlyIncome) + ": " + formatRupiah(state.budgetBelanja) + " belanja, " + formatRupiah(state.budgetDarurat) + " darurat, " + formatRupiah(state.budgetTabungan) + " tabungan. Ketuk tombol pensil di tiap pos untuk mengubahnya, tombol Alokasi untuk mencatat alokasi manual.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "SISA ANGGARAN BULANAN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatRupiah(totalRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusPill(text = "$totalPct% Terpakai", color = Emerald)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (totalRemaining > 0) "Aman & Terkendali" else "Anggaran habis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (totalUsed.toFloat() / totalBudget.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = Emerald
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Terpakai: " + formatRupiah(totalUsed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Total: " + formatRupiah(totalBudget),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle("Alokasi Kas Otomatis")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (autoAllocate) {
                            "Aktif. Tiap pemasukan baru (Rp) otomatis dibagi ke pos anggaran. Uang di dompet tidak berpindah, hanya penanda."
                        } else {
                            "Mati. Nyalakan supaya tiap pemasukan baru (Rp) otomatis dibagi ke pos anggaran."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoAllocate,
                    onCheckedChange = { viewModel.setAutoAllocate(it) }
                )
            }
            if (autoAllocate) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Belanja ${allocPercents.first}% · Darurat ${allocPercents.second}% · Tabungan ${allocPercents.third}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showAutoManage = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kelola pembagian")
                }
            }
        }

        DoitCard {
            SectionTitle("Pos Anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (belanjaVisible) {
                    UsageRow(
                        label = "Belanja harian",
                        spent = state.belanjaSpent,
                        budget = state.budgetBelanja,
                        isSavings = false,
                        category = builtinCategoryRef(Category.BELANJA),
                        allocated = monthAllocations[POT_BELANJA] ?: 0L,
                        onEdit = { fixedBudgetToEdit = FixedBudgetKind.BELANJA },
                        onDelete = { fixedPotToDelete = FixedBudgetKind.BELANJA },
                        onAllocate = { allocateTarget = AllocateTarget(POT_BELANJA, "Belanja harian") }
                    )
                }
                if (daruratVisible) {
                    UsageRow(
                        label = "Dana darurat",
                        spent = state.daruratSpent,
                        budget = state.budgetDarurat,
                        isSavings = false,
                        category = builtinCategoryRef(Category.DARURAT),
                        allocated = monthAllocations[POT_DARURAT] ?: 0L,
                        onEdit = { fixedBudgetToEdit = FixedBudgetKind.DARURAT },
                        onDelete = { fixedPotToDelete = FixedBudgetKind.DARURAT },
                        onAllocate = { allocateTarget = AllocateTarget(POT_DARURAT, "Dana darurat") }
                    )
                }
                if (tabunganVisible) {
                    UsageRow(
                        label = "Tabungan (terkumpul bulan ini)",
                        spent = monthAllocations[POT_TABUNGAN] ?: 0L,
                        budget = state.budgetTabungan,
                        isSavings = true,
                        category = builtinCategoryRef(Category.LAIN_KELUAR),
                        onEdit = { fixedBudgetToEdit = FixedBudgetKind.TABUNGAN },
                        onDelete = { fixedPotToDelete = FixedBudgetKind.TABUNGAN },
                        onAllocate = { allocateTarget = AllocateTarget(POT_TABUNGAN, "Tabungan") }
                    )
                }
                positions.forEach { position ->
                    UsageRow(
                        label = position.name,
                        spent = monthSpent[position.category] ?: 0L,
                        budget = position.limitAmount,
                        isSavings = false,
                        category = resolveCategoryRef(position.category, customCategories),
                        allocated = monthAllocations[customPotKey(position.id)] ?: 0L,
                        onEdit = { positionToEdit = position },
                        onDelete = { positionToDelete = position },
                        onAllocate = { allocateTarget = AllocateTarget(customPotKey(position.id), position.name) }
                    )
                }
                OutlinedButton(
                    onClick = { showAddPosition = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Pos Anggaran Baru")
                }
                hiddenPots.forEach { potKey ->
                    val label = when (potKey) {
                        POT_BELANJA -> "Belanja harian"
                        POT_DARURAT -> "Dana darurat"
                        POT_TABUNGAN -> "Tabungan"
                        else -> null
                    }
                    if (label != null) {
                        OutlinedButton(
                            onClick = { viewModel.restoreFixedPot(potKey) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pulihkan pos $label")
                        }
                    }
                }
            }
        }

        DoitCard {
            SectionTitle("Ubah anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            BudgetField("Pemasukan bulanan (Rp)", incomeText) { incomeText = it }
            Button(
                onClick = {
                    val income = incomeText.toLongOrNull()
                    if (income != null && income > 0) {
                        viewModel.setMonthlyIncome(income)
                        savedMessage = "Anggaran disimpan"
                    } else {
                        savedMessage = "Periksa lagi angkanya — pemasukan harus lebih dari 0"
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

        val editingFixed = fixedBudgetToEdit
        if (editingFixed != null) {
            val fixedLabel = when (editingFixed) {
                FixedBudgetKind.BELANJA -> "Belanja harian"
                FixedBudgetKind.DARURAT -> "Dana darurat"
                FixedBudgetKind.TABUNGAN -> "Tabungan"
            }
            val fixedCurrent = when (editingFixed) {
                FixedBudgetKind.BELANJA -> state.budgetBelanja
                FixedBudgetKind.DARURAT -> state.budgetDarurat
                FixedBudgetKind.TABUNGAN -> state.budgetTabungan
            }
            FixedBudgetEditDialog(
                label = fixedLabel,
                currentAmount = fixedCurrent,
                onDismiss = { fixedBudgetToEdit = null },
                onSave = { amount ->
                    when (editingFixed) {
                        FixedBudgetKind.BELANJA -> viewModel.setBudgetBelanja(amount)
                        FixedBudgetKind.DARURAT -> viewModel.setBudgetDarurat(amount)
                        FixedBudgetKind.TABUNGAN -> viewModel.setBudgetTabungan(amount)
                    }
                    fixedBudgetToEdit = null
                }
            )
        }

        if (showAddPosition) {
            BudgetPositionEditDialog(
                initial = null,
                customCategories = customCategories,
                onDismiss = { showAddPosition = false },
                onSave = { name, categoryKey, limit ->
                    viewModel.addBudgetPosition(name, categoryKey, limit)
                    showAddPosition = false
                }
            )
        }
        val editingPosition = positionToEdit
        if (editingPosition != null) {
            BudgetPositionEditDialog(
                initial = editingPosition,
                customCategories = customCategories,
                onDismiss = { positionToEdit = null },
                onSave = { name, categoryKey, limit ->
                    viewModel.updateBudgetPosition(
                        editingPosition.copy(name = name, category = categoryKey, limitAmount = limit)
                    )
                    positionToEdit = null
                }
            )
        }
        val deletingPosition = positionToDelete
        if (deletingPosition != null) {
            AlertDialog(
                onDismissRequest = { positionToDelete = null },
                title = { Text("Hapus pos \"${deletingPosition.name}\"?") },
                text = { Text("Pos anggaran ini akan hilang. Alokasi yang sudah dicatat untuk pos ini ikut terhapus. Transaksi yang sudah dicatat tidak berubah.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBudgetPosition(deletingPosition)
                            positionToDelete = null
                        }
                    ) { Text("Hapus", color = ExpenseRed) }
                },
                dismissButton = {
                    TextButton(onClick = { positionToDelete = null }) { Text("Batal") }
                }
            )
        }

        val deletingFixed = fixedPotToDelete
        if (deletingFixed != null) {
            val fixedLabel = when (deletingFixed) {
                FixedBudgetKind.BELANJA -> "Belanja harian"
                FixedBudgetKind.DARURAT -> "Dana darurat"
                FixedBudgetKind.TABUNGAN -> "Tabungan"
            }
            AlertDialog(
                onDismissRequest = { fixedPotToDelete = null },
                title = { Text("Hapus pos \"$fixedLabel\"?") },
                text = { Text("Pos anggaran ini akan disembunyikan. Alokasi yang sudah dicatat untuk pos ini ikut terhapus. Transaksi yang sudah dicatat tidak berubah. Pos bisa dipulihkan lagi lewat tombol Pulihkan.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteFixedPot(deletingFixed.potKey)
                            fixedPotToDelete = null
                        }
                    ) { Text("Hapus", color = ExpenseRed) }
                },
                dismissButton = {
                    TextButton(onClick = { fixedPotToDelete = null }) { Text("Batal") }
                }
            )
        }

        val allocating = allocateTarget
        if (allocating != null) {
            AllocateDialog(
                label = allocating.label,
                onDismiss = { allocateTarget = null },
                onSave = { amount, note ->
                    viewModel.addManualAllocation(allocating.potKey, amount, note)
                    allocateTarget = null
                }
            )
        }

        if (showAutoManage) {
            AutoSplitManageDialog(
                current = allocPercents,
                onDismiss = { showAutoManage = false },
                onSave = { belanja, darurat, tabungan ->
                    viewModel.setAllocationPercents(belanja, darurat, tabungan)
                    showAutoManage = false
                }
            )
        }
    }
}

@Composable
private fun UsageRow(
    label: String,
    spent: Long,
    budget: Long,
    isSavings: Boolean,
    category: CategoryRef,
    allocated: Long = 0L,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAllocate: (() -> Unit)? = null
) {
    val ratio = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    val pct = (ratio.coerceIn(0f, 1f) * 100).toInt()
    val status: Pair<String, Color> = when {
        ratio >= 1f && isSavings -> "Selesai" to Emerald
        ratio >= 1f -> "Melewati" to ExpenseRed
        ratio >= 0.8f -> "Hampir Habis" to WarningAmber
        isSavings -> "Menabung" to Emerald
        else -> "Stabil" to Emerald
    }
    val barColor: Color = when {
        ratio >= 1f && isSavings -> Emerald
        ratio >= 1f -> ExpenseRed
        ratio >= 0.8f -> WarningAmber
        else -> Emerald
    }
    val remaining = budget - spent
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconTile(ref = category, size = 40)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (allocated > 0 && !isSavings) {
                            "Sisa " + formatRupiah(remaining.coerceAtLeast(0)) + " · Alokasi " + formatRupiah(allocated)
                        } else {
                            "Sisa " + formatRupiah(remaining.coerceAtLeast(0))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatRupiah(spent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "dari " + formatRupiah(budget),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah pos", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus pos", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = barColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$pct% dialokasikan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onAllocate != null) {
                    TextButton(
                        onClick = onAllocate,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Alokasi", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            StatusPill(text = status.first, color = status.second)
        }
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

private enum class FixedBudgetKind(val potKey: String) {
    BELANJA(POT_BELANJA), DARURAT(POT_DARURAT), TABUNGAN(POT_TABUNGAN)
}

private data class AllocateTarget(val potKey: String, val label: String)

@Composable
private fun AllocateDialog(
    label: String,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alokasi ke $label") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Uang di dompet tidak berpindah, ini hanya penanda alokasi bulan ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    value = noteText,
                    onValueChange = { noteText = it },
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
                if (amount == null || amount <= 0) {
                    error = "Isi jumlah yang benar"
                    return@Button
                }
                onSave(amount, noteText)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun AutoSplitManageDialog(
    current: Triple<Int, Int, Int>,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int) -> Unit
) {
    var belanjaText by remember { mutableStateOf(current.first.toString()) }
    var daruratText by remember { mutableStateOf(current.second.toString()) }
    var tabunganText by remember { mutableStateOf(current.third.toString()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kelola pembagian otomatis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Total ketiga pos harus 100%. Pos yang dihapus tidak ikut pembagian, bagiannya dibagi ulang ke pos yang masih ada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = belanjaText,
                    onValueChange = { belanjaText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Belanja harian (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daruratText,
                    onValueChange = { daruratText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Dana darurat (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tabunganText,
                    onValueChange = { tabunganText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tabungan (%)") },
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
                val belanja = belanjaText.toIntOrNull()
                val darurat = daruratText.toIntOrNull()
                val tabungan = tabunganText.toIntOrNull()
                if (belanja == null || darurat == null || tabungan == null ||
                    belanja !in 0..100 || darurat !in 0..100 || tabungan !in 0..100
                ) {
                    error = "Isi persen 0–100 untuk tiap pos"
                    return@Button
                }
                if (belanja + darurat + tabungan != 100) {
                    error = "Total harus 100% (sekarang ${belanja + darurat + tabungan}%)"
                    return@Button
                }
                onSave(belanja, darurat, tabungan)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun FixedBudgetEditDialog(
    label: String,
    currentAmount: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf(currentAmount.toString()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah $label") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (Rp)") },
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
                if (amount == null || amount < 0) {
                    error = "Isi jumlah yang benar"
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

@Composable
private fun BudgetPositionEditDialog(
    initial: BudgetPosition?,
    customCategories: List<CustomCategory>,
    onDismiss: () -> Unit,
    onSave: (name: String, categoryKey: String, limit: Long) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember {
        mutableStateOf(
            initial?.let { resolveCategoryRef(it.category, customCategories) }
                ?: builtinCategoryRef(Category.MAKAN)
        )
    }
    var limitText by remember { mutableStateOf(if (initial != null) initial.limitAmount.toString() else "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Pos anggaran baru" else "Ubah pos anggaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama pos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryRefsForType(TxnType.EXPENSE, customCategories), key = { it.key }) { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Batas bulanan (Rp)") },
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
                val limit = limitText.toLongOrNull()
                if (name.isBlank() || limit == null || limit <= 0) {
                    error = "Isi nama dan batas yang benar"
                    return@Button
                }
                onSave(name.trim(), category.key, limit)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
