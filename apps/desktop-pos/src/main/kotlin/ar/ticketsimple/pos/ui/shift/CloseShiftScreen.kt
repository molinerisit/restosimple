package ar.ticketsimple.pos.ui.shift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.ticketsimple.pos.application.sale.ShiftSummary
import ar.ticketsimple.pos.domain.shifts.Shift
import ar.ticketsimple.pos.ui.theme.TSColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

@Composable
fun CloseShiftScreen(
    shift: Shift,
    summary: ShiftSummary,
    onConfirm: (closingCash: Double, notes: String?) -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var closingCash by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Cierre de caja", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Text("Cajero: ${shift.userName}  |  Turno #${shift.id.takeLast(6).uppercase()}",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        StepIndicator(currentStep = step, totalSteps = 3)

        when (step) {
            0 -> SummaryStep(summary)
            1 -> CountingStep(closingCash, onValueChange = { closingCash = it }, expected = summary.byMethod.getOrDefault(
                ar.ticketsimple.pos.domain.payments.PaymentMethod.CASH, 0.0) + shift.openingCash)
            2 -> ConfirmStep(shift, summary, closingCash, notes, onNotesChange = { notes = it })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text("Atrás")
                }
            }
            if (step < 2) {
                Button(
                    onClick  = { step++ },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = TSColors.Caramel)
                ) { Text("Continuar") }
            } else {
                Button(
                    onClick  = {
                        val cash = closingCash.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onConfirm(cash, notes.ifBlank { null })
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = TSColors.Success)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar turno", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(if (step > 0) 0.5f else 1f).height(52.dp)) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun SummaryStep(summary: ShiftSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Resumen de ventas", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            SummaryRow("Ventas realizadas", "${summary.salesCount}")
            SummaryRow("Total vendido", fmt.format(summary.total))
            SummaryRow("Ticket promedio", fmt.format(summary.avgTicket))
            HorizontalDivider()
            summary.byMethod.forEach { (method, amount) ->
                SummaryRow(method.label, fmt.format(amount))
            }
        }
    }
}

@Composable
private fun CountingStep(value: String, onValueChange: (String) -> Unit, expected: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Conteo de efectivo", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Contá el efectivo en caja y registrá el total:",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text("Efectivo contado") },
            prefix        = { Text("$  ") },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp)
        )
        val counted = value.replace(",", ".").toDoubleOrNull()
        if (counted != null) {
            val diff = counted - expected
            Surface(
                color = if (abs(diff) < 1.0) TSColors.Success10 else TSColors.Danger10,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    SummaryRow("Esperado en caja", fmt.format(expected))
                    SummaryRow("Contado", fmt.format(counted))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow(
                        if (diff >= 0) "Sobrante" else "Faltante",
                        fmt.format(abs(diff)),
                        color = if (abs(diff) < 1.0) TSColors.Success else TSColors.Danger
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmStep(shift: Shift, summary: ShiftSummary, closingCash: String, notes: String, onNotesChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Confirmar cierre", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow("Turno", "#${shift.id.takeLast(6).uppercase()}")
                SummaryRow("Ventas", "${summary.salesCount}")
                SummaryRow("Total", fmt.format(summary.total))
                SummaryRow("Efectivo en caja", closingCash.ifBlank { "0" })
            }
        }
        OutlinedTextField(
            value         = notes,
            onValueChange = onNotesChange,
            label         = { Text("Observaciones (opcional)") },
            minLines      = 3,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp)
        )
        Text("Esta acción no se puede deshacer.",
            style = MaterialTheme.typography.bodyMedium,
            color = TSColors.Danger)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(totalSteps) { i ->
            Surface(
                shape = RoundedCornerShape(50),
                color = if (i <= currentStep) TSColors.Caramel else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(width = if (i == currentStep) 32.dp else 8.dp, height = 8.dp)
            ) {}
        }
        Text("Paso ${currentStep + 1} de $totalSteps",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp))
    }
}
