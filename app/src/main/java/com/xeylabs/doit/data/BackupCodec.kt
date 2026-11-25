package com.xeylabs.doit.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class PendingRestore(val summary: String, val data: BackupData)

data class BackupData(
    val transactions: List<Transaction>,
    val wallets: List<Wallet>,
    val goals: List<SavingsGoal>,
    val recurrings: List<RecurringTransaction>,
    val debts: List<Debt>,
    val bills: List<Bill>,
    val budgetPositions: List<BudgetPosition>,
    val customCategories: List<CustomCategory>,
    val settings: SettingsSnapshot?
)

object BackupCodec {
    fun toJson(data: BackupData): String {
        val root = JSONObject()
        root.put("format", "doit-backup")
        root.put("version", 1)
        root.put("exportedAt", Instant.now().toString())
        root.put("transactions", JSONArray(data.transactions.map { txToJson(it) }))
        root.put("wallets", JSONArray(data.wallets.map { walletToJson(it) }))
        root.put("goals", JSONArray(data.goals.map { goalToJson(it) }))
        root.put("recurrings", JSONArray(data.recurrings.map { recurringToJson(it) }))
        root.put("debts", JSONArray(data.debts.map { debtToJson(it) }))
        root.put("bills", JSONArray(data.bills.map { billToJson(it) }))
        root.put("budgetPositions", JSONArray(data.budgetPositions.map { positionToJson(it) }))
        root.put("customCategories", JSONArray(data.customCategories.map { customToJson(it) }))
        data.settings?.let { root.put("settings", settingsToJson(it)) }
        return root.toString(2)
    }

    fun fromJson(text: String): BackupData {
        val root = JSONObject(text)
        if (root.optString("format") != "doit-backup") {
            throw IllegalArgumentException("Bukan file cadangan do.it")
        }
        if (root.optInt("version", -1) != 1) {
            throw IllegalArgumentException("Versi cadangan tidak didukung")
        }
        val transactions = readArray(root, "transactions") { jsonToTx(it) }
        val wallets = readArray(root, "wallets") { jsonToWallet(it) }
        val goals = readArray(root, "goals") { jsonToGoal(it) }
        val recurrings = readArray(root, "recurrings") { jsonToRecurring(it) }
        val debts = readArray(root, "debts") { jsonToDebt(it) }
        val bills = readArray(root, "bills") { jsonToBill(it) }
        val positions = readArray(root, "budgetPositions") { jsonToPosition(it) }
        val customs = readArray(root, "customCategories") { jsonToCustom(it) }
        val settings = root.optJSONObject("settings")?.let { jsonToSettings(it) }
        return BackupData(transactions, wallets, goals, recurrings, debts, bills, positions, customs, settings)
    }

