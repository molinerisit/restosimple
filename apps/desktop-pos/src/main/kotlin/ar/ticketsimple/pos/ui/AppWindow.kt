package ar.ticketsimple.pos.ui

import androidx.compose.runtime.*
import ar.ticketsimple.pos.application.sale.ShiftSummary
import ar.ticketsimple.pos.domain.shifts.Shift
import ar.ticketsimple.pos.domain.users.User
import ar.ticketsimple.pos.infrastructure.AppContainer
import ar.ticketsimple.pos.ui.auth.LoginScreen
import ar.ticketsimple.pos.ui.sale.SaleScreen
import ar.ticketsimple.pos.ui.shift.CloseShiftScreen
import ar.ticketsimple.pos.ui.shift.OpenShiftDialog
import ar.ticketsimple.pos.ui.theme.TicketSimpleTheme

enum class AppScreen { LOGIN, OPEN_SHIFT, SALE, CLOSE_SHIFT }

@Composable
fun App(container: AppContainer) {
    TicketSimpleTheme {
        val viewModel = remember { container.saleViewModel }
        val state by viewModel.state.collectAsState()

        var screen by remember { mutableStateOf(AppScreen.LOGIN) }
        var closeShiftSummary by remember { mutableStateOf<ShiftSummary?>(null) }
        var closeShiftRef by remember { mutableStateOf<Shift?>(null) }

        when (screen) {
            AppScreen.LOGIN -> {
                LoginScreen(onLogin = { pin ->
                    val ok = viewModel.loginUser(pin)
                    if (ok) {
                        screen = if (state.shift == null) AppScreen.OPEN_SHIFT else AppScreen.SALE
                    }
                    ok
                })
            }

            AppScreen.OPEN_SHIFT -> {
                val user = state.currentUser ?: return@TicketSimpleTheme
                OpenShiftDialog(
                    user         = user,
                    shiftService = container.shiftService,
                    onShiftOpened = {
                        viewModel.refreshAfterShiftOpen()
                        screen = AppScreen.SALE
                    }
                )
            }

            AppScreen.SALE -> {
                SaleScreen(viewModel)
            }

            AppScreen.CLOSE_SHIFT -> {
                val shift   = closeShiftRef ?: return@TicketSimpleTheme
                val summary = closeShiftSummary ?: return@TicketSimpleTheme
                CloseShiftScreen(
                    shift    = shift,
                    summary  = summary,
                    onConfirm = { cash, notes ->
                        val user = state.currentUser ?: return@CloseShiftScreen
                        container.shiftService.closeShift(shift.id, user.id, cash, notes)
                        closeShiftRef = null
                        closeShiftSummary = null
                        screen = AppScreen.LOGIN
                    },
                    onCancel = { screen = AppScreen.SALE }
                )
            }
        }

        LaunchedEffect(state.shift) {
            if (screen == AppScreen.SALE && state.currentUser != null && state.shift == null) {
                screen = AppScreen.OPEN_SHIFT
            }
        }
    }
}
