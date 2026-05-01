package ar.ticketsimple.pos.ui.sale

import ar.ticketsimple.pos.domain.catalog.Category
import ar.ticketsimple.pos.domain.catalog.Product
import ar.ticketsimple.pos.domain.payments.PaymentMethod

sealed interface SaleIntent {
    data class SelectCategory(val category: Category?) : SaleIntent
    data class AddProduct(val product: Product)         : SaleIntent
    data class AddByWeight(val product: Product, val quantity: Double) : SaleIntent
    data class RemoveItem(val itemId: String)           : SaleIntent
    data class VoidItem(val itemId: String)             : SaleIntent
    data class UpdateQuantity(val itemId: String, val delta: Int) : SaleIntent
    data class Search(val query: String)                : SaleIntent
    data class ApplyDiscount(val percent: Double)       : SaleIntent
    data class ApplySurcharge(val percent: Double)      : SaleIntent
    data object OpenPaymentDialog                       : SaleIntent
    data object ClosePaymentDialog                      : SaleIntent
    data class ConfirmPayment(val method: PaymentMethod, val received: Double) : SaleIntent
    data object CancelSale                              : SaleIntent
    data object SuspendSale                             : SaleIntent
    data object NewSale                                 : SaleIntent
    data object ToggleCompactMode                       : SaleIntent
    data object OpenDiscountDialog                      : SaleIntent
    data object CloseDiscountDialog                     : SaleIntent
    data class ShowWeightDialog(val product: Product)   : SaleIntent
    data object CloseWeightDialog                       : SaleIntent
    data class SetAvailability(val productId: String, val available: Boolean) : SaleIntent
    data object DismissToast                            : SaleIntent
}
