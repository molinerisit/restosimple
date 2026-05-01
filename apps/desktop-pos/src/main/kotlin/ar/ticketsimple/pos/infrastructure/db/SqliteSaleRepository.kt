package ar.ticketsimple.pos.infrastructure.db

import ar.ticketsimple.pos.domain.payments.Payment
import ar.ticketsimple.pos.domain.payments.PaymentMethod
import ar.ticketsimple.pos.domain.sales.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class SqliteSaleRepository : SaleRepository {

    override fun save(sale: Sale): Sale = transaction {
        val exists = SaleTable.selectAll().where { SaleTable.id eq sale.id }.count() > 0L

        if (exists) {
            SaleTable.update({ SaleTable.id eq sale.id }) {
                it[discountPercent]  = sale.discountPercent
                it[surchargePercent] = sale.surchargePercent
                it[status]           = sale.status.name
                it[notes]            = sale.notes
                it[closedAt]         = sale.closedAt
            }
            SaleItemTable.deleteWhere { saleId eq sale.id }
        } else {
            SaleTable.insert {
                it[id]               = sale.id
                it[shiftId]          = sale.shiftId
                it[userId]           = sale.userId
                it[discountPercent]  = sale.discountPercent
                it[surchargePercent] = sale.surchargePercent
                it[status]           = sale.status.name
                it[notes]            = sale.notes
                it[createdAt]        = sale.createdAt
                it[closedAt]         = sale.closedAt
            }
        }

        sale.items.forEach { item ->
            SaleItemTable.insert {
                it[id]          = item.id
                it[saleId]      = item.saleId
                it[productId]   = item.productId
                it[productName] = item.productName
                it[quantity]    = item.quantity
                it[unitPrice]   = item.unitPrice
                it[notes]       = item.notes
                it[voided]      = item.voided
            }
        }
        sale
    }

    fun savePayment(payment: Payment) = transaction {
        PaymentTable.insert {
            it[id]        = payment.id
            it[saleId]    = payment.saleId
            it[method]    = payment.method.name
            it[amount]    = payment.amount
            it[received]  = payment.received
            it[createdAt] = payment.createdAt
        }
    }

    override fun findById(id: String): Sale? = transaction {
        val row = SaleTable.selectAll().where { SaleTable.id eq id }.singleOrNull() ?: return@transaction null
        val items = SaleItemTable.selectAll().where { SaleItemTable.saleId eq id }.map { it.toItem(id) }
        row.toSale(items)
    }

    override fun salesByShift(shiftId: String): List<Sale> = transaction {
        SaleTable.selectAll().where { SaleTable.shiftId eq shiftId }.map { row ->
            val items = SaleItemTable.selectAll().where { SaleItemTable.saleId eq row[SaleTable.id] }.map { it.toItem(row[SaleTable.id]) }
            row.toSale(items)
        }
    }

    override fun salesByDate(date: LocalDate): List<Sale> = transaction {
        val start = date.atStartOfDay()
        val end   = date.plusDays(1).atStartOfDay()
        SaleTable.selectAll()
            .where { (SaleTable.createdAt greaterEq start) and (SaleTable.createdAt less end) }
            .map { row ->
                val items = SaleItemTable.selectAll().where { SaleItemTable.saleId eq row[SaleTable.id] }.map { it.toItem(row[SaleTable.id]) }
                row.toSale(items)
            }
    }

    override fun todaySales(): List<Sale> = salesByDate(LocalDate.now())

    fun paymentsForSale(saleId: String): List<Payment> = transaction {
        PaymentTable.selectAll().where { PaymentTable.saleId eq saleId }.map { row ->
            Payment(
                id        = row[PaymentTable.id],
                saleId    = row[PaymentTable.saleId],
                method    = PaymentMethod.valueOf(row[PaymentTable.method]),
                amount    = row[PaymentTable.amount],
                received  = row[PaymentTable.received],
                createdAt = row[PaymentTable.createdAt]
            )
        }
    }

    private fun ResultRow.toSale(items: List<SaleItem>) = Sale(
        id               = this[SaleTable.id],
        shiftId          = this[SaleTable.shiftId],
        userId           = this[SaleTable.userId],
        items            = items,
        discountPercent  = this[SaleTable.discountPercent],
        surchargePercent = this[SaleTable.surchargePercent],
        status           = SaleStatus.valueOf(this[SaleTable.status]),
        notes            = this[SaleTable.notes],
        createdAt        = this[SaleTable.createdAt],
        closedAt         = this[SaleTable.closedAt]
    )

    private fun ResultRow.toItem(sId: String) = SaleItem(
        id          = this[SaleItemTable.id],
        saleId      = sId,
        productId   = this[SaleItemTable.productId],
        productName = this[SaleItemTable.productName],
        quantity    = this[SaleItemTable.quantity],
        unitPrice   = this[SaleItemTable.unitPrice],
        notes       = this[SaleItemTable.notes],
        voided      = this[SaleItemTable.voided]
    )
}
