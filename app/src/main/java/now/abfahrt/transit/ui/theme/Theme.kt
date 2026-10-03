package now.abfahrt.transit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Brand Colours ─────────────────────────────────────────────────────────────
val TransitBlue       = Color(0xFF1565C0)
val TransitBlueDark   = Color(0xFF0D47A1)
val TransitBlueLight  = Color(0xFF42A5F5)
val DelayRed          = Color(0xFFD32F2F)
val OnTimeGreen       = Color(0xFF388E3C)
val CancelledGrey     = Color(0xFF9E9E9E)

private val LightScheme = lightColorScheme(
    primary          = TransitBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    secondary        = Color(0xFF545F71),
    surface          = Color(0xFFFAFCFF),
    background       = Color(0xFFF5F7FB),
    error            = DelayRed,
)

private val DarkScheme = darkColorScheme(
    primary          = TransitBlueLight,
    onPrimary        = Color(0xFF003064),
    primaryContainer = Color(0xFF00458F),
    secondary        = Color(0xFFBBC7DB),
    surface          = Color(0xFF111418),
    background       = Color(0xFF191C20),
    error            = Color(0xFFFFB4AB),
)

@Composable
fun AbfahrtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic colour (Material You) on Android 12+, fallback to static scheme
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkScheme
        else      -> LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
