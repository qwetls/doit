package com.xeylabs.doit.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("doit_settings", Context.MODE_PRIVATE)

    val budgetBelanja: StateFlow<Long> =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_BELANJA, 200_000L))
    val budgetDarurat: StateFlow<Long> =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_DARURAT, 100_000L))
    val budgetTabungan: StateFlow<Long> =
        MutableStateFlow(prefs.getLong(KEY_BUDGET_TABUNGAN, 700_000L))
    val savingsTarget: StateFlow<Long> =
        MutableStateFlow(prefs.getLong(KEY_SAVINGS_TARGET, 3_000_000L))

    fun setBudgetBelanja(value: Long) = save(KEY_BUDGET_BELANJA, value, budgetBelanja)
    fun setBudgetDarurat(value: Long) = save(KEY_BUDGET_DARURAT, value, budgetDarurat)
    fun setBudgetTabungan(value: Long) = save(KEY_BUDGET_TABUNGAN, value, budgetTabungan)
    fun setSavingsTarget(value: Long) = save(KEY_SAVINGS_TARGET, value, savingsTarget)

    private fun save(key: String, value: Long, flow: MutableStateFlow<Long>) {
        prefs.edit().putLong(key, value).apply()
        flow.value = value
    }

    companion object {
        const val KEY_BUDGET_BELANJA = "budget_belanja"
        const val KEY_BUDGET_DARURAT = "budget_darurat"
        const val KEY_BUDGET_TABUNGAN = "budget_tabungan"
        const val KEY_SAVINGS_TARGET = "savings_target"
    }
}
