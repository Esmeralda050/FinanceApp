package com.esmeraldadelgado.financeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = InkLight,
    primaryContainer = BrandPurpleSoft,
    onPrimaryContainer = InkDark,
    secondary = MoneyGreen,
    onSecondary = InkLight,
    secondaryContainer = MoneyGreenSoft,
    onSecondaryContainer = InkDark,
    tertiary = BagOrange,
    onTertiary = InkDark,
    tertiaryContainer = BagCream,
    onTertiaryContainer = InkDark,
    background = LightBackground,
    onBackground = InkDark,
    surface = LightSurface,
    onSurface = InkDark,
    surfaceVariant = BrandPurpleSoft,
    onSurfaceVariant = InkDark,
    outline = BrandPurpleLight,
    error = ErrorRed
)

@Composable
fun FinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}