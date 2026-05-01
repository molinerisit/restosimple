package ar.ticketsimple.pos.ui.sale

import ar.ticketsimple.pos.application.catalog.CatalogService
import ar.ticketsimple.pos.application.sale.SaleService
import ar.ticketsimple.pos.application.shifts.ShiftService
import ar.ticketsimple.pos.domain.catalog.Product
import ar.ticketsimple.pos.domain.sales.SaleItem
import ar.ticketsimple.pos.domain.users.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SaleViewModel(
    private val saleService: SaleService,
    private val catalogService: CatalogService,
    private val shiftService: ShiftService,
    private val userRepo: UserRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _state = MutableStateFlow(SaleState())
    val state: StateFlow<SaleState> = _state.asStateFlow()

    init { loadInitial() }

    private fun loadInitial() {
        scope.launch {
            val categories = catalogService.categories()
            val products   = catalogService.products()
            val shift      = shiftService.activeShift()
            _state.update { it.copy(categories = categories, products = products, shift = shift) }
        }
    }

    fun onIntent(intent: SaleIntent) {
        when (intent) {
            is SaleIntent.SelectCategory   -> _state.update { it.copy(selectedCategory = intent.category, searchQuery = "") }
            is SaleIntent.Search           -> _state.update { it.copy(searchQuery = intent.query, selectedCategory = null) }
            is SaleIntent.AddProduct       -> addProduct(intent.product)
            is SaleIntent.AddByWeight      -> addByWeight(intent.product, intent.quantity)
            is SaleIntent.RemoveItem       -> removeItem(intent.itemId)
            is SaleIntent.VoidItem         -> voidItem(intent.itemId)
            is SaleIntent.UpdateQuantity   -> updateQuantity(intent.itemId, intent.delta)
            is SaleIntent.ApplyDiscount    -> applyDiscount(intent.percent)
            is SaleIntent.ApplySurcharge   -> applySurcharge(intent.percent)
            is SaleIntent.OpenPaymentDialog  -> _state.update { it.copy(showPaymentDialog = true) }
            is SaleIntent.ClosePaymentDialog -> _state.update { it.copy(showPaymentDialog = false) }
            is SaleIntent.ConfirmPayment   -> confirmPayment(intent.method, intent.received)
            is SaleIntent.CancelSale       -> cancelSale()
            is SaleIntent.SuspendSale      -> suspendSale()
            is SaleIntent.NewSale          -> startNewSale()
            is SaleIntent.ToggleCompactMode -> _state.update { it.copy(isCompactMode = !it.isCompactMode) }
            is SaleIntent.OpenDiscountDialog  -> _state.update { it.copy(showDiscountDialog = true) }
            is SaleIntent.CloseDiscountDialog -> _state.update { it.copy(showDiscountDialog = false) }
            is SaleIntent.ShowWeightDialog -> _state.update { it.copy(showWeightDialog = true, weightProduct = intent.product) }
            is SaleIntent.CloseWeightDialog -> _state.update { it.copy(showWeightDialog = false, weightProduct = null) }
            is SaleIntent.SetAvailability  -> scope.launch { catalogService.setAvailability(intent.productId, intent.available); loadInitial() }
            is SaleIntent.DismissToast     -> _state.update { it.copy(toast = null) }
            is SaleIntent.SendToKitchen    -> sendToKitchen()
            is SaleIntent.OpenManagerPanel -> _state.update { it.copy(showManagerPanel = true) }
            is SaleIntent.CloseManagerPanel -> _state.update { it.copy(showManagerPanel = false) }
            is SaleIntent.CashIn           -> cashIn(intent.amount, intent.reason)
            is SaleIntent.CashOut          -> cashOut(intent.amount, intent.reason)
            is SaleIntent.OpenReports      -> _state.update { it.copy(showReports = true, showManagerPanel = false) }
            is SaleIntent.CloseReports     -> _state.update { it.copy(showReports = false) }
            is SaleIntent.RequestVoidItem  -> requestVoid(intent.itemId)
            is SaleIntent.ConfirmVoidWithPin -> confirmVoidWithPin(intent.itemId, intent.pin)
            is SaleIntent.CancelPinConfirm -> _state.update { it.copy(showPinConfirm = false, pendingVoidItemId = null) }
            is SaleIntent.RequestCloseShift -> _state.update { it.copy(showManagerPanel = false, requestCloseShift = true) }
            is SaleIntent.AcknowledgeCloseShift -> _state.update { it.copy(requestCloseShift = false) }
            is SaleIntent.Backup           -> doBackup()
        }
    }

    private fun addProduct(product: Product) {
        if (!product.available) { _state.update { it.copy(toast = "${product.name} no disponible") }; return }
        if (product.byWeight) { _state.update { it.copy(showWeightDialog = true, weightProduct = product) }; return }

        val sale = ensureSale()
        val existingIdx = sale.items.indexOfFirst { it.productId == product.id && !it.voided }
        val updatedItems = if (existingIdx >= 0) {
            sale.items.toMutableList().also {
                it[existingIdx] = it[existingIdx].copy(quantity = it[existingIdx].quantity + 1)
            }
        } else {
            sale.items + SaleItem(UUID.randomUUID().toString(), sale.id, product.id, product.name, 1, product.price)
        }
        val updated = sale.copy(items = updatedItems)
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated) }
    }

    private fun addByWeight(product: Product, quantity: Double) {
        val sale = ensureSale()
        val item = SaleItem(UUID.randomUUID().toString(), sale.id, product.id,
            "${product.name} (${quantity}kg)", 1, product.price * quantity)
        val updated = sale.copy(items = sale.items + item)
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated, showWeightDialog = false, weightProduct = null) }
    }

    private fun removeItem(itemId: String) {
        val sale = _state.value.currentSale ?: return
        val updated = sale.copy(items = sale.items.filter { it.id != itemId })
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated) }
    }

    private fun voidItem(itemId: String) {
        val sale = _state.value.currentSale ?: return
        val updated = sale.copy(items = sale.items.map { if (it.id == itemId) it.copy(voided = true) else it })
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated) }
    }

    private fun requestVoid(itemId: String) {
        val user = _state.value.currentUser ?: return
        if (user.canVoidItem()) {
            voidItem(itemId)
        } else {
            _state.update { it.copy(showPinConfirm = true, pendingVoidItemId = itemId) }
        }
    }

    private fun confirmVoidWithPin(itemId: String, pin: String) {
        val authUser = userRepo.findByPin(pin)
        if (authUser != null && authUser.canVoidItem()) {
            voidItem(itemId)
            _state.update { it.copy(showPinConfirm = false, pendingVoidItemId = null, toast = "Ítem anulado por ${authUser.name}") }
        } else {
            _state.update { it.copy(toast = "PIN incorrecto o sin permisos") }
        }
    }

    private fun sendToKitchen() {
        val sale = _state.value.currentSale ?: return
        scope.launch { saleService.sendToKitchen(sale, "TicketSimple") }
        _state.update { it.copy(toast = "Enviado a cocina") }
    }

    private fun cashIn(amount: Double, reason: String) {
        val shift = _state.value.shift ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        scope.launch { shiftService.addCashIn(shift.id, userId, amount, reason) }
        _state.update { it.copy(showManagerPanel = false, toast = "Ingreso registrado") }
    }

    private fun cashOut(amount: Double, reason: String) {
        val shift = _state.value.shift ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        scope.launch { shiftService.addCashOut(shift.id, userId, amount, reason) }
        _state.update { it.copy(showManagerPanel = false, toast = "Egreso registrado") }
    }

    private fun doBackup() {
        scope.launch {
            val path = ar.ticketsimple.pos.infrastructure.backup.BackupService.backup()
            _state.update { it.copy(toast = if (path != null) "Backup creado" else "Error al crear backup", lastBackupPath = path) }
        }
    }

    private fun updateQuantity(itemId: String, delta: Int) {
        val sale = _state.value.currentSale ?: return
        val updatedItems = sale.items.map { item ->
            if (item.id == itemId) {
                val newQty = item.quantity + delta
                if (newQty <= 0) item.copy(voided = true) else item.copy(quantity = newQty)
            } else item
        }
        val updated = sale.copy(items = updatedItems)
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated) }
    }

    private fun applyDiscount(percent: Double) {
        val sale = _state.value.currentSale ?: return
        val updated = sale.copy(discountPercent = percent)
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated, showDiscountDialog = false) }
    }

    private fun applySurcharge(percent: Double) {
        val sale = _state.value.currentSale ?: return
        val updated = sale.copy(surchargePercent = percent)
        scope.launch { saleService.save(updated) }
        _state.update { it.copy(currentSale = updated) }
    }

    private fun confirmPayment(method: ar.ticketsimple.pos.domain.payments.PaymentMethod, received: Double) {
        val sale = _state.value.currentSale ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        scope.launch {
            try {
                saleService.processPayment(sale, method, received, userId)
                _state.update { it.copy(currentSale = null, showPaymentDialog = false, toast = "Venta procesada") }
                startNewSale()
            } catch (e: Exception) {
                _state.update { it.copy(toast = "Error al procesar pago: ${e.message}") }
            }
        }
    }

    private fun cancelSale() {
        val sale = _state.value.currentSale ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        scope.launch { saleService.voidSale(sale, userId) }
        _state.update { it.copy(currentSale = null) }
    }

    private fun suspendSale() {
        val sale = _state.value.currentSale ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        scope.launch { saleService.suspendSale(sale, userId) }
        _state.update { it.copy(currentSale = null, toast = "Venta suspendida") }
    }

    private fun startNewSale() {
        val shift = _state.value.shift ?: return
        val userId = _state.value.currentUser?.id ?: "system"
        val sale = saleService.newSale(shift.id, userId)
        _state.update { it.copy(currentSale = sale) }
    }

    private fun ensureSale(): ar.ticketsimple.pos.domain.sales.Sale {
        val existing = _state.value.currentSale
        if (existing != null) return existing
        val shift = _state.value.shift ?: error("No hay turno activo")
        val userId = _state.value.currentUser?.id ?: "system"
        val newSale = saleService.newSale(shift.id, userId)
        _state.update { it.copy(currentSale = newSale) }
        return newSale
    }

    fun loginUser(pin: String): Boolean {
        val user = userRepo.findByPin(pin) ?: return false
        _state.update { it.copy(currentUser = user) }
        loadInitial()
        return true
    }

    fun refreshAfterShiftOpen() { loadInitial() }

    fun destroy() { scope.cancel() }
}