    private fun <T> readArray(root: JSONObject, key: String, parse: (JSONObject) -> T): List<T> {
        val arr = root.optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).map { parse(arr.getJSONObject(it)) }
    }

    private fun txToJson(tx: Transaction): JSONObject = JSONObject().apply {
        put("id", tx.id)
        put("type", tx.type)
        put("category", tx.category)
        put("amount", tx.amount)
        put("dateEpochDay", tx.dateEpochDay)
        put("note", tx.note)
        tx.walletId?.let { put("walletId", it) }
        tx.transferToWalletId?.let { put("transferToWalletId", it) }
        tx.transferReceivedAmount?.let { put("transferReceivedAmount", it) }
        tx.receiptPath?.let { put("receiptPath", it) }
    }

    private fun walletToJson(wallet: Wallet): JSONObject = JSONObject().apply {
        put("id", wallet.id)
        put("name", wallet.name)
        put("type", wallet.type)
        put("initialBalance", wallet.initialBalance)
        wallet.currency?.let { put("currency", it) }
    }

    private fun goalToJson(goal: SavingsGoal): JSONObject = JSONObject().apply {
        put("id", goal.id)
        put("name", goal.name)
        put("target", goal.target)
        put("saved", goal.saved)
        put("isDefault", goal.isDefault)
    }

    private fun recurringToJson(recurring: RecurringTransaction): JSONObject = JSONObject().apply {
        put("id", recurring.id)
        put("name", recurring.name)
        put("amount", recurring.amount)
        put("type", recurring.type)
        put("category", recurring.category)
        put("walletId", recurring.walletId)
        put("frequency", recurring.frequency)
        put("dayOfMonth", recurring.dayOfMonth)
        put("dayOfWeek", recurring.dayOfWeek)
        put("isActive", recurring.isActive)
        put("createdEpochDay", recurring.createdEpochDay)
        recurring.lastRunEpochDay?.let { put("lastRunEpochDay", it) }
    }

    private fun debtToJson(debt: Debt): JSONObject = JSONObject().apply {
        put("id", debt.id)
        put("personName", debt.personName)
        put("amount", debt.amount)
        put("direction", debt.direction)
        put("paid", debt.paid)
        put("isSettled", debt.isSettled)
        put("dateEpochDay", debt.dateEpochDay)
        debt.kind?.let { put("kind", it) }
        debt.provider?.let { put("provider", it) }
    }

    private fun billToJson(bill: Bill): JSONObject = JSONObject().apply {
        put("id", bill.id)
        put("name", bill.name)
        put("amount", bill.amount)
        put("dueDayOfMonth", bill.dueDayOfMonth)
        put("paidMonth", bill.paidMonth)
    }

    private fun positionToJson(position: BudgetPosition): JSONObject = JSONObject().apply {
        put("id", position.id)
        put("name", position.name)
        put("category", position.category)
        put("limitAmount", position.limitAmount)
    }

    private fun customToJson(custom: CustomCategory): JSONObject = JSONObject().apply {
        put("id", custom.id)
        put("name", custom.name)
        put("type", custom.type)
        put("iconKey", custom.iconKey)
        put("colorHex", custom.colorHex)
    }

    private fun settingsToJson(settings: SettingsSnapshot): JSONObject = JSONObject().apply {
        put("budgetBelanja", settings.budgetBelanja)
        put("budgetDarurat", settings.budgetDarurat)
        put("budgetTabungan", settings.budgetTabungan)
        put("savingsTarget", settings.savingsTarget)
        put("monthlyIncome", settings.monthlyIncome)
        put("displayName", settings.displayName)
        put("defaultCurrency", settings.defaultCurrency)
    }

    private fun jsonToTx(obj: JSONObject): Transaction {
        val type = obj.getString("type")
        require(TxnType.entries.any { it.name == type }) { "Tipe transaksi tidak dikenal: $type" }
        return Transaction(
            id = obj.getLong("id"),
            type = type,
            category = obj.getString("category"),
            amount = obj.getLong("amount"),
            dateEpochDay = obj.getLong("dateEpochDay"),
            note = obj.optString("note", ""),
            walletId = if (obj.isNull("walletId")) null else obj.optLong("walletId"),
            transferToWalletId = if (obj.isNull("transferToWalletId")) null else obj.optLong("transferToWalletId"),
            transferReceivedAmount = if (obj.isNull("transferReceivedAmount")) null else obj.optLong("transferReceivedAmount"),
            receiptPath = if (obj.isNull("receiptPath")) null else obj.optString("receiptPath")
        )
    }

    private fun jsonToWallet(obj: JSONObject): Wallet {
        val type = obj.getString("type")
        require(WalletType.entries.any { it.name == type }) { "Tipe dompet tidak dikenal: $type" }
        return Wallet(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            type = type,
            initialBalance = obj.getLong("initialBalance"),
            currency = if (obj.isNull("currency")) null else obj.optString("currency")
        )
    }

    private fun jsonToGoal(obj: JSONObject): SavingsGoal = SavingsGoal(
        id = obj.getLong("id"),
        name = obj.getString("name"),
        target = obj.getLong("target"),
        saved = obj.getLong("saved"),
        isDefault = obj.getBoolean("isDefault")
    )

    private fun jsonToRecurring(obj: JSONObject): RecurringTransaction {
        val type = obj.getString("type")
        require(TxnType.entries.any { it.name == type }) { "Tipe transaksi rutin tidak dikenal: $type" }
        val frequency = obj.getString("frequency")
        require(RecurringFrequency.entries.any { it.name == frequency }) { "Frekuensi tidak dikenal: $frequency" }
        return RecurringTransaction(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            amount = obj.getLong("amount"),
            type = type,
            category = obj.getString("category"),
            walletId = obj.getLong("walletId"),
            frequency = frequency,
            dayOfMonth = obj.getInt("dayOfMonth"),
            dayOfWeek = obj.getInt("dayOfWeek"),
            isActive = obj.getBoolean("isActive"),
            createdEpochDay = obj.getLong("createdEpochDay"),
            lastRunEpochDay = if (obj.isNull("lastRunEpochDay")) null else obj.optLong("lastRunEpochDay")
        )
    }

    private fun jsonToDebt(obj: JSONObject): Debt {
        val direction = obj.getString("direction")
        require(DebtDirection.entries.any { it.name == direction }) { "Arah utang tidak dikenal: $direction" }
        return Debt(
            id = obj.getLong("id"),
            personName = obj.getString("personName"),
            amount = obj.getLong("amount"),
            direction = direction,
            paid = obj.getLong("paid"),
            isSettled = obj.getBoolean("isSettled"),
            dateEpochDay = obj.getLong("dateEpochDay"),
            kind = if (obj.isNull("kind")) null else obj.optString("kind"),
            provider = if (obj.isNull("provider")) null else obj.optString("provider")
        )
    }

    private fun jsonToBill(obj: JSONObject): Bill = Bill(
        id = obj.getLong("id"),
        name = obj.getString("name"),
        amount = obj.getLong("amount"),
        dueDayOfMonth = obj.getInt("dueDayOfMonth"),
        paidMonth = obj.optString("paidMonth", "")
    )

    private fun jsonToPosition(obj: JSONObject): BudgetPosition = BudgetPosition(
        id = obj.getLong("id"),
        name = obj.getString("name"),
        category = obj.getString("category"),
        limitAmount = obj.getLong("limitAmount")
    )

    private fun jsonToCustom(obj: JSONObject): CustomCategory {
        val type = obj.getString("type")
        require(TxnType.entries.any { it.name == type }) { "Jenis kategori tidak dikenal: $type" }
        return CustomCategory(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            type = type,
            iconKey = obj.getString("iconKey"),
            colorHex = obj.getLong("colorHex")
        )
    }

    private fun jsonToSettings(obj: JSONObject): SettingsSnapshot = SettingsSnapshot(
        budgetBelanja = obj.optLong("budgetBelanja", 200_000L),
        budgetDarurat = obj.optLong("budgetDarurat", 100_000L),
        budgetTabungan = obj.optLong("budgetTabungan", 700_000L),
        savingsTarget = obj.optLong("savingsTarget", 3_000_000L),
        monthlyIncome = obj.optLong("monthlyIncome", 1_000_000L),
        displayName = obj.optString("displayName", "Adrien"),
        defaultCurrency = obj.optString("defaultCurrency", "IDR")
    )
}
