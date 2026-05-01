package ar.ticketsimple.pos.infrastructure.db

import ar.ticketsimple.pos.domain.users.Role
import ar.ticketsimple.pos.domain.users.User
import ar.ticketsimple.pos.domain.users.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SqliteUserRepository : UserRepository {

    override fun findByPin(pin: String): User? = transaction {
        UserTable.selectAll()
            .where { (UserTable.pin eq pin) and (UserTable.active eq true) }
            .singleOrNull()?.toUser()
    }

    override fun findById(id: String): User? = transaction {
        UserTable.selectAll().where { UserTable.id eq id }.singleOrNull()?.toUser()
    }

    override fun allActive(): List<User> = transaction {
        UserTable.selectAll().where { UserTable.active eq true }.map { it.toUser() }
    }

    override fun save(user: User) {
        transaction {
            val exists = UserTable.selectAll().where { UserTable.id eq user.id }.count() > 0L
            if (exists) {
                UserTable.update({ UserTable.id eq user.id }) {
                    it[name]   = user.name
                    it[pin]    = user.pin
                    it[role]   = user.role.name
                    it[active] = user.active
                }
            } else {
                UserTable.insert {
                    it[id]     = user.id
                    it[name]   = user.name
                    it[pin]    = user.pin
                    it[role]   = user.role.name
                    it[active] = user.active
                }
            }
        }
    }

    private fun ResultRow.toUser() = User(
        id     = this[UserTable.id],
        name   = this[UserTable.name],
        pin    = this[UserTable.pin],
        role   = Role.valueOf(this[UserTable.role]),
        active = this[UserTable.active]
    )
}
