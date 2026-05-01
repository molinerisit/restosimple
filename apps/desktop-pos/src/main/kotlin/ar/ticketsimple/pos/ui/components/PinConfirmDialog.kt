package ar.ticketsimple.pos.ui.components

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
fun PinConfirmDialog(
    title: String = "Requiere autorización",
    subtitle: String = "Ingresá el PIN de gerente o admin",
    onConfirm: (pin: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { i ->
                        Box(
                            modifier = Modifier.size(14.dp).background(
                                color = when {
                                    error        -> TSColors.Danger
                                    i < pin.length -> TSColors.Caramel
                                    else         -> MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(50)
                            )
                        )
                    }
                }
                if (error) Text("PIN incorrecto", style = MaterialTheme.typography.bodyMedium, color = TSColors.Danger)

                val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    keys.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { key ->
                                when (key) {
                                    "" -> Spacer(Modifier.size(64.dp))
                                    "⌫" -> OutlinedButton(onClick = { if (pin.isNotEmpty()) { pin = pin.dropLast(1); error = false } }, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(0.dp)) {
                                        Icon(Icons.Default.Backspace, contentDescription = "Borrar", modifier = Modifier.size(18.dp))
                                    }
                                    else -> Button(onClick = {
                                        error = false
                                        if (pin.length < 6) pin += key
                                    }, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                        contentPadding = PaddingValues(0.dp)) {
                                        Text(key, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pin) },
                enabled = pin.length >= 4,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TSColors.Caramel)
            ) { Text("Confirmar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    )
}
