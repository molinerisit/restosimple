package ar.ticketsimple.pos.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val TSColorScheme = lightColorScheme(
    primary          = TSColors.Caramel,
    onPrimary        = Color.White,
    primaryContainer = TSColors.Crema,
    onPrimaryContainer = TSColors.Espresso,
    secondary        = TSColors.Espresso,
    onSecondary      = Color.White,
    secondaryContainer = TSColors.Espresso10,
    onSecondaryContainer = TSColors.Espresso,
    tertiary         = TSColors.Salvia,
    onTertiary       = Color.White,
    background       = TSColors.Surface,
    onBackground     = TSColors.Ink,
    surface          = Color.White,
    onSurface        = TSColors.Ink,
    surfaceVariant   = TSColors.Crema,
    onSurfaceVariant = TSColors.Espresso,
    error            = TSColors.Danger,
    onError          = Color.White
)

val TSTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,  fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    bodyLarge    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium   = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge   = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.4.sp),
    labelMedium  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.4.sp)
)

@Composable
fun TicketSimpleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TSColorScheme,
        typography  = TSTypography,
        content     = content
    )
}

object Spacing {
    const val xs  = 4
    const val sm  = 8
    const val md  = 12
    const val lg  = 16
    const val xl  = 24
    const val xxl = 32
}
