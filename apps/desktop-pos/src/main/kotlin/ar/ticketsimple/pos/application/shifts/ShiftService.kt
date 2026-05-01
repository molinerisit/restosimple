package ar.ticketsimple.pos.application.shifts

import ar.ticketsimple.pos.domain.audit.AuditAction
import ar.ticketsimple.pos.domain.audit.AuditEvent
import ar.ticketsimple.pos.domain.audit.AuditRepository
import ar.ticketsimple.pos.domain.shifts.*
import java.time.LocalDateTime
import java.util.UUID

class ShiftService(
    private val shiftRepo: ShiftRepository,
    private val auditRepo: AuditRepository
) {
    fun activeShift(): Shift? = shiftRepo.activeShift()

    fun openShift(userId: String, userName: String, openingCash: Double): Shift {
        val shift = Shift(
            id          = UUID.randomUUID().toString(),
            userId      = userId,
            userName    = userName,
            openingCash = openingCash
        )
        shiftRepo.save(shift)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SHIFT_OPENED, shift.id, userId))
        return shift
    }

    fun closeShift(shiftId: String, userId: String, closingCash: Double, notes: String?): Shift {
        val shift = shiftRepo.findById(shiftId) ?: error("Turno no encontrado")
        val closed = shift.copy(
            closedAt    = LocalDateTime.now(),
            closingCash = closingCash,
            notes       = notes,
            status      = ShiftStatus.CLOSED
        )
        shiftRepo.save(closed)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.SHIFT_CLOSED, shiftId, userId,
            "closingCash=$closingCash"))
        return closed
    }

    fun addCashIn(shiftId: String, userId: String, amount: Double, reason: String) {
        val movement = CashMovement(UUID.randomUUID().toString(), shiftId, MovementType.IN, amount, reason)
        shiftRepo.saveCashMovement(movement)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.CASH_IN, shiftId, userId, "amount=$amount"))
    }

    fun addCashOut(shiftId: String, userId: String, amount: Double, reason: String) {
        val movement = CashMovement(UUID.randomUUID().toString(), shiftId, MovementType.OUT, amount, reason)
        shiftRepo.saveCashMovement(movement)
        auditRepo.record(AuditEvent(UUID.randomUUID().toString(), AuditAction.CASH_OUT, shiftId, userId, "amount=$amount"))
    }

    fun movements(shiftId: String): List<CashMovement> = shiftRepo.movementsByShift(shiftId)
}
