package ar.ticketsimple.pos.infrastructure.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    fun init(dataDir: String = "data") {
        File(dataDir).mkdirs()
        val dbPath = "$dataDir/ticketsimple.db"

        Database.connect(
            url    = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            exec("PRAGMA journal_mode=WAL;")
            exec("PRAGMA foreign_keys=ON;")
            exec("PRAGMA synchronous=NORMAL;")
            SchemaUtils.createMissingTablesAndColumns(
                CategoryTable,
                ProductTable,
                UserTable,
                ShiftTable,
                CashMovementTable,
                SaleTable,
                SaleItemTable,
                PaymentTable,
                AuditTable
            )
        }
    }
}
