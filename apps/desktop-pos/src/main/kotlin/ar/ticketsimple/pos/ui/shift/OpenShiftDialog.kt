package ar.ticketsimple.pos.ui.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.application.shifts.ShiftService
import ar.ticketsimple.pos.domain.users.User
import ar.ticketsimple.pos.ui.theme.TSColors

@Composable
fun OpenShiftDialog(
    user: User,
    shiftService: ShiftService,
    onShiftOpened: () -> Unit
) {
    var openingCash by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Column {
                Text("Abrir turno de caja", style = MaterialTheme.typography.headlineMedium)
                Text("Cajero: ${user.name}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ingresá el fondo inicial de caja:",
                    style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value         = openingCash,
                    onValueChange = { openingCash = it },
                    label         = { Text("Fondo inicial") },
                    prefix        = { Text("$  ") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
                Text("Si no hay fondo inicial, dejá en 0.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    val cash = openingCash.replace(",", ".").toDoubleOrNull() ?: 0.0
                    shiftService.openShift(user.id, user.name, cash)
                    onShiftOpened()
                },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = TSColors.Caramel)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Abrir turno", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    )
}
