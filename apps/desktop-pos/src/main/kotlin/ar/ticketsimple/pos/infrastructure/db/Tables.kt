package ar.ticketsimple.pos.infrastructure.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CategoryTable : Table("categories") {
    val id         = varchar("id", 36)
    val name       = varchar("name", 100)
    val color      = varchar("color", 7).default("#C46A3A")
    val sortOrder  = integer("sort_order").default(0)
    val active     = bool("active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object ProductTable : Table("products") {
    val id         = varchar("id", 36)
    val sku        = varchar("sku", 50).nullable()
    val name       = varchar("name", 200)
    val categoryId = varchar("category_id", 36).references(CategoryTable.id)
    val price      = double("price")
    val available  = bool("available").default(true)
    val byWeight   = bool("by_weight").default(false)
    val imageUrl   = varchar("image_url", 500).nullable()
    val sortOrder  = integer("sort_order").default(0)
    override val primaryKey = PrimaryKey(id)
}

object UserTable : Table("users") {
    val id     = varchar("id", 36)
    val name   = varchar("name", 100)
    val pin    = varchar("pin", 10)
    val role   = varchar("role", 20)
    val active = bool("active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object ShiftTable : Table("shifts") {
    val id           = varchar("id", 36)
    val userId       = varchar("user_id", 36).references(UserTable.id)
    val openedAt     = datetime("opened_at")
    val closedAt     = datetime("closed_at").nullable()
    val openingCash  = double("opening_cash").default(0.0)
    val closingCash  = double("closing_cash").nullable()
    val notes        = text("notes").nullable()
    val status       = varchar("status", 10).default("open")
    override val primaryKey = PrimaryKey(id)
}

object CashMovementTable : Table("cash_movements") {
    val id        = varchar("id", 36)
    val shiftId   = varchar("shift_id", 36).references(ShiftTable.id)
    val type      = varchar("type", 3)
    val amount    = double("amount")
    val reason    = varchar("reason", 300)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object SaleTable : Table("sales") {
    val id                = varchar("id", 36)
    val shiftId           = varchar("shift_id", 36).references(ShiftTable.id)
    val userId            = varchar("user_id", 36)
    val discountPercent   = double("discount_percent").default(0.0)
    val surchargePercent  = double("surcharge_percent").default(0.0)
    val status            = varchar("status", 20).default("open")
    val notes             = text("notes").nullable()
    val createdAt         = datetime("created_at")
    val closedAt          = datetime("closed_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object SaleItemTable : Table("sale_items") {
    val id          = varchar("id", 36)
    val saleId      = varchar("sale_id", 36).references(SaleTable.id)
    val productId   = varchar("product_id", 36)
    val productName = varchar("product_name", 200)
    val quantity    = integer("quantity")
    val unitPrice   = double("unit_price")
    val notes       = text("notes").nullable()
    val voided      = bool("voided").default(false)
    override val primaryKey = PrimaryKey(id)
}

object PaymentTable : Table("payments") {
    val id        = varchar("id", 36)
    val saleId    = varchar("sale_id", 36).references(SaleTable.id)
    val method    = varchar("method", 20)
    val amount    = double("amount")
    val received  = double("received")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object AuditTable : Table("audit_events") {
    val id        = varchar("id", 36)
    val action    = varchar("action", 50)
    val entityId  = varchar("entity_id", 36)
    val userId    = varchar("user_id", 36)
    val data      = text("data").nullable()
    val timestamp = datetime("timestamp")
    override val primaryKey = PrimaryKey(id)
}
