package com.xeylabs.doit.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.xeylabs.doit.ui.theme.ExpenseRed
import com.xeylabs.doit.ui.theme.HeroViolet
import com.xeylabs.doit.ui.theme.IncomeGreen
import com.xeylabs.doit.ui.theme.VioletAccent
import com.xeylabs.doit.ui.theme.WarningAmber
import com.xeylabs.doit.ui.theme.categoryColor
import com.xeylabs.doit.viewmodel.InsightKind
import com.xeylabs.doit.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, onOpenSavings: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val scrollState = rememberScrollState()
    var chartRange by remember { mutableStateOf(0) }

    val balance = state.monthIncome - state.monthExpense
    val defaultGoal = state.goals.firstOrNull { it.isDefault } ?: state.goals.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Halo, Adrien",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            MonthStepper(
                month = month,
                onPrev = { viewModel.prevMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }

        DoitCard(containerColor = HeroViolet) {
            Text(
                "Sisa bulan ini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedMoney(
                amount = balance,
                style = MaterialTheme.typography.displayLarge,
                color = if (balance >= 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroMini("Masuk", state.monthIncome, IncomeGreen)
                HeroMini("Keluar", state.monthExpense, ExpenseRed)
                HeroMini("Pemasukan/bln", state.monthlyIncome, MaterialTheme.colorScheme.onSurfaceVariant)
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
                BarChart(bars = state.last7Days.toBars())
            } else {
                BarChart(bars = state.monthlyBars.toBars())
            }
            Spacer(modifier = Modifier.height(8.dp))
            val chartTotal = if (chartRange == 0) {
                state.last7Days.sumOf { it.amount }
            } else {
                state.monthlyBars.sumOf { it.amount }
            }
            Text(
                text = "Total: " + formatRupiah(chartTotal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.categoryBreakdown.isNotEmpty()) {
            DoitCard {
                SectionTitle("Per kategori bulan ini")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DonutChart(
                        slices = state.categoryBreakdown.toDonutSlices(),
                        centerText = formatRupiah(state.monthExpense)
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
                                text = formatRupiah(item.amount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        DoitCard {
            SectionTitle("Anggaran bulan ini")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val savedThisMonth = (state.monthIncome - state.monthExpense).coerceAtLeast(0)
                BudgetRingItem(
                    label = "Belanja",
                    spent = state.belanjaSpent,
                    budget = state.budgetBelanja
                )
                BudgetRingItem(
                    label = "Darurat",
                    spent = state.daruratSpent,
                    budget = state.budgetDarurat
                )
                BudgetRingItem(
                    label = "Tabungan",
                    spent = savedThisMonth,
                    budget = state.budgetTabungan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val worstAlert = listOf(
                state.belanjaSpent to state.budgetBelanja,
                state.daruratSpent to state.budgetDarurat
            ).firstOrNull { (spent, budget) -> budget > 0 && spent >= budget }
            if (worstAlert != null) {
                Text(
                    text = "Ada anggaran yang sudah habis atau terlampaui — cek Riwayat untuk melihat penyebabnya.",
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
                    TextButton(onClick = onOpenSavings) { Text("Semua target") }
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

        Text("Terakhir dicatat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    state.recent.forEach { tx -> TransactionRow(tx = tx) }
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun HeroMini(label: String, amount: Long, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            formatRupiah(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BudgetRingItem(label: String, spent: Long, budget: Long) {
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
                remaining < 0 -> "Lebih " + formatRupiah(-remaining)
                ratio >= 0.8f && ratio < 1f -> "Hampir habis · sisa " + formatRupiah(remaining)
                else -> "Sisa " + formatRupiah(remaining)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
