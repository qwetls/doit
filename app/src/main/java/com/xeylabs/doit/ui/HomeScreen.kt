package com.xeylabs.doit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.HeroViolet
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    updateViewModel: UpdateViewModel,
    onNavigate: (DoitTab) -> Unit,
    onOpenProfile: () -> Unit,
    onQuickAdd: (TxnType) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val scrollState = rememberScrollState()

    val defaultCurrency = Currency.fromCode(state.defaultCurrency)
    val balance = state.monthIncome - state.monthExpense
    val defaultGoal = state.goals.firstOrNull { it.isDefault } ?: state.goals.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UpdateCard(updateViewModel)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(HeroViolet)
                ) {
                    Text(
                        text = displayName.trim().firstOrNull()?.uppercase() ?: "A",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Halo, $displayName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            MonthStepper(
                month = month,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }

        DoitCard(containerColor = HeroViolet) {
            Text(
                "Total saldo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedMoney(
                amount = state.totalBalance,
                style = MaterialTheme.typography.displayLarge,
                currency = defaultCurrency
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroMini("Masuk", state.monthIncome, IncomeGreen, defaultCurrency)
                HeroMini("Keluar", state.monthExpense, ExpenseRed, defaultCurrency)
                HeroMini("Sisa bulan ini", balance, MaterialTheme.colorScheme.onSurface, defaultCurrency)
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
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pemasukan")
                }
                Button(
                    onClick = { onQuickAdd(TxnType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pengeluaran")
                }
            }
        }

        val nearestDue = state.dueBills.firstOrNull()
        if (nearestDue != null) {
            val today = LocalDate.now()
            val daysLeft = ChronoUnit.DAYS.between(today, nearestDue.dueDate)
            val statusText = when {
                nearestDue.overdue -> "Terlambat ${-daysLeft} hari"
                daysLeft == 0L -> "Jatuh tempo hari ini"
                else -> "Jatuh tempo dalam $daysLeft hari"
            }
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(DoitTab.RENCANA) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (nearestDue.overdue) ExpenseRed else WarningAmber,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = nearestDue.bill.name + " · " + statusText +
                        if (state.dueBills.size > 1) " · +${state.dueBills.size - 1} lagi" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Rencana ›",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VioletAccent
                )
            }
        }

        if (defaultGoal != null) {
            val progress = if (defaultGoal.target > 0) {
                (defaultGoal.saved.toFloat() / defaultGoal.target.toFloat()).coerceIn(0f, 1f)
            } else 0f
            Column(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(DoitTab.RENCANA) },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tabungan ${defaultGoal.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = VioletAccent
                    )
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = VioletAccent,
                    trackColor = Color.White.copy(alpha = 0.10f)
                )
                Text(
                    formatRupiah(defaultGoal.saved) + " dari " + formatRupiah(defaultGoal.target),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Terakhir dicatat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onNavigate(DoitTab.RIWAYAT) }) { Text("Lihat semua") }
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
                            currency = viewModel.walletCurrency(tx.walletId)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun HeroMini(label: String, amount: Long, color: Color, currency: Currency) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            formatMoney(amount, currency),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
