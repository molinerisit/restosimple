package ar.ticketsimple.pos.application.sale

import ar.ticketsimple.pos.application.shifts.ShiftService
import ar.ticketsimple.pos.domain.audit.AuditAction
import ar.ticketsimple.pos.domain.audit.AuditEvent
import ar.ticketsimple.pos.domain.audit.AuditRepository
import ar.ticketsimple.pos.domain.payments.Payment
import ar.ticketsimple.pos.domain.payments.PaymentMethod
import ar.ticketsimple.pos.domain.sales.*
import ar.ticketsimple.pos.infrastructure.db.SqliteSaleRepository
import ar.ticketsimple.pos.infrastructure.printing.PrinterPort
import java.time.LocalDateTime
import java.util.UUID

class SaleService(
    private val saleRepo: SqliteSaleRepository,
    private val auditRepo: AuditRepository,
    private val printer: PrinterPort,
    private val shiftService: ShiftService,
    private val businessName: String = "TicketSimple"
) {
    fun newSale(shiftId: String, userId: String): Sale {
        val sale = Sale(id = UUID.randomUUID().toString(), shiftId = shiftId, userId = userId)
        saleRepo.save(sale)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SALE_CREATED, sale.id, userId))
        return sale
    }

    fun save(sale: Sale): Sale = saleRepo.save(sale)

    fun processPayment(
        sale: Sale,
        method: PaymentMethod,
        received: Double,
        userId: String
    ): Pair<Sale, Payment> {
        val payment = Payment(
            id       = UUID.randomUUID().toString(),
            saleId   = sale.id,
            method   = method,
            amount   = sale.total,
            received = received
        )
        val closed = sale.copy(status = SaleStatus.PAID, closedAt = LocalDateTime.now())
        saleRepo.save(closed)
        saleRepo.savePayment(payment)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SALE_PAID, sale.id, userId,
            "method=${method.name},amount=${sale.total}"))
        printer.printTicket(closed, payment, businessName)
        if (method == PaymentMethod.CASH) printer.openCashDrawer()
        return Pair(closed, payment)
    }

    fun voidSale(sale: Sale, userId: String): Sale {
        val voided = sale.copy(status = SaleStatus.VOIDED, closedAt = LocalDateTime.now())
        saleRepo.save(voided)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SALE_VOIDED, sale.id, userId))
        return voided
    }

    fun suspendSale(sale: Sale, userId: String): Sale {
        val suspended = sale.copy(status = SaleStatus.SUSPENDED)
        saleRepo.save(suspended)
        return suspended
    }

    fun salesSummaryForShift(shiftId: String): ShiftSummary {
        val sales = saleRepo.salesByShift(shiftId).filter { it.status == SaleStatus.PAID }
        val payments = sales.flatMap { saleRepo.paymentsForSale(it.id) }
        val byMethod = payments.groupBy { it.method }.mapValues { (_, p) -> p.sumOf { it.amount } }
        return ShiftSummary(
            salesCount = sales.size,
            total      = sales.sumOf { it.total },
            byMethod   = byMethod,
            avgTicket  = if (sales.isEmpty()) 0.0 else sales.sumOf { it.total } / sales.size
        )
    }

    fun topProductsForShift(shiftId: String, limit: Int = 5): List<Triple<String, Int, Double>> {
        val sales = saleRepo.salesByShift(shiftId).filter { it.status == SaleStatus.PAID }
        return sales.flatMap { it.items.filter { i -> !i.voided } }
            .groupBy { it.productName }
            .map { (name, items) -> Triple(name, items.sumOf { it.quantity }, items.sumOf { it.lineTotal }) }
            .sortedByDescending { it.second }
            .take(limit)
    }

    fun sendToKitchen(sale: Sale, businessName: String) {
        printer.printKitchenTicket(sale, businessName)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SALE_CREATED, sale.id, "system", "kitchen_send"))
    }
}

data class ShiftSummary(
    val salesCount: Int,
    val total: Double,
    val byMethod: Map<PaymentMethod, Double>,
    val avgTicket: Double
)
