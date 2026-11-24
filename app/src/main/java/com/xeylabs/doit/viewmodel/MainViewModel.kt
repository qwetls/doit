package com.xeylabs.doit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xeylabs.doit.data.AppDatabase
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.SettingsStore
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.data.TxnType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

data class HomeUiState(
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val belanjaSpent: Long = 0,
    val daruratSpent: Long = 0,
    val budgetBelanja: Long = 200_000L,
    val budgetDarurat: Long = 100_000L,
    val budgetTabungan: Long = 700_000L,
    val savingsTarget: Long = 3_000_000L,
    val saved: Long = 0,
    val avgMonthlyNet: Long = 0,
    val monthsToTarget: Long? = null,
    val recent: List<Transaction> = emptyList()
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).transactionDao()
    private val settings = SettingsStore(app)

    val selectedMonth = MutableStateFlow(YearMonth.now())
    val historyCategory = MutableStateFlow<Category?>(null)

    private val allTransactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class HomeInputs(
        val txs: List<Transaction>,
        val month: YearMonth,
        val budgetBelanja: Long,
        val budgetDarurat: Long,
        val budgetTabungan: Long
    )

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            allTransactions,
            selectedMonth,
            settings.budgetBelanja,
            settings.budgetDarurat,
            settings.budgetTabungan
        ) { txs, month, bBelanja, bDarurat, bTabungan ->
            HomeInputs(txs, month, bBelanja, bDarurat, bTabungan)
        },
        settings.savingsTarget
    ) { inputs, target ->
        val txs = inputs.txs
        val month = inputs.month
        val bBelanja = inputs.budgetBelanja
        val bDarurat = inputs.budgetDarurat
        val bTabungan = inputs.budgetTabungan
        val inMonth = txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
        val monthIncome = inMonth.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount }
        val monthExpense = inMonth.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
        val belanjaSpent = inMonth.filter { it.category == Category.BELANJA.name }.sumOf { it.amount }
        val daruratSpent = inMonth.filter { it.category == Category.DARURAT.name }.sumOf { it.amount }

        val totalIncome = txs.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount }
        val totalExpense = txs.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
        val saved = (totalIncome - totalExpense).coerceAtLeast(0)

        val monthlyNets = txs.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }
            .values.map { monthTxs ->
                monthTxs.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount } -
                    monthTxs.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
            }
        val avgMonthlyNet = if (monthlyNets.isEmpty()) 0 else monthlyNets.sum() / monthlyNets.size
        val remaining = (target - saved).coerceAtLeast(0)
        val monthsToTarget = when {
            remaining <= 0 -> 0L
            avgMonthlyNet > 0 -> ceil(remaining.toDouble() / avgMonthlyNet.toDouble()).toLong()
            else -> null
        }

        HomeUiState(
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            belanjaSpent = belanjaSpent,
            daruratSpent = daruratSpent,
            budgetBelanja = bBelanja,
            budgetDarurat = bDarurat,
            budgetTabungan = bTabungan,
            savingsTarget = target,
            saved = saved,
            avgMonthlyNet = avgMonthlyNet,
            monthsToTarget = monthsToTarget,
            recent = txs.take(5)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val historyList: StateFlow<List<Transaction>> = combine(
        allTransactions, selectedMonth, historyCategory
    ) { txs, month, category ->
        txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
            .filter { category == null || it.category == category.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(type: TxnType, category: Category, amount: Long, date: LocalDate, note: String) {
        viewModelScope.launch {
            dao.insert(
                Transaction(
                    type = type.name,
                    category = category.name,
                    amount = amount,
                    dateEpochDay = date.toEpochDay(),
                    note = note.trim()
                )
            )
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { dao.delete(tx) }
    }

    fun prevMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        val next = selectedMonth.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now())) selectedMonth.value = next
    }

    fun setHistoryCategory(category: Category?) {
        historyCategory.value = category
    }

    fun setBudgetBelanja(value: Long) = settings.setBudgetBelanja(value)
    fun setBudgetDarurat(value: Long) = settings.setBudgetDarurat(value)
    fun setBudgetTabungan(value: Long) = settings.setBudgetTabungan(value)
    fun setSavingsTarget(value: Long) = settings.setSavingsTarget(value)
}
