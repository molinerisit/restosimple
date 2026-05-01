package ar.ticketsimple.pos.infrastructure.db

import ar.ticketsimple.pos.domain.audit.AuditAction
import ar.ticketsimple.pos.domain.audit.AuditEvent
import ar.ticketsimple.pos.domain.audit.AuditRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SqliteAuditRepository : AuditRepository {

    override fun record(event: AuditEvent) {
        transaction {
            AuditTable.insert {
                it[id]        = event.id
                it[action]    = event.action.name
                it[entityId]  = event.entityId
                it[userId]    = event.userId
                it[data]      = event.data
                it[timestamp] = event.timestamp
            }
        }
    }

    override fun eventsForEntity(entityId: String): List<AuditEvent> = transaction {
        AuditTable.selectAll()
            .where { AuditTable.entityId eq entityId }
            .orderBy(AuditTable.timestamp, SortOrder.DESC)
            .map { row ->
                AuditEvent(
                    id        = row[AuditTable.id],
                    action    = AuditAction.valueOf(row[AuditTable.action]),
                    entityId  = row[AuditTable.entityId],
                    userId    = row[AuditTable.userId],
                    data      = row[AuditTable.data],
                    timestamp = row[AuditTable.timestamp]
                )
            }
    }
}
