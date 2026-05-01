package ar.ticketsimple.pos.ui.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.domain.payments.PaymentMethod
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.util.Locale

private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

@Composable
fun PaymentDialog(state: SaleState, onIntent: (SaleIntent) -> Unit) {
    val sale = state.currentSale ?: return
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var receivedInput by remember { mutableStateOf("") }

    val received = receivedInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val change   = if (selectedMethod == PaymentMethod.CASH) (received - sale.total).coerceAtLeast(0.0) else 0.0

    AlertDialog(
        onDismissRequest = { onIntent(SaleIntent.ClosePaymentDialog) },
        title = {
            Column {
                Text("Cobrar venta", style = MaterialTheme.typography.headlineMedium)
                Text(fmt.format(sale.total),
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = TSColors.Caramel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Forma de pago", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    PaymentMethod.entries.take(4).forEach { method ->
                        FilterChip(
                            selected  = selectedMethod == method,
                            onClick   = { selectedMethod = method; if (method != PaymentMethod.CASH) receivedInput = "" },
                            label     = { Text(method.label, style = MaterialTheme.typography.labelMedium) },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.drop(4).forEach { method ->
                        FilterChip(
                            selected  = selectedMethod == method,
                            onClick   = { selectedMethod = method; receivedInput = "" },
                            label     = { Text(method.label, style = MaterialTheme.typography.labelMedium) },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                }

                if (selectedMethod == PaymentMethod.CASH) {
                    OutlinedTextField(
                        value         = receivedInput,
                        onValueChange = { receivedInput = it },
                        label         = { Text("Efectivo recibido") },
                        prefix        = { Text("$  ") },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp)
                    )
                    if (received > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vuelto:", style = MaterialTheme.typography.titleMedium)
                            Text(fmt.format(change),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (change >= 0) TSColors.Success else TSColors.Danger
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(500.0, 1000.0, 2000.0, 5000.0).forEach { amount ->
                            SuggestionChip(
                                onClick = { receivedInput = amount.toInt().toString() },
                                label   = { Text("$${amount.toInt()}") }
                            )
                        }
                        SuggestionChip(
                            onClick = { receivedInput = sale.total.toInt().toString() },
                            label   = { Text("Exacto") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val rcv = if (selectedMethod == PaymentMethod.CASH) received else sale.total
                    onIntent(SaleIntent.ConfirmPayment(selectedMethod, rcv))
                },
                enabled  = selectedMethod != PaymentMethod.CASH || received >= sale.total,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = TSColors.Caramel)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Confirmar cobro", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onIntent(SaleIntent.ClosePaymentDialog) }) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DiscountDialog(currentDiscount: Double, onApply: (Double) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toInt().toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Aplicar descuento") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = input,
                    onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) input = it },
                    label         = { Text("Descuento (%)") },
                    suffix        = { Text("%") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 20).forEach { pct ->
                        SuggestionChip(onClick = { input = pct.toString() }, label = { Text("$pct%") })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(input.toDoubleOrNull() ?: 0.0) }) { Text("Aplicar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun WeightInputDialog(product: ar.ticketsimple.pos.domain.catalog.Product, onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(product.name) },
        text    = {
            OutlinedTextField(
                value         = input,
                onValueChange = { input = it },
                label         = { Text("Cantidad (kg)") },
                suffix        = { Text("kg") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick  = { input.replace(",", ".").toDoubleOrNull()?.let(onConfirm) },
                enabled  = input.replace(",", ".").toDoubleOrNull() != null
            ) { Text("Agregar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
