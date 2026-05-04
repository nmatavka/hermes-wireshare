package org.limewire.ui.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.limewire.ui.compose.integration.readComposeAppearanceSetting

@Composable
fun WireShareTheme(
    appearance: ComposeAppearance = readComposeAppearanceSetting(),
    localeEpoch: Int = 0,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appearance) {
        ComposeAppearance.SOLARIZED_LIGHT -> paletteToLightScheme(SOLARIZED_LIGHT_PALETTE)
        ComposeAppearance.SOLARIZED_DARK -> paletteToDarkScheme(SOLARIZED_DARK_PALETTE)
        ComposeAppearance.SELENIZED_DARK -> paletteToDarkScheme(SELENIZED_DARK_PALETTE)
        ComposeAppearance.SELENIZED_BLACK -> paletteToDarkScheme(SELENIZED_BLACK_PALETTE)
        ComposeAppearance.SELENIZED_LIGHT -> paletteToLightScheme(SELENIZED_LIGHT_PALETTE)
        ComposeAppearance.SELENIZED_WHITE -> paletteToLightScheme(SELENIZED_WHITE_PALETTE)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WireShareTypography,
        shapes = WireShareShapes,
        content = content
    )
}

private val WireShareTypography = Typography().run {
    copy(
        headlineSmall = headlineSmall.copy(
            fontSize = 21.sp,
            lineHeight = 26.sp
        ),
        titleLarge = titleLarge.copy(
            fontSize = 19.sp,
            lineHeight = 24.sp
        ),
        titleMedium = titleMedium.copy(
            fontSize = 16.sp,
            lineHeight = 20.sp
        ),
        titleSmall = titleSmall.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        bodyLarge = bodyLarge.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp
        ),
        bodyMedium = bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        bodySmall = bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 17.sp
        ),
        labelLarge = labelLarge.copy(
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = labelSmall.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

private val WireShareShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
)

private data class ThemePalette(
    val bg0: Color,
    val bg1: Color,
    val bg2: Color,
    val dim0: Color,
    val fg0: Color,
    val fg1: Color,
    val red: Color,
    val green: Color,
    val yellow: Color,
    val blue: Color,
    val magenta: Color,
    val cyan: Color,
    val orange: Color,
    val violet: Color
)

private fun paletteToLightScheme(palette: ThemePalette): ColorScheme {
    return lightColorScheme(
        primary = palette.blue,
        onPrimary = palette.bg0,
        primaryContainer = palette.bg1,
        onPrimaryContainer = palette.fg1,
        inversePrimary = palette.cyan,
        secondary = palette.cyan,
        onSecondary = palette.bg0,
        secondaryContainer = palette.bg1,
        onSecondaryContainer = palette.fg1,
        tertiary = palette.violet,
        onTertiary = palette.bg0,
        tertiaryContainer = palette.bg1,
        onTertiaryContainer = palette.fg1,
        background = palette.bg0,
        onBackground = palette.fg1,
        surface = palette.bg0,
        onSurface = palette.fg1,
        surfaceVariant = palette.bg1,
        onSurfaceVariant = palette.fg0,
        surfaceTint = palette.blue,
        inverseSurface = palette.fg1,
        inverseOnSurface = palette.bg0,
        error = palette.red,
        onError = palette.bg0,
        errorContainer = palette.bg1,
        onErrorContainer = palette.red,
        outline = palette.dim0,
        outlineVariant = palette.bg2,
        scrim = Color.Black
    )
}

private fun paletteToDarkScheme(palette: ThemePalette): ColorScheme {
    return darkColorScheme(
        primary = palette.blue,
        onPrimary = palette.bg0,
        primaryContainer = palette.bg2,
        onPrimaryContainer = palette.fg1,
        inversePrimary = palette.cyan,
        secondary = palette.cyan,
        onSecondary = palette.bg0,
        secondaryContainer = palette.bg2,
        onSecondaryContainer = palette.fg1,
        tertiary = palette.violet,
        onTertiary = palette.bg0,
        tertiaryContainer = palette.bg2,
        onTertiaryContainer = palette.fg1,
        background = palette.bg0,
        onBackground = palette.fg1,
        surface = palette.bg0,
        onSurface = palette.fg1,
        surfaceVariant = palette.bg1,
        onSurfaceVariant = palette.fg0,
        surfaceTint = palette.blue,
        inverseSurface = palette.fg1,
        inverseOnSurface = palette.bg0,
        error = palette.red,
        onError = palette.bg0,
        errorContainer = palette.bg2,
        onErrorContainer = palette.red,
        outline = palette.dim0,
        outlineVariant = palette.bg2,
        scrim = Color.Black
    )
}

