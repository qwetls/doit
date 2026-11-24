package com.xeylabs.doit.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    private val _onboardingDone =
        MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    private val _lastCategory =
        MutableStateFlow(prefs.getString(KEY_LAST_CATEGORY, null))
    val lastCategory: StateFlow<String?> = _lastCategory

    fun setBudgetBelanja(value: Long) = save(KEY_BUDGET_BELANJA, value, _budgetBelanja)
    fun setBudgetDarurat(value: Long) = save(KEY_BUDGET_DARURAT, value, _budgetDarurat)
    fun setBudgetTabungan(value: Long) = save(KEY_BUDGET_TABUNGAN, value, _budgetTabungan)
    fun setSavingsTarget(value: Long) = save(KEY_SAVINGS_TARGET, value, _savingsTarget)
    fun setMonthlyIncome(value: Long) = save(KEY_MONTHLY_INCOME, value, _monthlyIncome)

    fun setOnboardingDone(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()
        _onboardingDone.value = value
    }

    fun setLastCategory(name: String) {
        prefs.edit().putString(KEY_LAST_CATEGORY, name).apply()
        _lastCategory.value = name
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
        const val KEY_ONBOARDING_DONE = "onboarding_done"
        const val KEY_LAST_CATEGORY = "last_category"
    }
}
