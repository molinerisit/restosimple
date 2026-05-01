package ar.ticketsimple.pos.domain.catalog

data class Product(
    val id: String,
    val sku: String? = null,
    val name: String,
    val categoryId: String,
    val price: Double,
    val available: Boolean = true,
    val byWeight: Boolean = false,
    val imageUrl: String? = null,
    val sortOrder: Int = 0
)
