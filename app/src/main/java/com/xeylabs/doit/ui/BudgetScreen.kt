package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.xeylabs.doit.data.BudgetPosition
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.CategoryRef
import com.xeylabs.doit.data.CustomCategory
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.builtinCategoryRef
import com.xeylabs.doit.data.categoryRefsForType
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
    var incomeText by remember(state.monthlyIncome) { mutableStateOf(state.monthlyIncome.toString()) }
    var belanjaText by remember(state.budgetBelanja) { mutableStateOf(state.budgetBelanja.toString()) }
    var daruratText by remember(state.budgetDarurat) { mutableStateOf(state.budgetDarurat.toString()) }
    var tabunganText by remember(state.budgetTabungan) { mutableStateOf(state.budgetTabungan.toString()) }
    var savedMessage by remember { mutableStateOf("") }
    var showAddPosition by remember { mutableStateOf(false) }
    var positionToEdit by remember { mutableStateOf<BudgetPosition?>(null) }
    var positionToDelete by remember { mutableStateOf<BudgetPosition?>(null) }

    val savedThisMonth = (state.monthIncome - state.monthExpense).coerceAtLeast(0)
    val customTotal = positions.sumOf { it.limitAmount }
    val customSpent = positions
        .filter { it.category != Category.BELANJA.name && it.category != Category.DARURAT.name }
        .sumOf { monthSpent[it.category] ?: 0L }
    val totalBudget = state.budgetBelanja + state.budgetDarurat + state.budgetTabungan + customTotal
    val totalUsed = state.belanjaSpent + state.daruratSpent + savedThisMonth + customSpent
    val totalRemaining = (totalBudget - totalUsed).coerceAtLeast(0)
    val totalPct = if (totalBudget > 0) (totalUsed * 100 / totalBudget).toInt() else 0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Patokan dari gaji Rp1.000.000: 200rb belanja, 100rb darurat, 700rb tabungan. Ubah sesuai kebutuhanmu.",
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
            SectionTitle("Pos Anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UsageRow(
                    label = "Belanja harian",
                    spent = state.belanjaSpent,
                    budget = state.budgetBelanja,
                    isSavings = false,
                    category = builtinCategoryRef(Category.BELANJA)
                )
                UsageRow(
                    label = "Dana darurat",
                    spent = state.daruratSpent,
                    budget = state.budgetDarurat,
                    isSavings = false,
                    category = builtinCategoryRef(Category.DARURAT)
                )
                UsageRow(
                    label = "Tabungan (terkumpul bulan ini)",
                    spent = savedThisMonth,
                    budget = state.budgetTabungan,
                    isSavings = true,
                    category = builtinCategoryRef(Category.LAIN_KELUAR)
                )
                positions.forEach { position ->
                    UsageRow(
                        label = position.name,
                        spent = monthSpent[position.category] ?: 0L,
                        budget = position.limitAmount,
                        isSavings = false,
                        category = resolveCategoryRef(position.category, customCategories),
                        onEdit = { positionToEdit = position },
                        onDelete = { positionToDelete = position }
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
            }
        }

        DoitCard {
            SectionTitle("Ubah anggaran")
            Spacer(modifier = Modifier.height(12.dp))
            BudgetField("Pemasukan bulanan (Rp)", incomeText) { incomeText = it }
            BudgetField("Belanja harian (Rp)", belanjaText) { belanjaText = it }
            BudgetField("Dana darurat (Rp)", daruratText) { daruratText = it }
            BudgetField("Jatah tabungan (Rp)", tabunganText) { tabunganText = it }
            Button(
                onClick = {
                    val income = incomeText.toLongOrNull()
                    val belanja = belanjaText.toLongOrNull()
                    val darurat = daruratText.toLongOrNull()
                    val tabungan = tabunganText.toLongOrNull()
                    if (income != null && income > 0 && belanja != null && belanja >= 0 &&
                        darurat != null && darurat >= 0 && tabungan != null && tabungan >= 0
                    ) {
                        viewModel.setMonthlyIncome(income)
                        viewModel.setBudgetBelanja(belanja)
                        viewModel.setBudgetDarurat(darurat)
                        viewModel.setBudgetTabungan(tabungan)
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
                text = { Text("Pos anggaran ini akan hilang. Transaksi yang sudah dicatat tidak berubah.") },
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
    }
}

@Composable
private fun UsageRow(
    label: String,
    spent: Long,
    budget: Long,
    isSavings: Boolean,
    category: CategoryRef,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
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
                        "Sisa " + formatRupiah(remaining.coerceAtLeast(0)),
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
            Text(
                "$pct% dialokasikan",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
