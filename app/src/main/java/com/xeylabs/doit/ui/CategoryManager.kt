package com.xeylabs.doit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.CUSTOM_CATEGORY_COLOR_HEXES
import com.xeylabs.doit.data.CUSTOM_CATEGORY_ICON_KEYS
import com.xeylabs.doit.data.CustomCategory
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.customCategoryKey
import com.xeylabs.doit.data.resolveCategoryRef
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun CategoryManagerSection(viewModel: MainViewModel) {
    val customs by viewModel.customCategories.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomCategory?>(null) }
    var deleting by remember { mutableStateOf<CustomCategory?>(null) }

    DoitCard {
        SectionTitle("Kategori Saya")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Kategori buatanmu muncul di Catat Transaksi, laporan, dan filter riwayat — sama seperti kategori bawaan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (customs.isEmpty()) {
            Text(
                "Belum ada kategori kustom.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            customs.forEach { custom ->
                val ref = resolveCategoryRef(customCategoryKey(custom.id), customs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        CategoryIconTile(ref = ref, size = 40)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                custom.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (custom.type == TxnType.INCOME.name) "Pemasukan" else "Pengeluaran",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { editing = custom }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah kategori", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { deleting = custom }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus kategori", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah kategori")
        }
    }

    if (showAdd) {
        CustomCategoryEditDialog(
            initial = null,
            fixedType = null,
            onDismiss = { showAdd = false },
            onSave = { name, type, iconKey, colorHex ->
                viewModel.addCustomCategory(name, type, iconKey, colorHex)
                showAdd = false
            }
        )
    }
    val toEdit = editing
    if (toEdit != null) {
        CustomCategoryEditDialog(
            initial = toEdit,
            fixedType = null,
            onDismiss = { editing = null },
            onSave = { name, type, iconKey, colorHex ->
                viewModel.updateCustomCategory(
                    toEdit.copy(name = name, type = type.name, iconKey = iconKey, colorHex = colorHex)
                )
                editing = null
            }
        )
    }
    val toDelete = deleting
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Hapus kategori \"${toDelete.name}\"?") },
            text = { Text("Transaksi, jadwal rutin, dan pos anggaran yang memakai kategori ini akan dipindah ke \"Lain-lain\".") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomCategory(toDelete)
                        deleting = null
                    }
                ) { Text("Hapus", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCategoryEditDialog(
    initial: CustomCategory?,
    fixedType: TxnType?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TxnType, iconKey: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember {
        mutableStateOf(
            initial?.let { runCatching { TxnType.valueOf(it.type) }.getOrDefault(TxnType.EXPENSE) }
                ?: fixedType ?: TxnType.EXPENSE
        )
    }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: "category") }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: CUSTOM_CATEGORY_COLOR_HEXES.first()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Kategori baru" else "Ubah kategori") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (fixedType == null) {
                    Text("Jenis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = type == TxnType.EXPENSE,
                            onClick = { type = TxnType.EXPENSE },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = { Text("Pengeluaran") }
                        )
                        SegmentedButton(
                            selected = type == TxnType.INCOME,
                            onClick = { type = TxnType.INCOME },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = { Text("Pemasukan") }
                        )
                    }
                }
                Text("Ikon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CUSTOM_CATEGORY_ICON_KEYS, key = { it }) { key ->
                        FilterChip(
                            selected = iconKey == key,
                            onClick = { iconKey = key },
                            label = {
                                Icon(
                                    customCategoryIcon(key),
                                    contentDescription = key,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                Text("Warna", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CUSTOM_CATEGORY_COLOR_HEXES, key = { it }) { hex ->
                        FilterChip(
                            selected = colorHex == hex,
                            onClick = { colorHex = hex },
                            label = {
                                Box(
                                    modifier = Modifier.size(18.dp).clip(CircleShape)
                                        .background(Color(hex))
                                )
                            }
                        )
                    }
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    error = "Isi nama kategori"
                    return@Button
                }
                onSave(name.trim(), type, iconKey, colorHex)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
