package id.ihsg.stockanalysis.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IhsgColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF126A5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F4EC),
    onPrimaryContainer = Color(0xFF07372F),
    secondary = Color(0xFF6B5E2E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEEB5),
    onSecondaryContainer = Color(0xFF312802),
    tertiary = Color(0xFF725572),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBD7FA),
    onTertiaryContainer = Color(0xFF2A142C),
    error = Color(0xFFB3261E),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF151B19),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151B19),
    surfaceVariant = Color(0xFFE1E7E4),
    onSurfaceVariant = Color(0xFF424A47),
    outline = Color(0xFF707975)
)

@Composable
fun IhsgTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IhsgColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
