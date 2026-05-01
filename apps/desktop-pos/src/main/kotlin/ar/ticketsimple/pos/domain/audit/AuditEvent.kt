package ar.ticketsimple.pos.domain.audit

import java.time.LocalDateTime

enum class AuditAction {
    SALE_CREATED, SALE_PAID, SALE_VOIDED, SALE_SUSPENDED,
    ITEM_VOIDED, DISCOUNT_APPLIED, SURCHARGE_APPLIED,
    SHIFT_OPENED, SHIFT_CLOSED,
    CASH_IN, CASH_OUT,
    PRODUCT_AVAILABILITY_CHANGED
}

data class AuditEvent(
    val id: String,
    val action: AuditAction,
    val entityId: String,
    val userId: String,
    val data: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

interface AuditRepository {
    fun record(event: AuditEvent)
    fun eventsForEntity(entityId: String): List<AuditEvent>
}
