package tn.costo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.costo.Store
import tn.costo.model.Ingredient
import tn.costo.model.Unite
import tn.costo.model.basePriceLabel
import tn.costo.model.dt
import tn.costo.model.num
import tn.costo.model.packLabel

@Composable
fun IngredientsScreen(store: Store) {
    val d = store.data
    var editing by remember { mutableStateOf<Ingredient?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { BrandHeader("Ingrédients", "Prix d'achat de tes matières premières") }
            items(d.ingredients, key = { it.id }) { ing ->
                val uses = d.products.count { p -> p.recipe.any { it.ingredientId == ing.id } }
                IngredientRow(ing, uses) { editing = ing }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Ajouter un ingrédient") },
            containerColor = C.Bleu, contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }

    editing?.let { ing ->
        val uses = d.products.count { p -> p.recipe.any { it.ingredientId == ing.id } }
        IngredientDialog(
            initial = ing, uses = uses,
            onDismiss = { editing = null },
            onSave = { store.saveIngredient(it); editing = null },
            onDelete = { store.deleteIngredient(ing.id); editing = null },
        )
    }
    if (creating) {
        IngredientDialog(
            initial = Ingredient(store.newId("i"), "", 0.0, 1.0, Unite.KG), uses = 0, isNew = true,
            onDismiss = { creating = false },
            onSave = { store.saveIngredient(it); creating = false },
            onDelete = { creating = false },
        )
    }
}

@Composable
private fun IngredientRow(ing: Ingredient, uses: Int, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 60.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.Carte),
        border = BorderStroke(1.dp, C.Bord),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ing.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    when (uses) { 0 -> "Pas encore utilisé"; 1 -> "Utilisé dans 1 recette"; else -> "Utilisé dans $uses recettes" },
                    fontSize = 13.sp, color = C.Gris,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    trend(ing)?.let { (up, label) ->
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (up) C.Alerte else C.BleuDoux)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("${dt(ing.packPrice)} DT", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(ing.packLabel(), fontSize = 12.sp, color = C.Gris)
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.Edit, contentDescription = "Modifier", tint = C.Gris, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

/** Évolution depuis le prix précédent : (hausse ?, "+5 %"). */
private fun trend(ing: Ingredient): Pair<Boolean, String>? {
    if (ing.history.size < 2) return null
    val prev = ing.history[ing.history.size - 2]
    val prevBase = prev.packPrice / (prev.packQty * prev.unit.factor)
    if (prevBase <= 0.0) return null
    val change = (ing.pricePerBase - prevBase) / prevBase * 100.0
    if (kotlin.math.abs(change) < 0.5) return null
    val r = kotlin.math.round(change).toInt()
    return (r > 0) to (if (r > 0) "▲ +$r %" else "▼ $r %")
}

@Composable
private fun IngredientDialog(
    initial: Ingredient, uses: Int, isNew: Boolean = false,
    onDismiss: () -> Unit, onSave: (Ingredient) -> Unit, onDelete: () -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var price by remember { mutableStateOf(initial.packPrice) }
    var qty by remember { mutableStateOf(initial.packQty) }
    var unit by remember { mutableStateOf(initial.unit) }
    var confirmDelete by remember { mutableStateOf(false) }
    val preview = initial.copy(packPrice = price, packQty = qty, unit = unit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Nouvel ingrédient" else initial.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextInput(name, { name = it }, "Nom")
                NumberField(price, { price = it }, "Prix d'achat du paquet", suffix = "DT")
                NumberField(qty, { qty = it }, "Quantité dans le paquet", suffix = unit.label)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Unite.entries.forEach { u ->
                        val on = u == unit
                        Text(
                            u.label,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (on) C.Bleu else C.Fond).clickable { unit = u }.padding(vertical = 10.dp),
                            color = if (on) Color.White else C.Texte, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Text("Soit ${preview.basePriceLabel()}", color = C.Bleu, fontWeight = FontWeight.Bold)
                if (initial.history.isNotEmpty()) {
                    HorizontalDivider(color = C.Bord)
                    Text("Historique des prix", fontWeight = FontWeight.ExtraBold)
                    initial.history.asReversed().forEach { h ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(h.date, color = C.Gris, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${dt(h.packPrice)} DT · ${num(h.packQty)} ${h.unit.label}", fontSize = 13.sp)
                        }
                    }
                }
                if (!isNew) {
                    TextButton(onClick = { confirmDelete = true }) { Text("Supprimer l'ingrédient", color = C.Rouge) }
                    if (confirmDelete) {
                        Text(
                            if (uses > 0) "Il sera retiré de $uses recette(s). Confirmer ?" else "Confirmer la suppression ?",
                            color = C.Rouge, fontSize = 13.sp,
                        )
                        TextButton(onClick = onDelete) { Text("Oui, supprimer", color = C.Rouge, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && qty > 0,
                onClick = { onSave(preview.copy(name = name.trim())) },
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
