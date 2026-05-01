package ar.ticketsimple.pos.ui.sale

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.domain.catalog.Category
import ar.ticketsimple.pos.ui.components.OrderRow
import ar.ticketsimple.pos.ui.components.ProductTile
import ar.ticketsimple.pos.ui.components.TSSearchBar
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.util.Locale

private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

@Composable
fun SaleScreen(viewModel: SaleViewModel) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.F2) {
            // F2 focuses search — handled in SearchBar's FocusRequester
            false
        } else false
    }) {
        Column(Modifier.fillMaxSize()) {
            SaleTopBar(state, onToggleCompact = { viewModel.onIntent(SaleIntent.ToggleCompactMode) })
            HorizontalDivider()
            Row(Modifier.weight(1f)) {
                LeftPanel(
                    state    = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.weight(0.62f)
                )
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                OrderPanel(
                    state    = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.weight(0.38f)
                )
            }
        }

        if (state.showPaymentDialog) {
            PaymentDialog(state, onIntent = viewModel::onIntent)
        }
        if (state.showDiscountDialog) {
            DiscountDialog(
                currentDiscount = state.currentSale?.discountPercent ?: 0.0,
                onApply  = { viewModel.onIntent(SaleIntent.ApplyDiscount(it)) },
                onDismiss = { viewModel.onIntent(SaleIntent.CloseDiscountDialog) }
            )
        }
        if (state.showWeightDialog && state.weightProduct != null) {
            WeightInputDialog(
                product  = state.weightProduct!!,
                onConfirm = { qty -> viewModel.onIntent(SaleIntent.AddByWeight(state.weightProduct!!, qty)) },
                onDismiss = { viewModel.onIntent(SaleIntent.CloseWeightDialog) }
            )
        }

        state.toast?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onIntent(SaleIntent.DismissToast) }) { Text("OK") }
                }
            ) { Text(msg) }
        }
    }
}

@Composable
private fun SaleTopBar(state: SaleState, onToggleCompact: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("TicketSimple", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TSColors.Caramel))
        Spacer(Modifier.width(16.dp))
        if (state.shift != null) {
            Chip(label = "Turno #${state.shift.id.takeLast(4).uppercase()}")
            Spacer(Modifier.width(8.dp))
            Chip(label = state.shift.userName)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onToggleCompact) {
            Icon(
                if (state.isCompactMode) Icons.Default.GridView else Icons.Default.ViewList,
                contentDescription = "Modo ${if (state.isCompactMode) "visual" else "compacto"}",
                tint = TSColors.Caramel
            )
        }
        Icon(Icons.Default.Circle, contentDescription = "Estado", tint = TSColors.Success, modifier = Modifier.size(10.dp))
        Spacer(Modifier.width(4.dp))
        Text("Conectado", style = MaterialTheme.typography.labelMedium, color = TSColors.Success)
    }
}

@Composable
private fun Chip(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LeftPanel(state: SaleState, onIntent: (SaleIntent) -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxHeight().padding(12.dp)) {
        TSSearchBar(
            query         = state.searchQuery,
            onQueryChange = { onIntent(SaleIntent.Search(it)) }
        )
        Spacer(Modifier.height(10.dp))
        CategoryRow(
            categories       = state.categories,
            selectedCategory = state.selectedCategory,
            onSelect         = { onIntent(SaleIntent.SelectCategory(it)) }
        )
        Spacer(Modifier.height(10.dp))

        val cols = if (state.isCompactMode) 1 else GridCells.Adaptive(140.dp)
        val products = state.filteredProducts

        if (products.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin productos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (state.isCompactMode) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(products, key = { it.id }) { product ->
                    ProductTile(product, compact = true, onClick = { onIntent(SaleIntent.AddProduct(product)) })
                }
            }
        } else {
            LazyVerticalGrid(
                columns             = GridCells.Adaptive(140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductTile(product, compact = false, onClick = { onIntent(SaleIntent.AddProduct(product)) })
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelect: (Category?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            FilterChip(
                selected  = selectedCategory == null,
                onClick   = { onSelect(null) },
                label     = { Text("Todo") }
            )
        }
        items(categories, key = { it.id }) { cat ->
            FilterChip(
                selected  = cat.id == selectedCategory?.id,
                onClick   = { onSelect(cat) },
                label     = { Text(cat.name) }
            )
        }
    }
}

@Composable
private fun OrderPanel(state: SaleState, onIntent: (SaleIntent) -> Unit, modifier: Modifier) {
    val sale = state.currentSale

    Column(modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = if (sale != null) "Orden #${sale.id.takeLast(6).uppercase()}" else "Nueva venta",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.weight(1f))
            if (sale != null) {
                Text("${sale.itemCount} ítem${if (sale.itemCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (sale == null || sale.items.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Seleccioná un producto", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(sale.items.filter { !it.voided }, key = { it.id }) { item ->
                    OrderRow(
                        item        = item,
                        onIncrement = { onIntent(SaleIntent.UpdateQuantity(item.id, 1)) },
                        onDecrement = { onIntent(SaleIntent.UpdateQuantity(item.id, -1)) },
                        onVoid      = { onIntent(SaleIntent.VoidItem(item.id)) }
                    )
                }
            }

            HorizontalDivider()

            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                TotalRow("Subtotal", sale.subtotal)
                if (sale.discountPercent > 0)
                    TotalRow("Descuento ${sale.discountPercent.toInt()}%", -sale.discountAmount, color = TSColors.Success)
                if (sale.surchargePercent > 0)
                    TotalRow("Recargo ${sale.surchargePercent.toInt()}%", sale.surchargeAmount, color = TSColors.Warning)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(fmt.format(sale.total), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TSColors.Caramel))
                }
            }
        }

        HorizontalDivider()
        QuickActionsRow(sale != null, onIntent)

        Button(
            onClick  = { onIntent(SaleIntent.OpenPaymentDialog) },
            enabled  = sale != null && sale.items.any { !it.voided },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = TSColors.Caramel)
        ) {
            Icon(Icons.Default.Payment, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text  = if (sale != null) "Cobrar  ${fmt.format(sale.total)}" else "Cobrar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: Double, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(fmt.format(amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}

@Composable
private fun QuickActionsRow(hasSale: Boolean, onIntent: (SaleIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedButton(
            onClick  = { onIntent(SaleIntent.OpenDiscountDialog) },
            enabled  = hasSale,
            modifier = Modifier.weight(1f).height(40.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) { Text("Descuento", style = MaterialTheme.typography.labelMedium) }

        OutlinedButton(
            onClick  = { onIntent(SaleIntent.SuspendSale) },
            enabled  = hasSale,
            modifier = Modifier.weight(1f).height(40.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) { Text("Suspender", style = MaterialTheme.typography.labelMedium) }

        OutlinedButton(
            onClick  = { onIntent(SaleIntent.CancelSale) },
            enabled  = hasSale,
            modifier = Modifier.weight(1f).height(40.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = TSColors.Danger),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) { Text("Anular", style = MaterialTheme.typography.labelMedium) }
    }
}
