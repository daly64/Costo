package tn.costo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.costo.model.dt
import tn.costo.model.num
import tn.costo.model.parseNumber

/** Icône Costo : étiquette de prix avec un grain de café. */
@Composable
fun CostoIcon(size: Dp, background: Color = C.Nuit, bean: Color = C.Bleu, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val k = this.size.width / 100f
        drawRoundRect(background, cornerRadius = CornerRadius(24f * k, 24f * k))
        withTransform({
            scale(k, k, pivot = Offset.Zero)
            rotate(-35f, pivot = Offset(50f, 50f))
        }) {
            val tag = Path().apply {
                moveTo(36f, 30f); lineTo(78f, 30f)
                cubicTo(81.3f, 30f, 84f, 32.7f, 84f, 36f); lineTo(84f, 64f)
                cubicTo(84f, 67.3f, 81.3f, 70f, 78f, 70f); lineTo(36f, 70f)
                lineTo(18f, 50f); close()
            }
            drawPath(tag, Color.White)
            drawPath(tag, Color.White, style = Stroke(width = 4f, join = StrokeJoin.Round))
            drawCircle(background, radius = 4.5f, center = Offset(31f, 50f))
            drawOval(bean, topLeft = Offset(50f, 37f), size = Size(20f, 26f))
            val crease = Path().apply { moveTo(60f, 38f); cubicTo(54f, 45f, 66f, 55f, 60f, 62f) }
            drawPath(crease, Color.White, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }
    }
}

/** Petite tasse pour la barre de navigation. */
@Composable
fun CupIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val k = size.width / 24f
        val st = Stroke(width = 2f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val cup = Path().apply {
            moveTo(4f * k, 8f * k); lineTo(17f * k, 8f * k); lineTo(17f * k, 13f * k)
            cubicTo(17f * k, 15.8f * k, 14.8f * k, 18f * k, 12f * k, 18f * k)
            lineTo(9f * k, 18f * k)
            cubicTo(6.2f * k, 18f * k, 4f * k, 15.8f * k, 4f * k, 13f * k); close()
        }
        drawPath(cup, color, style = st)
        val handle = Path().apply {
            moveTo(17f * k, 9f * k); lineTo(18.5f * k, 9f * k)
            cubicTo(19.9f * k, 9f * k, 21f * k, 10.1f * k, 21f * k, 11.5f * k)
            cubicTo(21f * k, 12.9f * k, 19.9f * k, 14f * k, 18.5f * k, 14f * k); lineTo(17f * k, 14f * k)
        }
        drawPath(handle, color, style = st)
        drawLine(color, Offset(8f * k, 3f * k), Offset(8f * k, 5f * k), strokeWidth = 2f * k, cap = StrokeCap.Round)
        drawLine(color, Offset(12f * k, 3f * k), Offset(12f * k, 5f * k), strokeWidth = 2f * k, cap = StrokeCap.Round)
    }
}

@Composable
fun BrandHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CostoIcon(26.dp)
            Spacer(Modifier.width(8.dp))
            Text("costo", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = C.Nuit)
        }
        Spacer(Modifier.size(6.dp))
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = C.Texte)
        Text(subtitle, fontSize = 14.sp, color = C.Gris)
    }
}

@Composable
fun Section(title: String? = null, modifier: Modifier = Modifier, trailing: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C.Carte),
        border = BorderStroke(1.dp, C.Bord),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (title != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    if (trailing != null) Text(trailing, fontSize = 12.sp, color = C.Gris)
                }
            }
            content()
        }
    }
}

/** Pastille de marge : bleue si la marge atteint l'objectif, orange sinon. */
@Composable
fun MarginBadge(margin: Double?, target: Int) {
    val ok = margin != null && margin >= target
    val bg = when { margin == null -> C.Fond; ok -> C.BleuClair; else -> C.AlerteFond }
    val fg = when { margin == null -> C.Gris; ok -> C.BleuDoux; else -> C.Alerte }
    Text(
        if (margin == null) "—" else "${kotlin.math.round(margin).toInt()} %",
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
        color = fg, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
    )
}

@Composable
fun LabelValue(label: String, value: String, strong: Boolean = false, valueColor: Color = C.Texte) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = if (strong) 16.sp else 14.sp, color = if (strong) C.Texte else C.Gris,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(value, fontSize = if (strong) 20.sp else 16.sp, fontWeight = if (strong) FontWeight.ExtraBold else FontWeight.Bold, color = valueColor)
    }
}

fun money(x: Double) = "${dt(x)} DT"

/** Champ numérique : virgule acceptée, valeur transmise dès qu'elle est lisible. */
@Composable
fun NumberField(
    value: Double,
    onValue: (Double) -> Unit,
    label: String,
    suffix: String? = null,
    modifier: Modifier = Modifier,
    decimals: Boolean = true,
) {
    var text by remember { mutableStateOf(if (decimals) num(value) else value.toLong().toString()) }
    LaunchedEffect(value) {
        val cur = parseNumber(text)
        if (cur == null || kotlin.math.abs(cur - value) > 1e-9) text = if (decimals) num(value) else value.toLong().toString()
    }
    OutlinedTextField(
        value = text,
        onValueChange = { t ->
            text = t
            parseNumber(t)?.let { if (it >= 0) onValue(it) }
        },
        label = { Text(label) },
        suffix = if (suffix != null) { { Text(suffix) } } else null,
        singleLine = true,
        isError = parseNumber(text) == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.Bord, focusedBorderColor = C.Bleu),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun TextInput(value: String, onValue: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValue, label = { Text(label) }, singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = C.Bord, focusedBorderColor = C.Bleu),
        modifier = modifier.fillMaxWidth(),
    )
}
