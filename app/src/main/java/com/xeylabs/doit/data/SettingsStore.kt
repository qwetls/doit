package com.xeylabs.doit.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsSnapshot(
    val budgetBelanja: Long = 200_000L,
    val budgetDarurat: Long = 100_000L,
    val budgetTabungan: Long = 700_000L,
    val savingsTarget: Long = 3_000_000L,
    val monthlyIncome: Long = 1_000_000L,
    val paydayDay: Int = 0,
    val displayName: String = "Adrien",
    val defaultCurrency: String = "IDR",
    val autoAllocate: Boolean = false,
    val allocPctBelanja: Int = 20,
    val allocPctDarurat: Int = 10,
    val allocPctTabungan: Int = 70,
    val hiddenFixedPots: Set<String> = emptySet()
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("doit_settings", Context.MODE_PRIVATE)

    private val _budgetBelanja =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_BELANJA, 200_000L))
    val budgetBelanja: StateFlow<Long> = _budgetBelanja
    private val _budgetDarurat =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_DARURAT, 100_000L))
    val budgetDarurat: StateFlow<Long> = _budgetDarurat
    private val _budgetTabungan =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_TABUNGAN, 700_000L))
    val budgetTabungan: StateFlow<Long> = _budgetTabungan
    private val _savingsTarget =
        MutableStateFlow(prefs.getLong(KEY_SAVINGS_TARGET, 3_000_000L))
    val savingsTarget: StateFlow<Long> = _savingsTarget

    private val _monthlyIncome =
        MutableStateFlow(prefs.getLong(KEY_MONTHLY_INCOME, 1_000_000L))
    val monthlyIncome: StateFlow<Long> = _monthlyIncome

    private val _paydayDay =
        MutableStateFlow(prefs.getInt(KEY_PAYDAY_DAY, 0))
    val paydayDay: StateFlow<Int> = _paydayDay

    private val _onboardingDone =
        MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    private val _lastCategory =
        MutableStateFlow(prefs.getString(KEY_LAST_CATEGORY, null))
    val lastCategory: StateFlow<String?> = _lastCategory

    private val _lastWalletId =
        MutableStateFlow(prefs.getLong(KEY_LAST_WALLET_ID, 0L))
    val lastWalletId: StateFlow<Long> = _lastWalletId

    private val _displayName =
        MutableStateFlow(prefs.getString(KEY_DISPLAY_NAME, "Adrien") ?: "Adrien")
    val displayName: StateFlow<String> = _displayName

    private val _defaultCurrency =
        MutableStateFlow(prefs.getString(KEY_DEFAULT_CURRENCY, "IDR") ?: "IDR")
    val defaultCurrency: StateFlow<String> = _defaultCurrency

    private val _autoAllocate =
        MutableStateFlow(prefs.getBoolean(KEY_AUTO_ALLOCATE, false))
    val autoAllocate: StateFlow<Boolean> = _autoAllocate

    private val _allocPctBelanja =
        MutableStateFlow(prefs.getInt(KEY_ALLOC_PCT_BELANJA, 20))
    val allocPctBelanja: StateFlow<Int> = _allocPctBelanja

    private val _allocPctDarurat =
        MutableStateFlow(prefs.getInt(KEY_ALLOC_PCT_DARURAT, 10))
    val allocPctDarurat: StateFlow<Int> = _allocPctDarurat

    private val _allocPctTabungan =
        MutableStateFlow(prefs.getInt(KEY_ALLOC_PCT_TABUNGAN, 70))
    val allocPctTabungan: StateFlow<Int> = _allocPctTabungan

    private val _hiddenFixedPots =
        MutableStateFlow(
            prefs.getString(KEY_HIDDEN_FIXED_POTS, "")
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet() ?: emptySet()
        )
    val hiddenFixedPots: StateFlow<Set<String>> = _hiddenFixedPots

    fun setBudgetBelanja(value: Long) = save(KEY_BUDGET_BELANJA, value, _budgetBelanja)
    fun setBudgetDarurat(value: Long) = save(KEY_BUDGET_DARURAT, value, _budgetDarurat)
    fun setBudgetTabungan(value: Long) = save(KEY_BUDGET_TABUNGAN, value, _budgetTabungan)
    fun setSavingsTarget(value: Long) = save(KEY_SAVINGS_TARGET, value, _savingsTarget)
    fun setMonthlyIncome(value: Long) = save(KEY_MONTHLY_INCOME, value, _monthlyIncome)

    fun setPaydayDay(value: Int) {
        if (value !in 0..31) return
        prefs.edit().putInt(KEY_PAYDAY_DAY, value).apply()
        _paydayDay.value = value
    }

    fun setOnboardingDone(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()
        _onboardingDone.value = value
    }

    fun setLastCategory(name: String) {
        prefs.edit().putString(KEY_LAST_CATEGORY, name).apply()
        _lastCategory.value = name
    }

    fun setLastWalletId(id: Long) {
        prefs.edit().putLong(KEY_LAST_WALLET_ID, id).apply()
        _lastWalletId.value = id
    }

    fun setDisplayName(value: String) {
        val name = value.trim().ifBlank { "Adrien" }
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
        _displayName.value = name
    }

    fun setDefaultCurrency(code: String) {
        val normalized = if (code == "USD") "USD" else "IDR"
        prefs.edit().putString(KEY_DEFAULT_CURRENCY, normalized).apply()
        _defaultCurrency.value = normalized
    }

    fun setAutoAllocate(value: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ALLOCATE, value).apply()
        _autoAllocate.value = value
    }

    fun setAllocationPercents(belanja: Int, darurat: Int, tabungan: Int) {
        if (belanja !in 0..100 || darurat !in 0..100 || tabungan !in 0..100) return
        if (belanja + darurat + tabungan != 100) return
        prefs.edit()
            .putInt(KEY_ALLOC_PCT_BELANJA, belanja)
            .putInt(KEY_ALLOC_PCT_DARURAT, darurat)
            .putInt(KEY_ALLOC_PCT_TABUNGAN, tabungan)
            .apply()
        _allocPctBelanja.value = belanja
        _allocPctDarurat.value = darurat
        _allocPctTabungan.value = tabungan
    }

    fun setHiddenFixedPots(pots: Set<String>) {
        prefs.edit().putString(KEY_HIDDEN_FIXED_POTS, pots.joinToString(",")).apply()
        _hiddenFixedPots.value = pots
    }

    fun setFixedPotHidden(potKey: String, hidden: Boolean) {
        val updated = if (hidden) _hiddenFixedPots.value + potKey else _hiddenFixedPots.value - potKey
        setHiddenFixedPots(updated)
    }

    fun snapshot(): SettingsSnapshot = SettingsSnapshot(
        budgetBelanja = _budgetBelanja.value,
        budgetDarurat = _budgetDarurat.value,
        budgetTabungan = _budgetTabungan.value,
        savingsTarget = _savingsTarget.value,
        monthlyIncome = _monthlyIncome.value,
        paydayDay = _paydayDay.value,
        displayName = _displayName.value,
        defaultCurrency = _defaultCurrency.value,
        autoAllocate = _autoAllocate.value,
        allocPctBelanja = _allocPctBelanja.value,
        allocPctDarurat = _allocPctDarurat.value,
        allocPctTabungan = _allocPctTabungan.value,
        hiddenFixedPots = _hiddenFixedPots.value
    )

    fun restore(snapshot: SettingsSnapshot) {
        setBudgetBelanja(snapshot.budgetBelanja)
        setBudgetDarurat(snapshot.budgetDarurat)
        setBudgetTabungan(snapshot.budgetTabungan)
        setSavingsTarget(snapshot.savingsTarget)
        setMonthlyIncome(snapshot.monthlyIncome)
        setPaydayDay(snapshot.paydayDay)
        setDisplayName(snapshot.displayName)
        setDefaultCurrency(snapshot.defaultCurrency)
        setAutoAllocate(snapshot.autoAllocate)
        setAllocationPercents(snapshot.allocPctBelanja, snapshot.allocPctDarurat, snapshot.allocPctTabungan)
        setHiddenFixedPots(snapshot.hiddenFixedPots)
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _budgetBelanja.value = 200_000L
        _budgetDarurat.value = 100_000L
        _budgetTabungan.value = 700_000L
        _savingsTarget.value = 3_000_000L
        _monthlyIncome.value = 1_000_000L
        _paydayDay.value = 0
        _onboardingDone.value = false
        _lastCategory.value = null
        _lastWalletId.value = 0L
        _displayName.value = "Adrien"
        _defaultCurrency.value = "IDR"
        _autoAllocate.value = false
        _allocPctBelanja.value = 20
        _allocPctDarurat.value = 10
        _allocPctTabungan.value = 70
        _hiddenFixedPots.value = emptySet()
    }

    private fun save(key: String, value: Long, flow: MutableStateFlow<Long>) {
        prefs.edit().putLong(key, value).apply()
        flow.value = value
    }

    companion object {
        const val KEY_BUDGET_BELANJA = "budget_belanja"
        const val KEY_BUDGET_DARURAT = "budget_darurat"
        const val KEY_BUDGET_TABUNGAN = "budget_tabungan"
        const val KEY_SAVINGS_TARGET = "savings_target"
        const val KEY_MONTHLY_INCOME = "monthly_income"
        const val KEY_PAYDAY_DAY = "payday_day"
        const val KEY_ONBOARDING_DONE = "onboarding_done"
        const val KEY_LAST_CATEGORY = "last_category"
        const val KEY_LAST_WALLET_ID = "last_wallet_id"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_DEFAULT_CURRENCY = "default_currency"
        const val KEY_AUTO_ALLOCATE = "auto_allocate"
        const val KEY_ALLOC_PCT_BELANJA = "alloc_pct_belanja"
        const val KEY_ALLOC_PCT_DARURAT = "alloc_pct_darurat"
        const val KEY_ALLOC_PCT_TABUNGAN = "alloc_pct_tabungan"
        const val KEY_HIDDEN_FIXED_POTS = "hidden_fixed_pots"
    }
}
