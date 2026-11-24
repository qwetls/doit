package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val month by viewModel.selectedMonth.collectAsState()
    val history by viewModel.historyList.collectAsState()
    val selectedCategory by viewModel.historyCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var editingTx by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        MonthStepper(
            month = month,
            onPrev = { viewModel.prevMonth() },
            onNext = { viewModel.nextMonth() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Cari catatan, kategori, jumlah...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setHistoryCategory(null) },
                    label = { Text("Semua") }
                )
            }
            items(Category.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setHistoryCategory(category) },
                    label = { Text(category.label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (history.isEmpty()) {
            if (searchQuery.isNotBlank()) {
                Text(
                    "Tidak ada yang cocok dengan pencarian \"$searchQuery\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                DoitCard {
                    Text(
                        "Tidak ada catatan di bulan ini.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        "Ketuk tombol + di bawah untuk mencatat pemasukan atau pengeluaran.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            DoitCard {
                LazyColumn {
                    items(history, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            onEdit = { editingTx = tx },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }

    val txToEdit = editingTx
    if (txToEdit != null) {
        EditTransactionDialog(
            tx = txToEdit,
            viewModel = viewModel,
            onDismiss = { editingTx = null }
        )
    }
}
