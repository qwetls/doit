package com.xeylabs.doit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Transaction::class,
        SavingsGoal::class,
        Wallet::class,
        RecurringTransaction::class,
        Debt::class,
        Bill::class,
        BudgetPosition::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun walletDao(): WalletDao
    abstract fun recurringDao(): RecurringDao
    abstract fun debtDao(): DebtDao
    abstract fun billDao(): BillDao
    abstract fun budgetPositionDao(): BudgetPositionDao

    companion object {
        private const val CREATE_WALLETS =
            "CREATE TABLE IF NOT EXISTS wallets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "initialBalance INTEGER NOT NULL)"
        private const val CREATE_RECURRING =
            "CREATE TABLE IF NOT EXISTS recurring_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "type TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "walletId INTEGER NOT NULL, " +
                "frequency TEXT NOT NULL, " +
                "dayOfMonth INTEGER NOT NULL, " +
                "dayOfWeek INTEGER NOT NULL, " +
                "isActive INTEGER NOT NULL, " +
                "createdEpochDay INTEGER NOT NULL, " +
                "lastRunEpochDay INTEGER)"
        private const val CREATE_DEBTS =
            "CREATE TABLE IF NOT EXISTS debts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "personName TEXT NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "direction TEXT NOT NULL, " +
                "paid INTEGER NOT NULL, " +
                "isSettled INTEGER NOT NULL, " +
                "dateEpochDay INTEGER NOT NULL)"
        private const val CREATE_BILLS =
            "CREATE TABLE IF NOT EXISTS bills (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "dueDayOfMonth INTEGER NOT NULL, " +
                "paidMonth TEXT NOT NULL)"

        private fun seedDefaultWallets(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO wallets (name, type, initialBalance) VALUES ('Tunai', 'TUNAI', 0)")
            db.execSQL("INSERT INTO wallets (name, type, initialBalance) VALUES ('SeaBank', 'BANK', 0)")
        }

        private fun createFinanceTables(db: SupportSQLiteDatabase) {
            db.execSQL(CREATE_WALLETS)
            db.execSQL(CREATE_RECURRING)
            db.execSQL(CREATE_DEBTS)
            db.execSQL(CREATE_BILLS)
            // Kolom nullable tanpa DEFAULT: cocok persis dengan skema yang
            // Room hasilkan dari entity, jadi validasi migrasi lolos.
            db.execSQL("ALTER TABLE transactions ADD COLUMN walletId INTEGER")
            db.execSQL("ALTER TABLE transactions ADD COLUMN transferToWalletId INTEGER")
        }

        // v1 (rilis 1.0.0) -> v3: tabel tabungan belum ada di v1.
        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS savings_goals (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "target INTEGER NOT NULL, " +
                        "saved INTEGER NOT NULL, " +
                        "isDefault INTEGER NOT NULL)"
                )
                createFinanceTables(db)
                seedDefaultWallets(db)
            }
        }

        // v2 (rilis 1.1.0) -> v3: semua data lama dipertahankan.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createFinanceTables(db)
                seedDefaultWallets(db)
            }
        }

        // v3 (rilis 1.2.0/1.3.0) -> v4: mata uang dompet, jenis + penyedia utang,
        // jumlah diterima transfer. Semua kolom nullable tanpa DEFAULT — cocok
        // persis dengan skema yang Room hasilkan dari entity, jadi validasi
        // migrasi lolos dan semua data lama dipertahankan.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE wallets ADD COLUMN currency TEXT")
                db.execSQL("ALTER TABLE debts ADD COLUMN kind TEXT")
                db.execSQL("ALTER TABLE debts ADD COLUMN provider TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN transferReceivedAmount INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN receiptPath TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS budget_positions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "limitAmount INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doit.db"
                ).addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
        }
    }
}
