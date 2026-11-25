package com.xeylabs.doit.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.HeroEmerald
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    updateViewModel: UpdateViewModel,
    onNavigate: (DoitTab) -> Unit,
    onQuickAdd: (TxnType) -> Unit,
    onShowHistory: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val scrollState = rememberScrollState()
    var balanceHidden by remember { mutableStateOf(false) }

    val defaultCurrency = Currency.fromCode(state.defaultCurrency)
    val balance = state.monthIncome - state.monthExpense
    val net = state.monthIncome - state.monthExpense
    val prevNet = state.prevMonthIncome - state.prevMonthExpense
    val netChangeText = if (prevNet == 0L) null else String.format(
        Locale.US, "%+.1f%%", (net - prevNet) * 100.0 / prevNet.toDouble()
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FluxHeader(subtitle = "Beranda", displayName = displayName) {
            onNavigate(DoitTab.AKUN)
        }

        UpdateCard(updateViewModel)

        DoitCard(containerColor = HeroEmerald) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Bulan sebelumnya",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Bulan Ini · " + formatMonth(month),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Bulan berikutnya",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = { balanceHidden = !balanceHidden }) {
                    Icon(
                        if (balanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (balanceHidden) "Tampilkan saldo" else "Sembunyikan saldo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Total Saldo Bersih",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (balanceHidden) {
                Text(
                    "••••••••",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
            } else {
                AnimatedMoney(
                    amount = state.totalBalance,
                    style = MaterialTheme.typography.displayLarge,
                    currency = defaultCurrency
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (netChangeText != null) {
                val up = net >= prevNet
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(
                        text = netChangeText + " vs bulan lalu",
                        color = if (up) IncomeGreen else ExpenseRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (up) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (up) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroMini("Masuk", state.monthIncome, IncomeGreen, defaultCurrency, balanceHidden)
                HeroMini("Keluar", state.monthExpense, ExpenseRed, defaultCurrency, balanceHidden)
                HeroMini("Sisa bulan ini", balance, MaterialTheme.colorScheme.onSurface, defaultCurrency, balanceHidden)
            }
            val otherCurrencies = state.totalBalanceByCurrency.keys.filter { it != state.defaultCurrency }
            otherCurrencies.forEach { code ->
                val currency = Currency.fromCode(code)
                val otherBalance = state.totalBalanceByCurrency[code] ?: 0L
                val otherIn = state.monthIncomeByCurrency[code] ?: 0L
                val otherOut = state.monthExpenseByCurrency[code] ?: 0L
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Saldo ${currency.symbol}: " + formatMoney(otherBalance, currency) +
                        " · masuk " + formatMoney(otherIn, currency) +
                        " · keluar " + formatMoney(otherOut, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onQuickAdd(TxnType.INCOME) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald,
                        contentColor = Color(0xFF003824)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pemasukan")
                }
                Button(
                    onClick = { onQuickAdd(TxnType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color(0xFF690005)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pengeluaran")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InOutCard(
                label = "Pemasukan",
                amount = state.monthIncome,
                prevAmount = state.prevMonthIncome,
                isIncome = true,
                currency = defaultCurrency,
                hidden = balanceHidden,
                modifier = Modifier.weight(1f)
            )
            InOutCard(
                label = "Pengeluaran",
                amount = state.monthExpense,
                prevAmount = state.prevMonthExpense,
                isIncome = false,
                currency = defaultCurrency,
                hidden = balanceHidden,
                modifier = Modifier.weight(1f)
            )
        }

        val netPoints = state.last7DaysNet.map { it.amount }
        val netLabels = state.last7DaysNet.map { it.label }
        if (netPoints.isNotEmpty()) {
            val peakIndex = netPoints.indexOf(netPoints.max())
            val peakValue = netPoints.max()
            DoitCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("Tren Arus Kas")
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusPill(text = "7 Hari", color = Emerald)
                    }
                    Text(
                        "Net Inflow",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (peakValue != 0L) {
                    val peakDate = LocalDate.now().minusDays((netPoints.size - 1 - peakIndex).toLong())
                    val badgeText = peakDate.format(
                        DateTimeFormatter.ofPattern("d MMM", Locale("in", "ID"))
                    ) + ": " + (if (peakValue >= 0) "+" else "") + formatCompact(peakValue)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(peakIndex.toFloat().coerceAtLeast(0.5f)))
                        StatusPill(text = badgeText, color = Emerald)
                        Spacer(modifier = Modifier.weight((netPoints.size - 1 - peakIndex).toFloat().coerceAtLeast(0.5f)))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                NetFlowChart(points = netPoints, labels = netLabels, peakIndex = peakIndex)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAction(
                icon = Icons.Filled.Send,
                label = "Transfer",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAdd(TxnType.TRANSFER) }
            )
            QuickAction(
                icon = Icons.Filled.Payments,
                label = "Tarik Kas",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAdd(TxnType.EXPENSE) }
            )
            QuickAction(
                icon = Icons.Filled.Category,
                label = "Atur Pos",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(DoitTab.ANGGARAN) }
            )
            QuickAction(
                icon = Icons.Filled.Download,
                label = "Ekspor",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.exportCsv() }
            )
        }

        val firstDue = state.dueBills.firstOrNull()
        if (firstDue != null) {
            val today = LocalDate.now()
            val daysLeft = ChronoUnit.DAYS.between(today, firstDue.dueDate)
            val statusText = when {
                firstDue.overdue -> "Terlambat ${-daysLeft} hari"
                daysLeft == 0L -> "Jatuh tempo hari ini"
                else -> "Jatuh tempo dalam $daysLeft hari"
            }
            val extra = if (state.dueBills.size > 1) " · +${state.dueBills.size - 1} lagi" else ""
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onNavigate(DoitTab.ANGGARAN) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (firstDue.overdue) ExpenseRed else WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = firstDue.bill.name + " · " + statusText + extra,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.markBillPaid(firstDue.bill) }) {
                    Text("Bayar")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Transaksi Terbaru")
                Spacer(modifier = Modifier.width(8.dp))
                StatusPill(text = "Bulan Ini", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onShowHistory) { Text("Saring") }
        }
        if (state.recent.isEmpty()) {
            DoitCard {
                Text(
                    "Belum ada catatan bulan ini.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Ketuk tombol + di bawah untuk mencatat pemasukan atau pengeluaran pertamamu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            DoitCard {
                Column {
                    state.recent.forEach { tx ->
                        TransactionRow(
                            tx = tx,
                            currency = viewModel.walletCurrency(tx.walletId),
                            walletName = viewModel.walletName(tx.walletId)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onShowHistory() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lihat Semua Transaksi",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun HeroMini(
    label: String,
    amount: Long,
    color: Color,
    currency: Currency,
    hidden: Boolean
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (hidden) "••••" else formatMoney(amount, currency),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InOutCard(
    label: String,
    amount: Long,
    prevAmount: Long,
    isIncome: Boolean,
    currency: Currency,
    hidden: Boolean,
    modifier: Modifier = Modifier
) {
    val changeText = if (prevAmount == 0L) null else String.format(
        Locale.US, "%+.1f%%", (amount - prevAmount) * 100.0 / prevAmount.toDouble()
    )
    val status = when {
        prevAmount == 0L -> if (amount == 0L) "belum ada" else "baru"
        isIncome -> if (amount >= prevAmount) "sehat" else "turun"
        else -> if (amount <= prevAmount) "terkendali" else "naik"
    }
    val color = if (isIncome) IncomeGreen else ExpenseRed
    DoitCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Filled.SouthWest else Icons.Filled.NorthEast,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (hidden) "••••" else formatMoney(amount, currency),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (changeText != null) {
                Text(
                    changeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(Emerald.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Emerald,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
