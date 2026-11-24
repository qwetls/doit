package com.xeylabs.doit.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xeylabs.doit.DoitTab
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.WalletType
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.HeroViolet
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.ui.theme.categoryColor
import com.xeylabs.doit.viewmodel.InsightKind
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
    var chartRange by remember { mutableStateOf(0) }

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
                // Profil paling kiri: avatar inisial, ketuk untuk buka Profil & Pengaturan.
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

        // Hero ala app bank: nominal saldo besar + tombol tambah cepat.
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
            // Mata uang lain (Dollar) tampil terpisah — total tidak pernah
            // dicampur antar mata uang karena tanpa kurs tidak ada angka benar.
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

        if (state.wallets.isNotEmpty()) {
            DoitCard(modifier = Modifier.clickable { onNavigate(DoitTab.DOMPET) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Dompet")
                    Text(
                        "Kelola ›",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VioletAccent
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total saldo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatMoney(state.totalBalance, defaultCurrency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                state.wallets.forEach { walletBalance ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            walletBalance.wallet.name + " · " +
                                WalletType.fromName(walletBalance.wallet.type).label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            formatMoney(
                                walletBalance.balance,
                                Currency.fromCode(walletBalance.wallet.currency)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Cash vs e-money (mata uang utama) — ketahuan cash berapa, e-money berapa.
                val cashDc = state.cashByCurrency[state.defaultCurrency] ?: 0L
                val emoneyDc = state.emoneyByCurrency[state.defaultCurrency] ?: 0L
                val bankDc = state.bankByCurrency[state.defaultCurrency] ?: 0L
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tunai " + formatMoney(cashDc, defaultCurrency) +
                        " · E-money " + formatMoney(emoneyDc, defaultCurrency) +
                        " · Bank " + formatMoney(bankDc, defaultCurrency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Perhatian: tagihan jatuh tempo ≤7 hari / terlambat + anggaran yang hampir/habis.
        // Per-sumber pemasukan TIDAK di sini — hanya di Laporan.
        val budgetAlerts = listOf(
            "Belanja" to (state.belanjaSpent to state.budgetBelanja),
            "Darurat" to (state.daruratSpent to state.budgetDarurat)
        ).mapNotNull { (label, spentToBudget) ->
            val (spent, budget) = spentToBudget
            if (budget <= 0) return@mapNotNull null
            val ratio = spent.toFloat() / budget.toFloat()
            when {
                ratio >= 1f -> "Anggaran $label sudah habis (lebih " + formatMoney(spent - budget, defaultCurrency) + ")"
                ratio >= 0.8f -> "Anggaran $label hampir habis — sisa " + formatMoney(budget - spent, defaultCurrency)
                else -> null
            }
        }
        if (state.dueBills.isNotEmpty() || budgetAlerts.isNotEmpty()) {
            DoitCard {
                SectionTitle("Perhatian")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.dueBills.forEach { due ->
                        val today = LocalDate.now()
                        val daysLeft = ChronoUnit.DAYS.between(today, due.dueDate)
                        val statusText = when {
                            due.overdue -> "Terlambat ${-daysLeft} hari"
                            daysLeft == 0L -> "Jatuh tempo hari ini"
                            else -> "Jatuh tempo dalam $daysLeft hari"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate(DoitTab.RENCANA) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    due.bill.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    formatMoney(due.bill.amount, defaultCurrency) + " · " + statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (due.overdue) ExpenseRed else WarningAmber
                                )
                            }
                            TextButton(onClick = { viewModel.markBillPaid(due.bill) }) {
                                Text("Bayar")
                            }
                        }
                    }
                    budgetAlerts.forEach { alertText ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate(DoitTab.RENCANA) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = alertText,
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
                }
            }
        }

        if (state.insights.isNotEmpty()) {
            DoitCard {
                SectionTitle("Insight")
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.insights.forEach { insight ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (icon, tint) = when (insight.kind) {
                                InsightKind.GOOD -> Icons.Filled.CheckCircle to IncomeGreen
                                InsightKind.WARN -> Icons.Filled.Warning to WarningAmber
                                InsightKind.INFO -> Icons.Filled.TrendingUp to VioletAccent
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = insight.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Pengeluaran")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chartRange == 0,
                        onClick = { chartRange = 0 },
                        label = { Text("7 hari") }
                    )
                    FilterChip(
                        selected = chartRange == 1,
                        onClick = { chartRange = 1 },
                        label = { Text("6 bulan") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (chartRange == 0) {
                BarChart(bars = state.last7Days.toDayBars())
            } else {
                BarChart(bars = state.monthlyBars.toMonthBars())
            }
            Spacer(modifier = Modifier.height(8.dp))
            val chartTotal = if (chartRange == 0) {
                state.last7Days.sumOf { it.amount }
            } else {
                state.monthlyBars.sumOf { it.amount }
            }
            Text(
                text = "Total: " + formatMoney(chartTotal, defaultCurrency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { onNavigate(DoitTab.LAPORAN) }) { Text("Lihat laporan ›") }
        }

        if (state.categoryBreakdown.isNotEmpty()) {
            DoitCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Per kategori bulan ini")
                    TextButton(onClick = { onNavigate(DoitTab.LAPORAN) }) { Text("Laporan ›") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DonutChart(
                        slices = state.categoryBreakdown.toDonutSlices(),
                        centerText = formatMoney(state.monthExpense, defaultCurrency)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    ) {
                        state.categoryBreakdown.forEach { item ->
                            val pct = if (state.monthExpense > 0) {
                                (item.amount * 100 / state.monthExpense).toInt()
                            } else 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(10.dp).clip(
                                        androidx.compose.foundation.shape.CircleShape
                                    ).background(categoryColor(item.category.name))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.category.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = formatMoney(item.amount, defaultCurrency),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        DoitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Anggaran bulan ini")
                TextButton(onClick = { onNavigate(DoitTab.RENCANA) }) { Text("Atur anggaran") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val savedThisMonth = (state.monthIncome - state.monthExpense).coerceAtLeast(0)
                BudgetRingItem(
                    label = "Belanja",
                    spent = state.belanjaSpent,
                    budget = state.budgetBelanja,
                    currency = defaultCurrency
                )
                BudgetRingItem(
                    label = "Darurat",
                    spent = state.daruratSpent,
                    budget = state.budgetDarurat,
                    currency = defaultCurrency
                )
                BudgetRingItem(
                    label = "Tabungan",
                    spent = savedThisMonth,
                    budget = state.budgetTabungan,
                    currency = defaultCurrency
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val worstAlert = listOf(
                state.belanjaSpent to state.budgetBelanja,
                state.daruratSpent to state.budgetDarurat
            ).firstOrNull { (spent, budget) -> budget > 0 && spent >= budget }
            if (worstAlert != null) {
                Text(
                    text = "Ada anggaran yang sudah habis atau terlampaui — cek Rencana untuk melihat penyebabnya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpenseRed
                )
            }
        }

        if (defaultGoal != null) {
            DoitCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Tabungan ${defaultGoal.name}")
                    TextButton(onClick = { onNavigate(DoitTab.RENCANA) }) { Text("Semua target") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progress = if (defaultGoal.target > 0) {
                        (defaultGoal.saved.toFloat() / defaultGoal.target.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    BudgetRing(
                        progress = progress,
                        color = VioletAccent,
                        centerText = "${(progress * 100).toInt()}%",
                        size = 96.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AnimatedMoney(
                            amount = defaultGoal.saved,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "dari " + formatRupiah(defaultGoal.target),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val projectedDate = state.projectedDate
                        val estimateText = when {
                            state.monthsToTarget == 0L -> "Target tercapai!"
                            state.monthsToTarget != null && projectedDate != null ->
                                "Perkiraan tercapai sekitar " + projectedDate.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                        "MMM yyyy",
                                        java.util.Locale("in", "ID")
                                    )
                                )
                            else -> "Belum bisa diperkirakan"
                        }
                        Text(
                            estimateText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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

@Composable
private fun BudgetRingItem(label: String, spent: Long, budget: Long, currency: Currency) {
    val ratio = if (budget > 0) spent.toFloat() / budget.toFloat() else 0f
    val color = when {
        ratio >= 1f -> ExpenseRed
        ratio >= 0.8f -> WarningAmber
        else -> VioletAccent
    }
    val pct = (ratio.coerceIn(0f, 1f) * 100).toInt()
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BudgetRing(progress = ratio, color = color, centerText = "$pct%")
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        val remaining = budget - spent
        Text(
            text = when {
                remaining < 0 -> "Lebih " + formatMoney(-remaining, currency)
                ratio >= 0.8f && ratio < 1f -> "Hampir habis · sisa " + formatMoney(remaining, currency)
                else -> "Sisa " + formatMoney(remaining, currency)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
