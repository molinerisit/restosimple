package ar.ticketsimple.pos.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.ui.theme.TSColors

@Composable
fun LoginScreen(onLogin: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.width(320.dp)
        ) {
            Text("TicketSimple",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, color = TSColors.Caramel))
            Text("Ingresá tu PIN",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            PinDisplay(pin = pin, error = error)

            if (error) {
                Text("PIN incorrecto. Intentá de nuevo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TSColors.Danger)
            }

            NumPad(
                onDigit  = { d ->
                    error = false
                    if (pin.length < 6) pin += d
                    if (pin.length >= 4) {
                        val ok = onLogin(pin)
                        if (!ok && pin.length >= 6) { error = true; pin = "" }
                        else if (ok) pin = ""
                        else if (!ok && pin.length < 4) {}
                    }
                },
                onDelete = { if (pin.isNotEmpty()) { pin = pin.dropLast(1); error = false } },
                onClear  = { pin = ""; error = false }
            )
        }
    }
}

@Composable
private fun PinDisplay(pin: String, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) { i ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when {
                            error         -> TSColors.Danger
                            i < pin.length -> TSColors.Caramel
                            else          -> MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun NumPad(onDigit: (String) -> Unit, onDelete: () -> Unit, onClear: () -> Unit) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","C","0","⌫")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    when (key) {
                        "⌫" -> OutlinedButton(
                            onClick  = onDelete,
                            modifier = Modifier.size(88.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) { Icon(Icons.Default.Backspace, contentDescription = "Borrar") }
                        "C" -> OutlinedButton(
                            onClick  = onClear,
                            modifier = Modifier.size(88.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) { Text("C", style = MaterialTheme.typography.titleLarge) }
                        else -> Button(
                            onClick  = { onDigit(key) },
                            modifier = Modifier.size(88.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface)
                        ) { Text(key, style = MaterialTheme.typography.headlineLarge) }
                    }
                }
            }
        }
    }
}
