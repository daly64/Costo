package tn.costo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun Onboarding(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val dark = pager.currentPage == 0
    Column(
        Modifier.fillMaxSize().background(if (dark) C.Nuit else C.Fond)
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDone) {
                Text("Passer", color = if (dark) C.BleuPale else C.Gris, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { page ->
            when (page) {
                0 -> WelcomeIntro()
                1 -> WelcomeStep(
                    "Ajoute tes ingrédients",
                    "Entre une seule fois le prix d'achat du café, du lait, du sucre… Costo s'en sert pour toutes tes recettes.",
                ) { IngredientsIllustration() }
                else -> WelcomeStep(
                    "Choisis ta marge, Costo calcule le prix",
                    "Compose tes boissons avec tes doses, et vois tout de suite le prix conseillé et ton bénéfice par tasse.",
                ) { PriceIllustration() }
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    val on = i == pager.currentPage
                    val color = when { on && dark -> Color.White; on -> C.Bleu; dark -> Color(0xFF4E73AE); else -> Color(0xFFB9C9E6) }
                    Box(Modifier.height(8.dp).width(if (on) 24.dp else 8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                }
            }
            Spacer(Modifier.height(24.dp))
            val last = pager.currentPage == 2
            Button(
                onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = if (dark) ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = C.Nuit)
                else ButtonDefaults.buttonColors(containerColor = C.Bleu, contentColor = Color.White),
            ) {
                Text(if (last) "Commencer" else "Suivant", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun WelcomeIntro() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CostoIcon(140.dp, background = C.Bleu, bean = C.Nuit)
        Spacer(Modifier.height(20.dp))
        Text("costo", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Le vrai coût de chaque tasse", color = Color.White, fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Calcule ce que te coûte chaque boisson et fixe des prix qui gardent ta marge.",
            color = C.BleuPale, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun WelcomeStep(title: String, text: String, illustration: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Box(
            Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(28.dp)).background(C.BleuClair).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) { illustration() }
        Spacer(Modifier.height(32.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = C.Nuit, lineHeight = 32.sp)
        Spacer(Modifier.height(10.dp))
        Text(text, fontSize = 16.sp, color = C.Gris, lineHeight = 24.sp)
    }
}

@Composable
private fun IngredientsIllustration() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(Triple("Café en grains", "38,000 DT/kg", 0), Triple("Lait", "1,300 DT/L", 18), Triple("Sucre", "5,429 DT/kg", 36))
            .forEach { (name, price, indent) ->
                Row(
                    Modifier.padding(start = indent.dp).fillMaxWidth().height(56.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp))
                        .background(Color.White).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = C.Bleu)
                }
            }
    }
}

@Composable
private fun PriceIllustration() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Cappuccino", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text("Coût 0,967 DT", fontSize = 14.sp, color = C.Gris)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(60, 65, 70, 75).forEach { m ->
                val on = m == 70
                Box(
                    Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (on) C.Bleu else Color.White),
                    contentAlignment = Alignment.Center,
                ) { Text("$m %", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (on) Color.White else C.Texte) }
            }
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.Nuit).padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Text("Prix de vente conseillé", color = C.BleuPale, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("3,223 DT", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

