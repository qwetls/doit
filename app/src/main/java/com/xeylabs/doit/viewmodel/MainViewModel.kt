package com.xeylabs.doit.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xeylabs.doit.data.AppDatabase
import com.xeylabs.doit.data.Bill
import com.xeylabs.doit.data.Category
import com.xeylabs.doit.data.Debt
import com.xeylabs.doit.data.DebtDirection
import com.xeylabs.doit.data.RecurringFrequency
import com.xeylabs.doit.data.RecurringTransaction
import com.xeylabs.doit.data.SavingsGoal
import com.xeylabs.doit.data.SettingsStore
import com.xeylabs.doit.data.Transaction
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.data.Wallet
import com.xeylabs.doit.data.WalletType
import com.xeylabs.doit.widget.DoitWidgetProvider
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

data class DaySpend(val label: String, val amount: Long)
data class MonthSpend(val label: String, val amount: Long)
data class CategorySpend(val category: Category, val amount: Long)
data class WalletBalance(val wallet: Wallet, val balance: Long)
data class IncomeSourceSpend(val category: Category, val amount: Long)
data class WalletMonthSpend(val wallet: Wallet, val income: Long, val expense: Long)
data class BillDue(val bill: Bill, val dueDate: LocalDate, val overdue: Boolean)

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
    val recent: List<Transaction> = emptyList(),
    val wallets: List<WalletBalance> = emptyList(),
    val totalBalance: Long = 0,
    val incomeBySource: List<IncomeSourceSpend> = emptyList(),
    val dueBills: List<BillDue> = emptyList()
)

data class ReportUiState(
    val monthIncome: Long = 0,
    val monthExpense: Long = 0,
    val monthNet: Long = 0,
    val perCategory: List<CategorySpend> = emptyList(),
    val perWallet: List<WalletMonthSpend> = emptyList(),
    val perSource: List<IncomeSourceSpend> = emptyList()
)

