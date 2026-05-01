package ar.ticketsimple.pos.domain.sales

data class SaleItem(
    val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String? = null,
    val voided: Boolean = false
) {
    val lineTotal: Double get() = if (voided) 0.0 else unitPrice * quantity
}
