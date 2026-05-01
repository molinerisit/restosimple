package ar.ticketsimple.pos.domain.catalog

data class Category(
    val id: String,
    val name: String,
    val color: String = "#C46A3A",
    val sortOrder: Int = 0,
    val active: Boolean = true
)
