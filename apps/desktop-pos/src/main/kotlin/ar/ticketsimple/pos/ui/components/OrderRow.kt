package ar.ticketsimple.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.domain.sales.SaleItem
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.util.Locale

private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

@Composable
fun OrderRow(
    item: SaleItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onVoid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textDecoration = if (item.voided) TextDecoration.LineThrough else TextDecoration.None
    val alpha = if (item.voided) 0.4f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text           = item.productName,
                style          = MaterialTheme.typography.bodyMedium.copy(textDecoration = textDecoration),
                color          = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines       = 1,
                overflow       = TextOverflow.Ellipsis
            )
            Text(
                text  = currencyFmt.format(item.unitPrice) + " c/u",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }

        if (!item.voided) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallIconButton(onClick = onDecrement, icon = Icons.Default.Remove, description = "Quitar")
                Text(
                    text  = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                SmallIconButton(onClick = onIncrement, icon = Icons.Default.Add, description = "Agregar")
            }
        }

        Spacer(Modifier.width(8.dp))
        Text(
            text  = currencyFmt.format(item.lineTotal),
            style = MaterialTheme.typography.titleMedium.copy(textDecoration = textDecoration),
            color = TSColors.Caramel.copy(alpha = alpha)
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SmallIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(14.dp))
    }
}
