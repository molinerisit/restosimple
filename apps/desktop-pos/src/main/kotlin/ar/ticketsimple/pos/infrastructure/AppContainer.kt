package ar.ticketsimple.pos.infrastructure

import ar.ticketsimple.pos.application.catalog.CatalogService
import ar.ticketsimple.pos.application.sale.SaleService
import ar.ticketsimple.pos.application.shifts.ShiftService
import ar.ticketsimple.pos.infrastructure.db.*
import ar.ticketsimple.pos.infrastructure.printing.ConsolePrinterAdapter
import ar.ticketsimple.pos.infrastructure.printing.PrinterPort
import ar.ticketsimple.pos.ui.sale.SaleViewModel

class AppContainer(
    printerPort: PrinterPort = ConsolePrinterAdapter(),
    businessName: String = "TicketSimple"
) {
    val catalogRepository = SqliteCatalogRepository()
    val saleRepository    = SqliteSaleRepository()
    val shiftRepository   = SqliteShiftRepository()
    val userRepository    = SqliteUserRepository()
    val auditRepository   = SqliteAuditRepository()

    val catalogService = CatalogService(catalogRepository)
    val shiftService   = ShiftService(shiftRepository, auditRepository)
    val saleService    = SaleService(saleRepository, auditRepository, printerPort, shiftService, businessName)

    val saleViewModel = SaleViewModel(saleService, catalogService, shiftService, userRepository)
}
