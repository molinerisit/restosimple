package ar.ticketsimple.pos.infrastructure.db

import ar.ticketsimple.pos.domain.shifts.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SqliteShiftRepository : ShiftRepository {

    override fun save(shift: Shift): Shift = transaction {
        val exists = ShiftTable.selectAll().where { ShiftTable.id eq shift.id }.count() > 0L
        if (exists) {
            ShiftTable.update({ ShiftTable.id eq shift.id }) {
                it[closedAt]    = shift.closedAt
                it[closingCash] = shift.closingCash
                it[notes]       = shift.notes
                it[status]      = shift.status.name.lowercase()
            }
        } else {
            ShiftTable.insert {
                it[id]          = shift.id
                it[userId]      = shift.userId
                it[openedAt]    = shift.openedAt
                it[closedAt]    = shift.closedAt
                it[openingCash] = shift.openingCash
                it[closingCash] = shift.closingCash
                it[notes]       = shift.notes
                it[status]      = shift.status.name.lowercase()
            }
        }
        shift
    }

    override fun findById(id: String): Shift? = transaction {
        ShiftTable.selectAll().where { ShiftTable.id eq id }.singleOrNull()?.let { row ->
            val userName = UserTable.selectAll().where { UserTable.id eq row[ShiftTable.userId] }
                .singleOrNull()?.get(UserTable.name) ?: "Desconocido"
            row.toShift(userName)
        }
    }

    override fun activeShift(): Shift? = transaction {
        ShiftTable.selectAll().where { ShiftTable.status eq "open" }
            .orderBy(ShiftTable.openedAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()?.let { row ->
                val userName = UserTable.selectAll().where { UserTable.id eq row[ShiftTable.userId] }
                    .singleOrNull()?.get(UserTable.name) ?: "Desconocido"
                row.toShift(userName)
            }
    }

    override fun saveCashMovement(movement: CashMovement) {
        transaction {
            CashMovementTable.insert {
                it[id]        = movement.id
                it[shiftId]   = movement.shiftId
                it[type]      = movement.type.name
                it[amount]    = movement.amount
                it[reason]    = movement.reason
                it[createdAt] = movement.createdAt
            }
        }
    }

    override fun movementsByShift(shiftId: String): List<CashMovement> = transaction {
        CashMovementTable.selectAll()
            .where { CashMovementTable.shiftId eq shiftId }
            .orderBy(CashMovementTable.createdAt)
            .map { row ->
                CashMovement(
                    id        = row[CashMovementTable.id],
                    shiftId   = row[CashMovementTable.shiftId],
                    type      = MovementType.valueOf(row[CashMovementTable.type]),
                    amount    = row[CashMovementTable.amount],
                    reason    = row[CashMovementTable.reason],
                    createdAt = row[CashMovementTable.createdAt]
                )
            }
    }

    private fun ResultRow.toShift(userName: String) = Shift(
        id          = this[ShiftTable.id],
        userId      = this[ShiftTable.userId],
        userName    = userName,
        openedAt    = this[ShiftTable.openedAt],
        closedAt    = this[ShiftTable.closedAt],
        openingCash = this[ShiftTable.openingCash],
        closingCash = this[ShiftTable.closingCash],
        notes       = this[ShiftTable.notes],
        status      = if (this[ShiftTable.status] == "open") ShiftStatus.OPEN else ShiftStatus.CLOSED
    )
}
