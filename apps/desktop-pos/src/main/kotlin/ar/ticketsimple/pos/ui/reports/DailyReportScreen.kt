package ar.ticketsimple.pos.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.application.sale.SaleService
import ar.ticketsimple.pos.application.sale.ShiftSummary
import ar.ticketsimple.pos.domain.payments.PaymentMethod
import ar.ticketsimple.pos.domain.sales.SaleStatus
import ar.ticketsimple.pos.domain.shifts.Shift
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun DailyReportScreen(
    shift: Shift,
    saleService: SaleService,
    onBack: () -> Unit
) {
    val summary = remember { saleService.salesSummaryForShift(shift.id) }
    val topProducts = remember { saleService.topProductsForShift(shift.id, limit = 5) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Reporte del turno", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
                Text("${shift.userName}  ·  ${shift.openedAt.format(timeFmt)} → ${shift.closedAt?.format(timeFmt) ?: "Activo"}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Total vendido", fmt.format(summary.total), Icons.Default.AttachMoney, TSColors.Caramel, Modifier.weight(1f))
            StatCard("Ventas", "${summary.salesCount}", Icons.Default.Receipt, TSColors.Success, Modifier.weight(1f))
            StatCard("Ticket promedio", fmt.format(summary.avgTicket), Icons.Default.TrendingUp, TSColors.Info, Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ventas por método de pago", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                PaymentMethod.entries.forEach { method ->
                    val amount = summary.byMethod[method] ?: 0.0
                    if (amount > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = TSColors.Caramel10, shape = RoundedCornerShape(6.dp)) {
                                    Icon(paymentIcon(method), contentDescription = null, tint = TSColors.Caramel,
                                        modifier = Modifier.padding(6.dp).size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(method.label, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(fmt.format(amount), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
                if (summary.byMethod.values.all { it == 0.0 }) {
                    Text("Sin ventas registradas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (topProducts.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Productos más vendidos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    topProducts.forEachIndexed { i, (name, qty, total) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = rankColor(i).copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
                                    Text("${i + 1}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = rankColor(i)))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text("$qty unidades", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(fmt.format(total), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                        if (i < topProducts.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Efectivo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                ReportRow("Fondo inicial", fmt.format(shift.openingCash))
                ReportRow("Ventas en efectivo", fmt.format(summary.byMethod[PaymentMethod.CASH] ?: 0.0))
                HorizontalDivider()
                ReportRow("Esperado en caja",
                    fmt.format(shift.openingCash + (summary.byMethod[PaymentMethod.CASH] ?: 0.0)),
                    color = TSColors.Caramel)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = color))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}

private fun paymentIcon(method: PaymentMethod): ImageVector = when (method) {
    PaymentMethod.CASH     -> Icons.Default.Payments
    PaymentMethod.DEBIT    -> Icons.Default.CreditCard
    PaymentMethod.CREDIT   -> Icons.Default.CreditCard
    PaymentMethod.QR       -> Icons.Default.QrCode
    PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
    PaymentMethod.OTHER    -> Icons.Default.MoreHoriz
}

private fun rankColor(index: Int): Color = when (index) {
    0    -> Color(0xFFD97706)
    1    -> Color(0xFF9E9E9E)
    2    -> Color(0xFFC46A3A)
    else -> Color(0xFF7F9B7A)
}

