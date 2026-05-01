package ar.ticketsimple.pos.ui.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.domain.shifts.Shift
import ar.ticketsimple.pos.ui.theme.TSColors

@Composable
fun ManagerPanel(
    shift: Shift?,
    onCashIn: (amount: Double, reason: String) -> Unit,
    onCashOut: (amount: Double, reason: String) -> Unit,
    onOpenReports: () -> Unit,
    onCloseShift: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCashDialog by remember { mutableStateOf<CashDialogType?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = TSColors.Caramel)
                Spacer(Modifier.width(8.dp))
                Text("Panel de gerente", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (shift != null) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Turno activo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${shift.userName}  ·  #${shift.id.takeLast(6).uppercase()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                ManagerAction(Icons.Default.ArrowDownward, "Ingreso de efectivo", "Registrar entrada de caja", TSColors.Success) { showCashDialog = CashDialogType.IN }
                ManagerAction(Icons.Default.ArrowUpward, "Egreso de efectivo", "Registrar retiro de caja", TSColors.Warning) { showCashDialog = CashDialogType.OUT }
                ManagerAction(Icons.Default.BarChart, "Reportes del día", "Ver ventas y resumen del turno", TSColors.Info) { onOpenReports(); onDismiss() }
                HorizontalDivider()
                ManagerAction(Icons.Default.LockClock, "Cerrar turno", "Finalizar caja y generar resumen", TSColors.Danger) { onCloseShift(); onDismiss() }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
        }
    )

    showCashDialog?.let { type ->
        CashMovementDialog(
            type     = type,
            onConfirm = { amount, reason ->
                if (type == CashDialogType.IN) onCashIn(amount, reason) else onCashOut(amount, reason)
                showCashDialog = null
            },
            onDismiss = { showCashDialog = null }
        )
    }
}

@Composable
private fun ManagerAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick    = onClick,
        shape      = RoundedCornerShape(12.dp),
        color      = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier   = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = accentColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

enum class CashDialogType { IN, OUT }

@Composable
private fun CashMovementDialog(type: CashDialogType, onConfirm: (Double, String) -> Unit, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val isIn = type == CashDialogType.IN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isIn) "Ingreso de efectivo" else "Egreso de efectivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Monto") }, prefix = { Text("$  ") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("Motivo") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.replace(",", ".").toDoubleOrNull()?.let { onConfirm(it, reason.ifBlank { if (isIn) "Ingreso" else "Egreso" }) } },
                enabled = amount.replace(",", ".").toDoubleOrNull() != null && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIn) TSColors.Success else TSColors.Warning)
            ) { Text(if (isIn) "Registrar ingreso" else "Registrar egreso") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