private val indonesia = Locale("in", "ID")
private val dayAbbrev = DateTimeFormatter.ofPattern("EEE", indonesia)
private val monthAbbrev = DateTimeFormatter.ofPattern("MMM", indonesia)
private val monthYear = DateTimeFormatter.ofPattern("MMM yyyy", indonesia)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val dao = db.transactionDao()
    private val goalDao = db.savingsGoalDao()
    private val walletDao = db.walletDao()
    private val recurringDao = db.recurringDao()
    private val debtDao = db.debtDao()
    private val billDao = db.billDao()
    private val settings = SettingsStore(app)

    val selectedMonth = MutableStateFlow(YearMonth.now())
    val historyCategory = MutableStateFlow<Category?>(null)
    val searchQuery = MutableStateFlow("")

    val onboardingDone: StateFlow<Boolean> = settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lastCategory: StateFlow<String?> = settings.lastCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val lastWalletId: StateFlow<Long> = settings.lastWalletId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val allTransactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val goals: StateFlow<List<SavingsGoal>> = goalDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<Wallet>> = walletDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurrings: StateFlow<List<RecurringTransaction>> = recurringDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<Debt>> = debtDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<Bill>> = billDao.getAll()
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

    /** Saldo tiap dompet = saldo awal + pemasukan − pengeluaran − transfer keluar + transfer masuk. */
    val walletBalances: StateFlow<List<WalletBalance>> = combine(
        wallets, allTransactions
    ) { walletList, txs ->
        computeWalletBalances(walletList, txs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Pemasukan per sumber (Gaji pokok / Lembur / Sampingan / Lain-lain) di bulan terpilih. */
    val incomeBySource: StateFlow<List<IncomeSourceSpend>> = combine(
        allTransactions, selectedMonth
    ) { txs, month ->
        computeIncomeBySource(txs, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportState: StateFlow<ReportUiState> = combine(
        allTransactions, selectedMonth, wallets
    ) { txs, month, walletList ->
        val inMonth = txs.filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
        val income = inMonth.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount }
        val expense = inMonth.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
        val defaultId = walletList.firstOrNull()?.id
        ReportUiState(
            monthIncome = income,
            monthExpense = expense,
            monthNet = income - expense,
            perCategory = Category.forType(TxnType.EXPENSE)
                .map { category ->
                    CategorySpend(
                        category,
                        inMonth.filter { it.category == category.name }.sumOf { it.amount }
                    )
                }
                .filter { it.amount > 0 }
                .sortedByDescending { it.amount },
            perWallet = walletList.map { wallet ->
                val walletTxs = inMonth.filter { (it.walletId ?: defaultId) == wallet.id }
                val transferIn = inMonth
                    .filter { it.type == TxnType.TRANSFER.name && it.transferToWalletId == wallet.id }
                    .sumOf { it.amount }
                val transferOut = inMonth
                    .filter { it.type == TxnType.TRANSFER.name && (it.walletId ?: defaultId) == wallet.id }
                    .sumOf { it.amount }
                WalletMonthSpend(
                    wallet = wallet,
                    income = walletTxs.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount } + transferIn,
                    expense = walletTxs.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount } + transferOut
                )
            },
            perSource = computeIncomeBySource(txs, month)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

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
            // Seed dompet default (fresh install; upgrader sudah di-seed oleh migrasi).
            if (walletDao.getAllOnce().isEmpty()) {
                walletDao.insert(Wallet(name = "Tunai", type = WalletType.TUNAI.name, initialBalance = 0))
                walletDao.insert(Wallet(name = "SeaBank", type = WalletType.BANK.name, initialBalance = 0))
            }
            evaluateRecurrings()
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
        settings.monthlyIncome,
        wallets,
        bills
    ) { inputs, goals, monthlyIncome, walletList, billList ->
        buildHomeState(inputs, goals, monthlyIncome, walletList, billList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun computeWalletBalances(
        walletList: List<Wallet>,
        txs: List<Transaction>
    ): List<WalletBalance> {
        val defaultId = walletList.firstOrNull()?.id
        return walletList.map { wallet ->
            var balance = wallet.initialBalance
            txs.forEach { tx ->
                val effectiveWallet = tx.walletId ?: defaultId
                when (tx.type) {
                    TxnType.INCOME.name -> if (effectiveWallet == wallet.id) balance += tx.amount
                    TxnType.EXPENSE.name -> if (effectiveWallet == wallet.id) balance -= tx.amount
                    TxnType.TRANSFER.name -> {
                        if (effectiveWallet == wallet.id) balance -= tx.amount
                        if (tx.transferToWalletId == wallet.id) balance += tx.amount
                    }
                }
            }
            WalletBalance(wallet, balance)
        }
    }

    private fun computeIncomeBySource(
        txs: List<Transaction>,
        month: YearMonth
    ): List<IncomeSourceSpend> {
        return txs.filter {
            it.type == TxnType.INCOME.name &&
                YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month
        }
            .groupBy { Category.fromName(it.category) }
            .map { (category, list) -> IncomeSourceSpend(category, list.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
    }

    private fun computeDueBills(billList: List<Bill>): List<BillDue> {
        val today = LocalDate.now()
        val currentMonthKey = YearMonth.now().toString()
        val ym = YearMonth.now()
        return billList.mapNotNull { bill ->
            if (bill.paidMonth == currentMonthKey) return@mapNotNull null
            val dueDate = ym.atDay(bill.dueDayOfMonth.coerceIn(1, ym.lengthOfMonth()))
            val overdue = dueDate.isBefore(today)
            val dueSoon = !overdue && !dueDate.isAfter(today.plusDays(7))
            if (!overdue && !dueSoon) return@mapNotNull null
            BillDue(bill, dueDate, overdue)
        }.sortedBy { it.dueDate }
    }

    private fun buildHomeState(
        inputs: HomeInputs,
        goals: List<SavingsGoal>,
        monthlyIncome: Long,
        walletList: List<Wallet>,
        billList: List<Bill>
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

        val walletBalanceList = computeWalletBalances(walletList, txs)

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
            recent = txs.take(5),
            wallets = walletBalanceList,
            totalBalance = walletBalanceList.sumOf { it.balance },
            incomeBySource = computeIncomeBySource(txs, month),
            dueBills = computeDueBills(billList)
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

    // ---------- Transaksi berulang: dievaluasi tiap kali aplikasi dibuka ----------

    private fun nextDueDate(recurring: RecurringTransaction, after: LocalDate): LocalDate {
        return when (RecurringFrequency.fromName(recurring.frequency)) {
            RecurringFrequency.MONTHLY -> {
                var ym = YearMonth.from(after)
                var candidate = ym.atDay(recurring.dayOfMonth.coerceIn(1, ym.lengthOfMonth()))
                if (!candidate.isAfter(after)) {
                    ym = ym.plusMonths(1)
                    candidate = ym.atDay(recurring.dayOfMonth.coerceIn(1, ym.lengthOfMonth()))
                }
                candidate
            }
            RecurringFrequency.WEEKLY -> {
                val targetDow = DayOfWeek.of(recurring.dayOfWeek.coerceIn(1, 7))
                var candidate = after.plusDays(1)
                while (candidate.dayOfWeek != targetDow) candidate = candidate.plusDays(1)
                candidate
            }
        }
    }

    private suspend fun evaluateRecurrings() {
        val today = LocalDate.now()
        var createdAny = false
        recurringDao.getAllOnce().filter { it.isActive }.forEach { recurring ->
            var anchor = recurring.lastRunEpochDay?.let { LocalDate.ofEpochDay(it) }
                ?: LocalDate.ofEpochDay(recurring.createdEpochDay)
            var lastDue: LocalDate? = null
            var guard = 0
            while (guard < 36) {
                val nextDue = nextDueDate(recurring, anchor)
                if (nextDue.isAfter(today)) break
                dao.insert(
                    Transaction(
                        type = recurring.type,
                        category = recurring.category,
                        amount = recurring.amount,
                        dateEpochDay = nextDue.toEpochDay(),
                        note = recurring.name + " · otomatis",
                        walletId = recurring.walletId
                    )
                )
                lastDue = nextDue
                anchor = nextDue
                createdAny = true
                guard++
            }
            if (lastDue != null) {
                recurringDao.update(recurring.copy(lastRunEpochDay = lastDue.toEpochDay()))
            }
        }
        if (createdAny) DoitWidgetProvider.refresh(getApplication())
    }

    private suspend fun resolveDefaultWalletId(): Long? {
        val walletList = walletDao.getAllOnce()
        if (walletList.isEmpty()) return null
        val last = settings.lastWalletId.value
        return if (walletList.any { it.id == last }) last else walletList.first().id
    }

    fun walletName(id: Long?): String {
        return wallets.value.firstOrNull { it.id == id }?.name
            ?: wallets.value.firstOrNull()?.name
            ?: "Dompet"
    }

    fun addTransaction(
        type: TxnType,
        category: Category,
        amount: Long,
        date: LocalDate,
        note: String,
        walletId: Long? = null
    ) {
        viewModelScope.launch {
            val resolved = walletId ?: resolveDefaultWalletId()
            dao.insert(
                Transaction(
                    type = type.name,
                    category = category.name,
                    amount = amount,
                    dateEpochDay = date.toEpochDay(),
                    note = note.trim(),
                    walletId = resolved
                )
            )
            if (resolved != null) settings.setLastWalletId(resolved)
            settings.setLastCategory(category.name)
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    /** Transfer antar dompet: BUKAN pemasukan/pengeluaran, satu baris tipe TRANSFER. */
    fun addTransfer(fromWalletId: Long, toWalletId: Long, amount: Long, date: LocalDate, note: String) {
        if (fromWalletId == toWalletId || amount <= 0) return
        viewModelScope.launch {
            val walletList = walletDao.getAllOnce()
            val fromName = walletList.firstOrNull { it.id == fromWalletId }?.name ?: ""
            val toName = walletList.firstOrNull { it.id == toWalletId }?.name ?: ""
            val fullNote = listOf("$fromName → $toName", note.trim())
                .filter { it.isNotBlank() }.joinToString(" · ")
            dao.insert(
                Transaction(
                    type = TxnType.TRANSFER.name,
                    category = Category.TRANSFER.name,
                    amount = amount,
                    dateEpochDay = date.toEpochDay(),
                    note = fullNote,
                    walletId = fromWalletId,
                    transferToWalletId = toWalletId
                )
            )
            settings.setLastWalletId(fromWalletId)
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

    // ---------- Dompet ----------

    fun addWallet(name: String, type: WalletType, initialBalance: Long) {
        viewModelScope.launch {
            walletDao.insert(Wallet(name = name.trim(), type = type.name, initialBalance = initialBalance))
        }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch { walletDao.update(wallet) }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            // Dompet terakhir tidak boleh dihapus — transaksi butuh dompet default.
            if (walletDao.getAllOnce().size <= 1) return@launch
            walletDao.delete(wallet)
        }
    }

    // ---------- Transaksi berulang ----------

    fun addRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch { recurringDao.insert(recurring) }
    }

    fun updateRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch { recurringDao.update(recurring) }
    }

    fun setRecurringActive(recurring: RecurringTransaction, active: Boolean) {
        viewModelScope.launch { recurringDao.update(recurring.copy(isActive = active)) }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch { recurringDao.delete(recurring) }
    }

    // ---------- Utang & piutang ----------

    fun addDebt(personName: String, amount: Long, direction: DebtDirection, date: LocalDate) {
        viewModelScope.launch {
            debtDao.insert(
                Debt(
                    personName = personName.trim(),
                    amount = amount,
                    direction = direction.name,
                    paid = 0,
                    isSettled = false,
                    dateEpochDay = date.toEpochDay()
                )
            )
        }
    }

    fun recordDebtPayment(debt: Debt, payment: Long) {
        if (payment <= 0) return
        viewModelScope.launch {
            val newPaid = (debt.paid + payment).coerceAtMost(debt.amount)
            debtDao.update(
                debt.copy(paid = newPaid, isSettled = newPaid >= debt.amount)
            )
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch { debtDao.delete(debt) }
    }

    // ---------- Tagihan ----------

    fun addBill(name: String, amount: Long, dueDayOfMonth: Int) {
        viewModelScope.launch {
            billDao.insert(
                Bill(
                    name = name.trim(),
                    amount = amount,
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31)
                )
            )
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch { billDao.update(bill) }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch { billDao.delete(bill) }
    }

    /** Tandai tagihan bulan ini lunas + catat sebagai pengeluaran di dompet terakhir dipakai. */
    fun markBillPaid(bill: Bill) {
        viewModelScope.launch {
            val currentMonthKey = YearMonth.now().toString()
            if (bill.paidMonth == currentMonthKey) return@launch
            billDao.update(bill.copy(paidMonth = currentMonthKey))
            val walletId = resolveDefaultWalletId()
            dao.insert(
                Transaction(
                    type = TxnType.EXPENSE.name,
                    category = Category.LAIN_KELUAR.name,
                    amount = bill.amount,
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    note = "Tagihan: " + bill.name,
                    walletId = walletId
                )
            )
            DoitWidgetProvider.refresh(getApplication())
        }
    }

    // ---------- Laporan + ekspor CSV ----------

    fun exportCsv() {
        viewModelScope.launch {
            val month = selectedMonth.value
            val txs = allTransactions.value
                .filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
                .sortedBy { it.dateEpochDay }
            val walletList = wallets.value
            fun nameOf(id: Long?): String =
                walletList.firstOrNull { it.id == id }?.name
                    ?: walletList.firstOrNull()?.name ?: "Dompet"
            fun csvEscape(text: String): String = text.replace("\"", "\"\"")
            val csv = StringBuilder("tanggal,tipe,kategori,dompet,jumlah,catatan\n")
            txs.forEach { tx ->
                val typeLabel = when (tx.type) {
                    TxnType.INCOME.name -> "Pemasukan"
                    TxnType.EXPENSE.name -> "Pengeluaran"
                    else -> "Transfer"
                }
                val walletLabel = if (tx.type == TxnType.TRANSFER.name) {
                    nameOf(tx.walletId) + " -> " + nameOf(tx.transferToWalletId)
                } else {
                    nameOf(tx.walletId)
                }
                csv.append(LocalDate.ofEpochDay(tx.dateEpochDay))
                    .append(',').append(typeLabel)
                    .append(',').append(Category.fromName(tx.category).label)
                    .append(',').append(walletLabel)
                    .append(',').append(tx.amount)
                    .append(",\"").append(csvEscape(tx.note)).append("\"\n")
            }
            val app = getApplication<Application>()
            val dir = File(app.cacheDir, "exports").apply { mkdirs() }
            val fileName = "doit-" + month.toString() + ".csv"
            val file = File(dir, fileName)
            file.writeText(csv.toString())
            val uri = FileProvider.getUriForFile(app, "com.xeylabs.doit.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "do.it — transaksi " + formatMonthCsv(month))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(chooser)
        }
    }

    private fun formatMonthCsv(month: YearMonth): String {
        return month.format(DateTimeFormatter.ofPattern("MMMM yyyy", indonesia))
    }

    // ---------- Target tabungan (v1.1, tidak berubah) ----------

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
