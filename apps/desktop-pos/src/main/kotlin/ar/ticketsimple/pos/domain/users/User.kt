package ar.ticketsimple.pos.domain.users

enum class Role(val label: String) {
    CASHIER("Cajero"),
    WAITER("Camarero"),
    KITCHEN("Cocina"),
    MANAGER("Gerente"),
    ADMIN("Admin"),
    OWNER("Dueño")
}

data class User(
    val id: String,
    val name: String,
    val pin: String,
    val role: Role,
    val active: Boolean = true
) {
    fun canVoidItem(): Boolean = role in listOf(Role.MANAGER, Role.ADMIN, Role.OWNER)
    fun canOpenShift(): Boolean = role in listOf(Role.CASHIER, Role.MANAGER, Role.ADMIN, Role.OWNER)
    fun canCloseShift(): Boolean = role in listOf(Role.CASHIER, Role.MANAGER, Role.ADMIN, Role.OWNER)
    fun canApplyDiscount(): Boolean = role in listOf(Role.MANAGER, Role.ADMIN, Role.OWNER)
    fun canAdjustCash(): Boolean = role in listOf(Role.MANAGER, Role.ADMIN, Role.OWNER)
    fun canManageCatalog(): Boolean = role in listOf(Role.ADMIN, Role.OWNER)
}

interface UserRepository {
    fun findByPin(pin: String): User?
    fun findById(id: String): User?
    fun allActive(): List<User>
    fun save(user: User)
}
