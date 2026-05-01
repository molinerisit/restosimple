package ar.ticketsimple.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.domain.catalog.Product
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.util.Locale

private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

@Composable
fun ProductTile(
    product: Product,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (product.available) Color.White else Color(0xFFF5F5F5)
    val textColor = if (product.available) TSColors.Ink else Color(0xFF9E9E9E)
    val priceColor = if (product.available) TSColors.Caramel else Color(0xFF9E9E9E)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, if (product.available) Color(0xFFE8E0D8) else Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
            .clickable(enabled = product.available, onClick = onClick)
            .padding(if (compact) 8.dp else 12.dp)
    ) {
        if (compact) {
            CompactTileContent(product, textColor, priceColor)
        } else {
            VisualTileContent(product, textColor, priceColor)
        }
    }
}

@Composable
private fun VisualTileContent(product: Product, textColor: Color, priceColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TSColors.Crema),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = product.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = TSColors.Caramel
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text     = product.name,
            style    = MaterialTheme.typography.labelLarge,
            color    = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        if (product.byWeight) {
            Text("$ por kg", style = MaterialTheme.typography.labelMedium, color = priceColor)
        } else {
            Text(
                text  = currencyFmt.format(product.price),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = priceColor
            )
        }
        if (!product.available) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Sin stock",
                style = MaterialTheme.typography.labelMedium,
                color = TSColors.Danger
            )
        }
    }
}

@Composable
private fun CompactTileContent(product: Product, textColor: Color, priceColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = product.name,
            style    = MaterialTheme.typography.bodyMedium,
            color    = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text  = if (product.byWeight) "x kg" else currencyFmt.format(product.price),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = priceColor
        )
    }
}
