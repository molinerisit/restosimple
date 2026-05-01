package ar.ticketsimple.pos.domain.sales

import java.time.LocalDateTime

enum class SaleStatus { OPEN, PAID, VOIDED, SUSPENDED }

data class Sale(
    val id: String,
    val shiftId: String,
    val userId: String,
    val items: List<SaleItem> = emptyList(),
    val discountPercent: Double = 0.0,
    val surchargePercent: Double = 0.0,
    val status: SaleStatus = SaleStatus.OPEN,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val closedAt: LocalDateTime? = null
) {
    val subtotal: Double get() = items.sumOf { it.lineTotal }
    val discountAmount: Double get() = subtotal * discountPercent / 100.0
    val surchargeAmount: Double get() = subtotal * surchargePercent / 100.0
    val total: Double get() = subtotal - discountAmount + surchargeAmount
    val itemCount: Int get() = items.sumOf { it.quantity }
}
