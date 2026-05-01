package ar.ticketsimple.pos.infrastructure.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.DriverManager

object DatabaseFactory {

    fun init(dataDir: String = "data") {
        File(dataDir).mkdirs()
        val jdbcUrl = "jdbc:sqlite:$dataDir/ticketsimple.db"

        // PRAGMAs must run outside any transaction
        DriverManager.getConnection(jdbcUrl).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("PRAGMA journal_mode=WAL;")
                stmt.execute("PRAGMA foreign_keys=ON;")
                stmt.execute("PRAGMA synchronous=NORMAL;")
            }
        }

        Database.connect(url = jdbcUrl, driver = "org.sqlite.JDBC")

        transaction {
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
