package ar.ticketsimple.pos.domain.shifts

import java.time.LocalDateTime

enum class ShiftStatus { OPEN, CLOSED }

data class Shift(
    val id: String,
    val userId: String,
    val userName: String,
    val openedAt: LocalDateTime = LocalDateTime.now(),
    val closedAt: LocalDateTime? = null,
    val openingCash: Double = 0.0,
    val closingCash: Double? = null,
    val notes: String? = null,
    val status: ShiftStatus = ShiftStatus.OPEN
)

data class CashMovement(
    val id: String,
    val shiftId: String,
    val type: MovementType,
    val amount: Double,
    val reason: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MovementType { IN, OUT }

interface ShiftRepository {
    fun save(shift: Shift): Shift
    fun findById(id: String): Shift?
    fun activeShift(): Shift?
    fun saveCashMovement(movement: CashMovement)
    fun movementsByShift(shiftId: String): List<CashMovement>
}
