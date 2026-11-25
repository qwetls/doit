package com.xeylabs.doit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.IncomeGreen

@Composable
fun TransactionRow(
    tx: Transaction,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    currency: Currency = Currency.IDR,
    walletName: String? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = tx.type == TxnType.INCOME.name
    val isTransfer = tx.type == TxnType.TRANSFER.name
    val category = Category.fromName(tx.category)
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIconTile(
            category = category,
            tint = when {
                isTransfer -> MaterialTheme.colorScheme.onSurfaceVariant
                isIncome -> IncomeGreen
                else -> ExpenseRed
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (tx.note.isNotBlank() && isTransfer) tx.note else category.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDate(tx.dateEpochDay) +
                    (if (walletName != null) " · " + walletName else "") +
                    (if (tx.note.isNotBlank() && !isTransfer) " · " + tx.note else ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = when {
                isTransfer -> formatMoney(tx.amount, currency)
                else -> formatSignedMoney(tx.amount, isIncome, currency)
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isTransfer -> MaterialTheme.colorScheme.onSurface
                isIncome -> IncomeGreen
                else -> ExpenseRed
            }
        )
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Ubah",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
