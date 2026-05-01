package ar.ticketsimple.pos

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ar.ticketsimple.pos.infrastructure.AppContainer
import ar.ticketsimple.pos.infrastructure.db.DataSeeder
import ar.ticketsimple.pos.infrastructure.db.DatabaseFactory
import ar.ticketsimple.pos.ui.App
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("TicketSimple")

fun main() {
    log.info("Iniciando TicketSimple POS v1.0.0")
    DatabaseFactory.init()
    DataSeeder.seedIfEmpty()
    val container = AppContainer()

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Maximized,
            size      = DpSize(1280.dp, 800.dp)
        )
        Window(
            onCloseRequest = { container.saleViewModel.destroy(); exitApplication() },
            title          = "TicketSimple POS",
            state          = windowState
        ) {
            App(container)
        }
    }
}