private val SOLARIZED_LIGHT_PALETTE = ThemePalette(
    bg0 = Color(0xFFFDF6E3),
    bg1 = Color(0xFFEEE8D5),
    bg2 = Color(0xFF93A1A1),
    dim0 = Color(0xFF839496),
    fg0 = Color(0xFF657B83),
    fg1 = Color(0xFF586E75),
    red = Color(0xFFDC322F),
    green = Color(0xFF859900),
    yellow = Color(0xFFB58900),
    blue = Color(0xFF268BD2),
    magenta = Color(0xFFD33682),
    cyan = Color(0xFF2AA198),
    orange = Color(0xFFCB4B16),
    violet = Color(0xFF6C71C4)
)

private val SOLARIZED_DARK_PALETTE = ThemePalette(
    bg0 = Color(0xFF002B36),
    bg1 = Color(0xFF073642),
    bg2 = Color(0xFF586E75),
    dim0 = Color(0xFF657B83),
    fg0 = Color(0xFF839496),
    fg1 = Color(0xFF93A1A1),
    red = Color(0xFFDC322F),
    green = Color(0xFF859900),
    yellow = Color(0xFFB58900),
    blue = Color(0xFF268BD2),
    magenta = Color(0xFFD33682),
    cyan = Color(0xFF2AA198),
    orange = Color(0xFFCB4B16),
    violet = Color(0xFF6C71C4)
)

private val SELENIZED_DARK_PALETTE = ThemePalette(
    bg0 = Color(0xFF103C48),
    bg1 = Color(0xFF184956),
    bg2 = Color(0xFF2D5B69),
    dim0 = Color(0xFF72898F),
    fg0 = Color(0xFFADBCBC),
    fg1 = Color(0xFFCAD8D9),
    red = Color(0xFFFA5750),
    green = Color(0xFF75B938),
    yellow = Color(0xFFDBB32D),
    blue = Color(0xFF4695F7),
    magenta = Color(0xFFF275BE),
    cyan = Color(0xFF41C7B9),
    orange = Color(0xFFED8649),
    violet = Color(0xFFAF88EB)
)

private val SELENIZED_BLACK_PALETTE = ThemePalette(
    bg0 = Color(0xFF181818),
    bg1 = Color(0xFF252525),
    bg2 = Color(0xFF3B3B3B),
    dim0 = Color(0xFF777777),
    fg0 = Color(0xFFB9B9B9),
    fg1 = Color(0xFFDEDEDE),
    red = Color(0xFFED4A46),
    green = Color(0xFF70B433),
    yellow = Color(0xFFDBB32D),
    blue = Color(0xFF368AEB),
    magenta = Color(0xFFEB6EB7),
    cyan = Color(0xFF3FC5B7),
    orange = Color(0xFFE67F43),
    violet = Color(0xFFA580E2)
)

private val SELENIZED_LIGHT_PALETTE = ThemePalette(
    bg0 = Color(0xFFFBF3DB),
    bg1 = Color(0xFFECE3CC),
    bg2 = Color(0xFFD5CDB6),
    dim0 = Color(0xFF909995),
    fg0 = Color(0xFF53676D),
    fg1 = Color(0xFF3A4D53),
    red = Color(0xFFD2212D),
    green = Color(0xFF489100),
    yellow = Color(0xFFAD8900),
    blue = Color(0xFF0072D4),
    magenta = Color(0xFFCA4898),
    cyan = Color(0xFF009C8F),
    orange = Color(0xFFC25D1E),
    violet = Color(0xFF8762C6)
)

private val SELENIZED_WHITE_PALETTE = ThemePalette(
    bg0 = Color(0xFFFFFFFF),
    bg1 = Color(0xFFEBEBEB),
    bg2 = Color(0xFFCDCDCD),
    dim0 = Color(0xFF878787),
    fg0 = Color(0xFF474747),
    fg1 = Color(0xFF282828),
    red = Color(0xFFD6000C),
    green = Color(0xFF1D9700),
    yellow = Color(0xFFC49700),
    blue = Color(0xFF0064E4),
    magenta = Color(0xFFDD0F9D),
    cyan = Color(0xFF00AD9C),
    orange = Color(0xFFD04A00),
    violet = Color(0xFF7F51D6)
)
