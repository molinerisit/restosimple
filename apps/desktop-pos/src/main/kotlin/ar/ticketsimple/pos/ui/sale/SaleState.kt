package ar.ticketsimple.pos.ui.sale

import ar.ticketsimple.pos.domain.catalog.Category
import ar.ticketsimple.pos.domain.catalog.Product
import ar.ticketsimple.pos.domain.payments.PaymentMethod
import ar.ticketsimple.pos.domain.sales.Sale
import ar.ticketsimple.pos.domain.shifts.Shift
import ar.ticketsimple.pos.domain.users.User

data class SaleState(
    val categories: List<Category>        = emptyList(),
    val products: List<Product>           = emptyList(),
    val selectedCategory: Category?       = null,
    val searchQuery: String               = "",
    val currentSale: Sale?                = null,
    val shift: Shift?                     = null,
    val currentUser: User?                = null,
    val isCompactMode: Boolean            = false,
    val showPaymentDialog: Boolean        = false,
    val showDiscountDialog: Boolean       = false,
    val showWeightDialog: Boolean         = false,
    val weightProduct: Product?           = null,
    val isLoading: Boolean                = false,
    val toast: String?                    = null
) {
    val filteredProducts: List<Product>
        get() {
            val base = if (selectedCategory != null)
                products.filter { it.categoryId == selectedCategory.id }
            else products
            return if (searchQuery.isBlank()) base
            else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
}

data class PaymentDialogState(
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    val receivedInput: String         = "",
    val isProcessing: Boolean         = false
)
