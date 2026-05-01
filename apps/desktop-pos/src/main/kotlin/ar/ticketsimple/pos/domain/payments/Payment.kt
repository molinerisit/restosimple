package ar.ticketsimple.pos.domain.payments

import java.time.LocalDateTime

enum class PaymentMethod(val label: String) {
    CASH("Efectivo"),
    DEBIT("Débito"),
    CREDIT("Crédito"),
    QR("QR / MP"),
    TRANSFER("Transferencia"),
    OTHER("Otro")
}

data class Payment(
    val id: String,
    val saleId: String,
    val method: PaymentMethod,
    val amount: Double,
    val received: Double = amount,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val change: Double get() = (received - amount).coerceAtLeast(0.0)
}
