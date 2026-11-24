package com.xeylabs.doit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xeylabs.doit.data.AppDatabase
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.SavingsGoal
import com.xeylabs.doit.data.SettingsStore
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.widget.DoitWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

data class DaySpend(val label: String, val amount: Long)
data class MonthSpend(val label: String, val amount: Long)
data class CategorySpend(val category: Category, val amount: Long)

enum class InsightKind { GOOD, WARN, INFO }
data class Insight(val text: String, val kind: InsightKind)

data class HomeUiState(
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val belanjaSpent: Long = 0,
    val daruratSpent: Long = 0,
    val makanSpent: Long = 0,
    val transportSpent: Long = 0,
    val lainSpent: Long = 0,
    val budgetBelanja: Long = 200_000L,
    val budgetDarurat: Long = 100_000L,
    val budgetTabungan: Long = 700_000L,
    val monthlyIncome: Long = 1_000_000L,
    val goals: List<SavingsGoal> = emptyList(),
    val avgMonthlyNet: Long = 0,
    val monthsToTarget: Long? = null,
    val projectedDate: LocalDate? = null,
    val last7Days: List<DaySpend> = emptyList(),
    val monthlyBars: List<MonthSpend> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val prevMonthExpense: Long = 0,
    val prevMonthIncome: Long = 0,
    val insights: List<Insight> = emptyList(),
    val recent: List<Transaction> = emptyList()
)

