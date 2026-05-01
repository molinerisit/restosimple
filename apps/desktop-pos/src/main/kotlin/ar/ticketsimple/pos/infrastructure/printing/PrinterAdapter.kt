package ar.ticketsimple.pos.infrastructure.printing

import ar.ticketsimple.pos.domain.payments.Payment
import ar.ticketsimple.pos.domain.sales.Sale
import ar.ticketsimple.pos.domain.shifts.Shift
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.net.Socket
import java.text.NumberFormat
import java.util.Locale

interface PrinterPort {
    fun printTicket(sale: Sale, payment: Payment, businessName: String)
    fun printKitchenTicket(sale: Sale, businessName: String)
    fun printShiftSummary(shift: Shift, totalSales: Double, salesCount: Int)
    fun openCashDrawer()
}

class ConsolePrinterAdapter : PrinterPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun printTicket(sale: Sale, payment: Payment, businessName: String) {
        log.info("=== TICKET ===")
        log.info(businessName)
        log.info("Venta #${sale.id.takeLast(6).uppercase()}")
        sale.items.filter { !it.voided }.forEach { item ->
            log.info("  ${item.quantity}x ${item.productName}  ${item.lineTotal.currency()}")
        }
        if (sale.discountPercent > 0) log.info("  Descuento ${sale.discountPercent}%  -${sale.discountAmount.currency()}")
        log.info("  TOTAL: ${sale.total.currency()}")
        log.info("  Pagado con: ${payment.method.label}")
        if (payment.change > 0) log.info("  Vuelto: ${payment.change.currency()}")
        log.info("==============")
    }

    override fun printKitchenTicket(sale: Sale, businessName: String) {
        log.info("=== COCINA ===")
        log.info("Comanda #${sale.id.takeLast(6).uppercase()}")
        sale.items.filter { !it.voided }.forEach { item ->
            log.info("  ${item.quantity}x ${item.productName}${if (item.notes != null) " (${item.notes})" else ""}")
        }
        log.info("==============")
    }

    override fun printShiftSummary(shift: Shift, totalSales: Double, salesCount: Int) {
        log.info("=== CIERRE DE CAJA ===")
        log.info("Cajero: ${shift.userName}")
        log.info("Ventas: $salesCount  Total: ${totalSales.currency()}")
        log.info("======================")
    }

    override fun openCashDrawer() {
        log.info("[Cajón abierto]")
    }
}

class NetworkEscPosPrinterAdapter(private val host: String, private val port: Int = 9100) : PrinterPort {
    private val log = LoggerFactory.getLogger(javaClass)
    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    override fun printTicket(sale: Sale, payment: Payment, businessName: String) {
        sendRaw(buildTicket(sale, payment, businessName))
    }

    override fun printKitchenTicket(sale: Sale, businessName: String) {
        val text = buildString {
            appendLine(center("COCINA", 42))
            appendLine("Comanda #${sale.id.takeLast(6).uppercase()}")
            appendLine(divider())
            sale.items.filter { !it.voided }.forEach { item ->
                appendLine("${item.quantity}x ${item.productName}${if (item.notes != null) " (${item.notes})" else ""}")
            }
            appendLine()
        }
        sendRaw(text.toByteArray(Charsets.ISO_8859_1))
    }

    override fun printShiftSummary(shift: Shift, totalSales: Double, salesCount: Int) {
        val lines = buildString {
            appendLine(center("CIERRE DE CAJA", 42))
            appendLine(center("Cajero: ${shift.userName}", 42))
            appendLine(divider())
            appendLine("Ventas del turno: $salesCount")
            appendLine("Total: ${fmt.format(totalSales)}")
            appendLine(divider())
            appendLine()
        }
        sendRaw(lines.toByteArray(Charsets.ISO_8859_1))
    }

    override fun openCashDrawer() {
        sendRaw(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
    }

    private fun buildTicket(sale: Sale, payment: Payment, businessName: String): ByteArray {
        val text = buildString {
            appendLine(center(businessName, 42))
            appendLine(divider())
            sale.items.filter { !it.voided }.forEach { item ->
                val line = "${item.quantity}x ${item.productName}"
                val price = fmt.format(item.lineTotal)
                appendLine(line.padEnd(42 - price.length) + price)
            }
            if (sale.discountPercent > 0) {
                val disc = "-${fmt.format(sale.discountAmount)}"
                val label = "Desc. ${sale.discountPercent.toInt()}%"
                appendLine(label.padEnd(42 - disc.length) + disc)
            }
            appendLine(divider())
            val total = fmt.format(sale.total)
            appendLine("TOTAL".padEnd(42 - total.length) + total)
            appendLine("Forma de pago: ${payment.method.label}")
            if (payment.change > 0) appendLine("Vuelto: ${fmt.format(payment.change)}")
            appendLine()
            appendLine(center("¡Gracias!", 42))
            appendLine()
        }
        return text.toByteArray(Charsets.ISO_8859_1)
    }

    private fun center(text: String, width: Int) = text.padStart((width + text.length) / 2).padEnd(width)
    private fun divider(width: Int = 42) = "-".repeat(width)

    private fun sendRaw(bytes: ByteArray) {
        try {
            Socket(host, port).use { socket ->
                socket.getOutputStream().write(bytes)
                socket.getOutputStream().flush()
            }
        } catch (e: Exception) {
            log.warn("Impresora no disponible: ${e.message}")
        }
    }
}

private fun Double.currency(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(this)
