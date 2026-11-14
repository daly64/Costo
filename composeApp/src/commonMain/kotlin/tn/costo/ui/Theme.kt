package tn.costo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tn.costo.resources.Res
import tn.costo.resources.nunito_bold
import tn.costo.resources.nunito_extrabold
import tn.costo.resources.nunito_regular
import tn.costo.resources.nunito_semibold

/** Palette de l'identité visuelle Costo. */
object C {
    val Nuit = Color(0xFF0E2F66)
    val Bleu = Color(0xFF1D5FBF)
    val BleuDoux = Color(0xFF164A99)
    val BleuClair = Color(0xFFE3EDFB)
    val BleuPale = Color(0xFFC9D8F2)
    val Fond = Color(0xFFF2F5FA)
    val Carte = Color(0xFFFFFFFF)
    val Bord = Color(0xFFE2E4EA)
    val Texte = Color(0xFF1A1A1A)
    val Gris = Color(0xFF5F6368)
    val AlerteFond = Color(0xFFFDEEDC)
    val Alerte = Color(0xFF8A4B08)
    val Rouge = Color(0xFFB3261E)
}

@Composable
fun nunito(): FontFamily = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
)

@Composable
fun CostoTheme(content: @Composable () -> Unit) {
    val f = nunito()
    val base = Typography()
    fun TextStyle.n() = copy(fontFamily = f)
    val typo = Typography(
        displayLarge = base.displayLarge.n(), displayMedium = base.displayMedium.n(), displaySmall = base.displaySmall.n(),
        headlineLarge = base.headlineLarge.n().copy(fontWeight = FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.n().copy(fontWeight = FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.n().copy(fontWeight = FontWeight.ExtraBold),
        titleLarge = base.titleLarge.n().copy(fontWeight = FontWeight.ExtraBold),
        titleMedium = base.titleMedium.n().copy(fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.n().copy(fontWeight = FontWeight.Bold),
        bodyLarge = base.bodyLarge.n(), bodyMedium = base.bodyMedium.n(), bodySmall = base.bodySmall.n(),
        labelLarge = base.labelLarge.n().copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        labelMedium = base.labelMedium.n(), labelSmall = base.labelSmall.n(),
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.Bleu, onPrimary = Color.White,
            primaryContainer = C.BleuClair, onPrimaryContainer = C.Nuit,
            secondary = C.Nuit, onSecondary = Color.White,
            secondaryContainer = C.BleuClair, onSecondaryContainer = C.Nuit,
            background = C.Fond, onBackground = C.Texte,
            surface = C.Carte, onSurface = C.Texte,
            surfaceVariant = C.Fond, onSurfaceVariant = C.Gris,
            surfaceContainer = C.Carte, surfaceContainerHigh = C.Carte, surfaceContainerHighest = C.Fond,
            outline = C.Bord, outlineVariant = C.Bord, error = C.Rouge,
        ),
        typography = typo,
        content = content,
    )
}