private val indonesia = Locale("in", "ID")
private val dayAbbrev = DateTimeFormatter.ofPattern("EEE", indonesia)
private val monthAbbrev = DateTimeFormatter.ofPattern("MMM", indonesia)
private val monthYear = DateTimeFormatter.ofPattern("MMM yyyy", indonesia)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val dao = db.transactionDao()
    private val goalDao = db.savingsGoalDao()
    private val settings = SettingsStore(app)

    val selectedMonth = MutableStateFlow(YearMonth.now())
    val historyCategory = MutableStateFlow<Category?>(null)
    val searchQuery = MutableStateFlow("")

    val onboardingDone: StateFlow<Boolean> = settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lastCategory: StateFlow<String?> = settings.lastCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allTransactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val goals: StateFlow<List<SavingsGoal>> = goalDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCategories: StateFlow<List<Category>> = allTransactions
        .map { txs ->
            txs.filter { it.type == TxnType.EXPENSE.name }
                .groupingBy { Category.fromName(it.category) }
                .eachCount()
                .entries.sortedByDescending { it.value }
                .map { it.key }
                .take(4)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (goalDao.getAllOnce().isEmpty()) {
                goalDao.insert(
                    SavingsGoal(
                        name = "Laptop",
                        target = settings.savingsTarget.value,
                        saved = 0,
                        isDefault = true
                    )
                )
            }
        }
    }

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
        goals,
        settings.monthlyIncome
    ) { inputs, goals, monthlyIncome ->
        buildHomeState(inputs, goals, monthlyIncome)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun buildHomeState(
        inputs: HomeInputs,
        goals: List<SavingsGoal>,
        monthlyIncome: Long
    ): HomeUiState {
        val txs = inputs.txs
        val month = inputs.month
        val today = LocalDate.now()
        val inMonth = txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
        val prevMonth = month.minusMonths(1)
        val inPrevMonth = txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == prevMonth }

        fun expenseOf(list: List<Transaction>) =
            list.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
        fun incomeOf(list: List<Transaction>) =
            list.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount }
        fun categorySpent(list: List<Transaction>, category: Category) =
            list.filter { it.category == category.name }.sumOf { it.amount }

        val monthIncome = incomeOf(inMonth)
        val monthExpense = expenseOf(inMonth)
        val prevMonthExpense = expenseOf(inPrevMonth)
        val prevMonthIncome = incomeOf(inPrevMonth)

        val monthlyNets = txs.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }
            .values.map { monthTxs -> incomeOf(monthTxs) - expenseOf(monthTxs) }
        val avgMonthlyNet = if (monthlyNets.isEmpty()) 0 else monthlyNets.sum() / monthlyNets.size

        val defaultGoal = goals.firstOrNull { it.isDefault } ?: goals.firstOrNull()
        val monthsToTarget: Long?
        val projectedDate: LocalDate?
        if (defaultGoal == null) {
            monthsToTarget = null
            projectedDate = null
        } else {
            val remaining = (defaultGoal.target - defaultGoal.saved).coerceAtLeast(0)
            monthsToTarget = when {
                remaining <= 0 -> 0L
                avgMonthlyNet > 0 -> ceil(remaining.toDouble() / avgMonthlyNet.toDouble()).toLong()
                else -> null
            }
            projectedDate = monthsToTarget?.let { today.plusMonths(it) }
        }

        val last7Days = (6 downTo 0).map { back ->
            val date = today.minusDays(back.toLong())
            val amount = txs.filter {
                it.type == TxnType.EXPENSE.name && LocalDate.ofEpochDay(it.dateEpochDay) == date
            }.sumOf { it.amount }
            DaySpend(date.format(dayAbbrev), amount)
        }

        val monthlyBars = (5 downTo 0).map { back ->
            val ym = YearMonth.now().minusMonths(back.toLong())
            val amount = txs.filter {
                it.type == TxnType.EXPENSE.name &&
                    YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == ym
            }.sumOf { it.amount }
            MonthSpend(ym.atDay(1).format(monthAbbrev), amount)
        }

        val categoryBreakdown = Category.forType(TxnType.EXPENSE)
            .map { category -> CategorySpend(category, categorySpent(inMonth, category)) }
            .filter { it.amount > 0 }
            .sortedByDescending { it.amount }

        val insights = buildInsights(
            txs = txs,
            month = month,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            prevMonthExpense = prevMonthExpense,
            categoryBreakdown = categoryBreakdown,
            belanjaSpent = categorySpent(inMonth, Category.BELANJA),
            daruratSpent = categorySpent(inMonth, Category.DARURAT),
            budgetBelanja = inputs.budgetBelanja,
            budgetDarurat = inputs.budgetDarurat,
            defaultGoal = defaultGoal,
            projectedDate = projectedDate
        )

        return HomeUiState(
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            belanjaSpent = categorySpent(inMonth, Category.BELANJA),
            daruratSpent = categorySpent(inMonth, Category.DARURAT),
            makanSpent = categorySpent(inMonth, Category.MAKAN),
            transportSpent = categorySpent(inMonth, Category.TRANSPORT),
            lainSpent = categorySpent(inMonth, Category.LAIN_KELUAR),
            budgetBelanja = inputs.budgetBelanja,
            budgetDarurat = inputs.budgetDarurat,
            budgetTabungan = inputs.budgetTabungan,
            monthlyIncome = monthlyIncome,
            goals = goals,
            avgMonthlyNet = avgMonthlyNet,
            monthsToTarget = monthsToTarget,
            projectedDate = projectedDate,
            last7Days = last7Days,
            monthlyBars = monthlyBars,
            categoryBreakdown = categoryBreakdown,
            prevMonthExpense = prevMonthExpense,
            prevMonthIncome = prevMonthIncome,
            insights = insights,
            recent = txs.take(5)
        )
    }

    private fun buildInsights(
        txs: List<Transaction>,
        month: YearMonth,
        monthIncome: Long,
        monthExpense: Long,
        prevMonthExpense: Long,
        categoryBreakdown: List<CategorySpend>,
        belanjaSpent: Long,
        daruratSpent: Long,
        budgetBelanja: Long,
        budgetDarurat: Long,
        defaultGoal: SavingsGoal?,
        projectedDate: LocalDate?
    ): List<Insight> {
        if (txs.isEmpty()) {
            return listOf(Insight("Belum ada catatan — ketuk + untuk mulai mencatat.", InsightKind.INFO))
        }
        val out = mutableListOf<Insight>()

        if (prevMonthExpense > 0) {
            val diffPct = ((monthExpense - prevMonthExpense) * 100.0 / prevMonthExpense).toInt()
            when {
                diffPct > 0 -> out += Insight(
                    "Pengeluaran naik $diffPct% dibanding bulan lalu.",
                    InsightKind.WARN
                )
                diffPct < 0 -> out += Insight(
                    "Pengeluaran turun ${-diffPct}% dibanding bulan lalu — bagus!",
                    InsightKind.GOOD
                )
                else -> out += Insight("Pengeluaran sama seperti bulan lalu.", InsightKind.INFO)
            }
        }

        val top = categoryBreakdown.firstOrNull()
        if (top != null && monthExpense > 0) {
            val pct = (top.amount * 100 / monthExpense).toInt()
            out += Insight(
                "Kategori terbesar: ${top.category.label} · $pct% dari pengeluaran bulan ini.",
                InsightKind.INFO
            )
        }

        if (month == YearMonth.now()) {
            val dayOfMonth = LocalDate.now().dayOfMonth
            val totalBudget = budgetBelanja + budgetDarurat
            val spent = belanjaSpent + daruratSpent
            val remaining = totalBudget - spent
            val dailyBurn = if (dayOfMonth > 0) spent / dayOfMonth else 0
            if (remaining > 0 && dailyBurn > 0) {
                val daysLeft = (remaining / dailyBurn).toInt()
                out += Insight(
                    "Anggaran belanja + darurat cukup untuk sekitar $daysLeft hari lagi.",
                    if (daysLeft < 7) InsightKind.WARN else InsightKind.INFO
                )
            } else if (remaining <= 0 && totalBudget > 0) {
                out += Insight(
                    "Anggaran belanja + darurat bulan ini sudah habis.",
                    InsightKind.WARN
                )
            }
        }

        if (defaultGoal != null) {
            if (defaultGoal.saved >= defaultGoal.target) {
                out += Insight(
                    "Target ${defaultGoal.name} tercapai — selamat!",
                    InsightKind.GOOD
                )
            } else if (projectedDate != null) {
                out += Insight(
                    "${defaultGoal.name} tercapai sekitar ${projectedDate.format(monthYear)}.",
                    InsightKind.INFO
                )
            } else if (monthIncome == 0L && monthExpense == 0L) {
                out += Insight(
                    "Catat pemasukan rutin supaya proyeksi tabungan bisa dihitung.",
                    InsightKind.INFO
                )
            }
        }

        return out.take(4)
    }

    val historyList: StateFlow<List<Transaction>> = combine(
        allTransactions, selectedMonth, historyCategory, searchQuery
    ) { txs, month, category, query ->
        val q = query.trim()
        txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
            .filter { category == null || it.category == category.name }
            .filter {
                q.isEmpty() ||
                    it.note.contains(q, ignoreCase = true) ||
                    Category.fromName(it.category).label.contains(q, ignoreCase = true) ||
                    it.amount.toString().contains(q)
            }
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
            settings.setLastCategory(category.name)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            dao.update(tx)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            dao.delete(tx)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun addGoal(name: String, target: Long) {
        viewModelScope.launch {
            goalDao.insert(SavingsGoal(name = name.trim(), target = target, saved = 0))
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun addToGoal(goal: SavingsGoal, amount: Long) {
        viewModelScope.launch {
            goalDao.update(goal.copy(saved = (goal.saved + amount).coerceAtLeast(0)))
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            goalDao.update(goal)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        if (goal.isDefault) return
        viewModelScope.launch {
            goalDao.delete(goal)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    fun completeOnboarding(
        income: Long,
        budgetBelanja: Long,
        budgetDarurat: Long,
        budgetTabungan: Long,
        target: Long
    ) {
        viewModelScope.launch {
            settings.setMonthlyIncome(income)
            settings.setBudgetBelanja(budgetBelanja)
            settings.setBudgetDarurat(budgetDarurat)
            settings.setBudgetTabungan(budgetTabungan)
            settings.setSavingsTarget(target)
            val existing = goalDao.getAllOnce()
            val defaultGoal = existing.firstOrNull { it.isDefault }
            if (defaultGoal != null) {
                goalDao.update(defaultGoal.copy(target = target))
            } else if (existing.isEmpty()) {
                goalDao.insert(SavingsGoal(name = "Laptop", target = target, saved = 0, isDefault = true))
            }
            settings.setOnboardingDone(true)
        }
    }

    fun skipOnboarding() {
        settings.setOnboardingDone(true)
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

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setMonthlyIncome(value: Long) = settings.setMonthlyIncome(value)
    fun setBudgetBelanja(value: Long) = settings.setBudgetBelanja(value)
    fun setBudgetDarurat(value: Long) = settings.setBudgetDarurat(value)
    fun setBudgetTabungan(value: Long) = settings.setBudgetTabungan(value)
}
