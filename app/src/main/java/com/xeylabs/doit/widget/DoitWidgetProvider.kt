package com.xeylabs.doit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.xeylabs.doit.MainActivity
import com.xeylabs.doit.R
import com.xeylabs.doit.data.AppDatabase
import com.xeylabs.doit.data.Currency
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.ui.formatMoney
import com.xeylabs.doit.ui.formatRupiah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class DoitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateOne(context, appWidgetManager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DoitWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.xeylabs.doit.action.REFRESH_WIDGET"

        fun refresh(context: Context) {
            context.sendBroadcast(Intent(ACTION_REFRESH).setPackage(context.packageName))
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.get(context)
                val txs = db.transactionDao().getAllOnce()
                val goals = db.savingsGoalDao().getAllOnce()
                val wallets = db.walletDao().getAllOnce()
                val prefs = context.getSharedPreferences("doit_settings", Context.MODE_PRIVATE)
                val budgetBelanja = prefs.getLong("budget_belanja", 200_000L)
                val budgetDarurat = prefs.getLong("budget_darurat", 100_000L)
                // Widget memakai mata uang utama — Rp dan Dollar tidak dicampur.
                val defaultCurrency = Currency.fromCode(prefs.getString("default_currency", "IDR"))
                val defaultWalletId = wallets.firstOrNull()?.id
                fun txCurrency(tx: com.xeylabs.doit.data.Transaction): String =
                    wallets.firstOrNull { it.id == (tx.walletId ?: defaultWalletId) }?.currency ?: "IDR"
                val dcTxs = txs.filter { txCurrency(it) == defaultCurrency.code }

                val month = YearMonth.now()
                val inMonth = dcTxs.filter {
                    YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month
                }
                val monthIncome = inMonth.filter { it.type == TxnType.INCOME.name }.sumOf { it.amount }
                val monthExpense = inMonth.filter { it.type == TxnType.EXPENSE.name }.sumOf { it.amount }
                val remaining = monthIncome - monthExpense
                val budgetSpent = inMonth.filter {
                    it.type == TxnType.EXPENSE.name &&
                        (it.category == "BELANJA" || it.category == "DARURAT")
                }.sumOf { it.amount }
                val budgetRemaining = (budgetBelanja + budgetDarurat - budgetSpent).coerceAtLeast(0)

                val defaultGoal = goals.firstOrNull { it.isDefault } ?: goals.firstOrNull()
                val savingsText = if (defaultGoal != null) {
                    val pct = if (defaultGoal.target > 0) {
                        (defaultGoal.saved * 100 / defaultGoal.target).toInt().coerceIn(0, 100)
                    } else 0
                    "${defaultGoal.name}: " + formatRupiah(defaultGoal.saved) +
                        " dari " + formatRupiah(defaultGoal.target) + " ($pct%)"
                } else {
                    "Belum ada target tabungan"
                }
                val savingsPct = if (defaultGoal != null && defaultGoal.target > 0) {
                    (defaultGoal.saved * 100 / defaultGoal.target).toInt().coerceIn(0, 100)
                } else 0

                val views = RemoteViews(context.packageName, R.layout.widget_doit)
                views.setTextViewText(R.id.widget_remaining, formatMoney(remaining, defaultCurrency))
                views.setTextViewText(
                    R.id.widget_budget,
                    "Sisa anggaran: " + formatMoney(budgetRemaining, defaultCurrency)
                )
                views.setTextViewText(R.id.widget_savings, savingsText)
                views.setProgressBar(R.id.widget_savings_bar, 100, savingsPct, false)

                val openApp = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openApp)
                manager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
