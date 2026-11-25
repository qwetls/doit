package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.categoryRefsForType
import com.xeylabs.doit.data.resolveCategoryRef
import com.xeylabs.doit.viewmodel.MainViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    tx: Transaction,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val customCategories by viewModel.customCategories.collectAsState()
    val type = TxnType.valueOf(tx.type)
    val txCurrency = viewModel.walletCurrency(tx.walletId)
    var amountText by remember { mutableStateOf(tx.amount.toString()) }
    var category by remember { mutableStateOf(resolveCategoryRef(tx.category, customCategories)) }
    var date by remember { mutableStateOf(LocalDate.ofEpochDay(tx.dateEpochDay)) }
    var note by remember { mutableStateOf(tx.note) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val categories = categoryRefsForType(type, customCategories)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TxnType.INCOME) "Ubah pemasukan" else "Ubah pengeluaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah (" + txCurrency.symbol + ")") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    category = option
                                    expanded = false
                                }
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
                    modifier = Modifier.fillMaxWidth()
                )

                if (tx.receiptPath != null) {
                    Text(
                        "Struk",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    ReceiptImage(
                        relativePath = tx.receiptPath,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
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
                viewModel.updateTransaction(
                    tx.copy(
                        category = category.key,
                        amount = amount,
                        dateEpochDay = date.toEpochDay(),
                        note = note.trim()
                    )
                )
                onDismiss()
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
