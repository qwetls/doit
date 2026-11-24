package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.SavingsGoal
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SavingsSection(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showAddGoal by remember { mutableStateOf(false) }
    var addAmountGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var editGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var deleteGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    val totalSaved = state.goals.sumOf { it.saved }
    val totalTarget = state.goals.sumOf { it.target }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DoitCard {
            Text(
                "Total terkumpul",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedMoney(amount = totalSaved, style = MaterialTheme.typography.headlineLarge)
            Text(
                "dari total target " + formatRupiah(totalTarget),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Rata-rata nabung bersih: " + formatRupiah(state.avgMonthlyNet) + " / bulan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.goals.isEmpty()) {
            DoitCard {
                Text(
                    "Belum ada target tabungan.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Buat target pertamamu — misalnya Laptop, HP baru, atau Dana darurat besar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            state.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    monthsToTarget = if (goal.isDefault) state.monthsToTarget else null,
                    projectedDate = if (goal.isDefault) state.projectedDate else null,
                    onAdd = { addAmountGoal = goal },
                    onEdit = { editGoal = goal },
                    onDelete = { deleteGoal = goal }
                )
            }
        }

        Button(
            onClick = { showAddGoal = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat target baru")
        }
    }

    if (showAddGoal) {
        GoalEditDialog(
            title = "Target baru",
            initialName = "",
            initialTarget = 0L,
            onDismiss = { showAddGoal = false },
            onSave = { name, target ->
                viewModel.addGoal(name, target)
                showAddGoal = false
            }
        )
    }

    val goalToAdd = addAmountGoal
    if (goalToAdd != null) {
        AddSavingsDialog(
            goal = goalToAdd,
            onDismiss = { addAmountGoal = null },
            onSave = { amount ->
                viewModel.addToGoal(goalToAdd, amount)
                addAmountGoal = null
            }
        )
    }

    val goalToEdit = editGoal
    if (goalToEdit != null) {
        GoalEditDialog(
            title = "Ubah target",
            initialName = goalToEdit.name,
            initialTarget = goalToEdit.target,
            onDismiss = { editGoal = null },
            onSave = { name, target ->
                viewModel.updateGoal(goalToEdit.copy(name = name, target = target))
                editGoal = null
            }
        )
    }

    val goalToDelete = deleteGoal
    if (goalToDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteGoal = null },
            title = { Text("Hapus target ${goalToDelete.name}?") },
            text = { Text("Target dan progres tabungannya akan hilang. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGoal(goalToDelete)
                        deleteGoal = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteGoal = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    monthsToTarget: Long?,
    projectedDate: java.time.LocalDate?,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.target > 0) {
        (goal.saved.toFloat() / goal.target.toFloat()).coerceIn(0f, 1f)
    } else 0f
    DoitCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (goal.isDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UTAMA",
                        style = MaterialTheme.typography.labelMedium,
                        color = VioletAccent
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Ubah target", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!goal.isDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus target", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BudgetRing(
                progress = progress,
                color = VioletAccent,
                centerText = "${(progress * 100).toInt()}%",
                size = 84.dp,
                strokeWidth = 9.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedMoney(amount = goal.saved, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "dari " + formatRupiah(goal.target),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Sisa: " + formatRupiah((goal.target - goal.saved).coerceAtLeast(0)),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (goal.isDefault) {
                    val estimateText = when {
                        monthsToTarget == 0L -> "Target tercapai!"
                        monthsToTarget != null && projectedDate != null ->
                            "Perkiraan tercapai sekitar " + projectedDate.format(
                                DateTimeFormatter.ofPattern("MMM yyyy", Locale("in", "ID"))
                            )
                        else -> "Belum bisa diperkirakan"
                    }
                    Text(estimateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text("Tambah tabungan")
        }
    }
}

@Composable
private fun AddSavingsDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val quickAmounts = listOf(50_000L, 100_000L, 200_000L, 500_000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah ke ${goal.name}") },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAmounts.forEach { quick ->
                        TextButton(onClick = { amountText = quick.toString() }) {
                            Text(formatRupiah(quick).removePrefix("Rp"))
                        }
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

@Composable
private fun GoalEditDialog(
    title: String,
    initialName: String,
    initialTarget: Long,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var targetText by remember { mutableStateOf(if (initialTarget > 0) initialTarget.toString() else "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama target") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target (Rp)") },
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
                val target = targetText.toLongOrNull()
                if (name.isBlank() || target == null || target <= 0) {
                    error = "Isi nama dan target yang benar"
                    return@Button
                }
                onSave(name.trim(), target)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
