package tn.costo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.costo.Store
import tn.costo.model.Ingredient
import tn.costo.model.Kind
import tn.costo.model.Product
import tn.costo.model.RecipeLine
import tn.costo.model.basePriceLabel
import tn.costo.model.breakdown
import tn.costo.model.doseLabel
import tn.costo.model.marginFor
import tn.costo.model.num
import tn.costo.model.pct
import tn.costo.model.realMargin
import tn.costo.model.roundUpPrice
import tn.costo.model.suggestedPrice

@Composable
fun DrinkDetailScreen(store: Store, id: String, onBack: () -> Unit) {
    val d = store.data
    val p = d.products.firstOrNull { it.id == id }
    if (p == null) {
        LaunchedEffect(id) { onBack() }
        return
    }
    val b = d.breakdown(p)
    val margin = d.marginFor(p)
    val suggested = suggestedPrice(b.total, margin)
    var editLine by remember { mutableStateOf<RecipeLine?>(null) }
    var adding by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
            Column(Modifier.weight(1f)) {
                Text(p.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(p.category + if (p.hidden) " · masqué au menu" else "", fontSize = 13.sp, color = C.Gris)
            }
            IconButton(onClick = { renaming = true }) { Icon(Icons.Filled.Edit, contentDescription = "Renommer", tint = C.Gris) }
            IconButton(onClick = { deleting = true }) { Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = C.Gris) }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Section("Ingrédients", trailing = "pour 1 portion") {
                if (b.lines.isEmpty()) Text("Aucun ingrédient pour l'instant.", color = C.Gris, fontSize = 14.sp)
                b.lines.forEach { lc ->
                    HorizontalDivider(color = C.Bord)
                    Row(
                        Modifier.fillMaxWidth().clickable { editLine = lc.line }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(lc.ingredient?.name ?: "Ingrédient supprimé", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${doseLabel(lc.line.qty, lc.ingredient)} × ${lc.ingredient?.basePriceLabel() ?: "—"}",
                                fontSize = 13.sp, color = C.Gris,
                            )
                        }
                        Text(money(lc.cost), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { adding = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, C.BleuPale),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = C.Bleu)
                    Text(" Ajouter un ingrédient", color = C.Bleu, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = C.Bord)
                LabelValue("Ingrédients", money(b.ingredients))
                if (p.kind != Kind.REVENTE) LabelValue("Pertes (${num(d.settings.lossPct)} %)", money(b.losses))
                if (p.kind == Kind.BOISSON) LabelValue("Coût fixe (énergie, nettoyage)", money(b.fixed))
                LabelValue("Coût de revient", money(b.total), strong = true)
            }

            Section("Marge souhaitée") {
                val choices = (listOf(50, 60, 65, 70, 75, 80) + margin).distinct().sorted()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    choices.forEach { m ->
                        val on = m == margin
                        Text(
                            "$m %",
                            modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (on) C.Bleu else C.Fond)
                                .clickable { store.saveProduct(p.copy(margin = m)) }
                                .padding(top = 12.dp),
                            color = if (on) Color.White else C.Texte,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.Nuit).padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text("Prix de vente conseillé", color = C.BleuPale, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(money(suggested), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Arrondi : ${money(roundUpPrice(suggested))}", color = C.BleuPale, fontSize = 14.sp)
                }
            }

            Section("Prix au menu") {
                NumberField(p.salePrice, { store.saveProduct(p.copy(salePrice = it)) }, "Prix de vente", suffix = "DT")
                val rm = realMargin(b.total, p.salePrice)
                LabelValue("Marge réelle", if (rm == null) "—" else "${pct(rm)} %",
                    valueColor = if (rm != null && rm >= margin) C.BleuDoux else C.Alerte)
                LabelValue("Bénéfice par portion", money(p.salePrice - b.total))
                if (rm != null && rm < margin) {
                    Text(
                        "Ta marge est sous l'objectif de $margin %. Prix conseillé : ${money(roundUpPrice(suggested))}.",
                        fontSize = 13.sp, color = C.Alerte,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.AlerteFond).padding(10.dp),
                    )
                }
            }

            Section("Type de produit") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Kind.entries.forEach { k ->
                        val on = k == p.kind
                        Text(
                            k.label,
                            modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (on) C.BleuClair else C.Fond)
                                .clickable { store.saveProduct(p.copy(kind = k)) }.padding(top = 10.dp),
                            color = if (on) C.Nuit else C.Gris, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
                Text(p.kind.help, fontSize = 13.sp, color = C.Gris)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Masqué au menu", fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = p.hidden, onCheckedChange = { store.saveProduct(p.copy(hidden = it)) },
                        colors = SwitchDefaults.colors(checkedTrackColor = C.Bleu),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    editLine?.let { line ->
        val ing = d.ingredients.firstOrNull { it.id == line.ingredientId }
        DoseDialog(
            title = ing?.name ?: "Ingrédient",
            unit = ing?.unit?.base ?: "",
            initial = line.qty,
            onDismiss = { editLine = null },
            onSave = { q ->
                store.saveProduct(p.copy(recipe = p.recipe.map { if (it === line) it.copy(qty = q) else it }))
                editLine = null
            },
            onRemove = {
                store.saveProduct(p.copy(recipe = p.recipe.filterNot { it === line }))
                editLine = null
            },
        )
    }

    if (adding) {
        AddIngredientDialog(
            ingredients = d.ingredients.filter { i -> p.recipe.none { it.ingredientId == i.id } },
            onDismiss = { adding = false },
            onAdd = { ing, q ->
                store.saveProduct(p.copy(recipe = p.recipe + RecipeLine(ing.id, q)))
                adding = false
            },
        )
    }

    if (renaming) {
        var name by remember { mutableStateOf(p.name) }
        var cat by remember { mutableStateOf(p.category) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Modifier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextInput(name, { name = it }, "Nom")
                    TextInput(cat, { cat = it }, "Catégorie")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) store.saveProduct(p.copy(name = name.trim(), category = cat.trim().ifBlank { p.category }))
                    renaming = false
                }) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Annuler") } },
        )
    }

    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text("Supprimer ${p.name} ?") },
            text = { Text("La boisson et sa recette seront supprimées de Costo. Le menu affiché ne change pas.") },
            confirmButton = {
                TextButton(onClick = { deleting = false; store.deleteProduct(p.id); onBack() }) { Text("Supprimer", color = C.Rouge) }
            },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun DoseDialog(title: String, unit: String, initial: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit, onRemove: () -> Unit) {
    var q by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { NumberField(q, { q = it }, "Dose pour 1 portion", suffix = unit) },
        confirmButton = { TextButton(onClick = { onSave(q) }) { Text("Enregistrer") } },
        dismissButton = {
            Row {
                TextButton(onClick = onRemove) { Text("Retirer", color = C.Rouge) }
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        },
    )
}

@Composable
private fun AddIngredientDialog(ingredients: List<Ingredient>, onDismiss: () -> Unit, onAdd: (Ingredient, Double) -> Unit) {
    var chosen by remember { mutableStateOf<Ingredient?>(null) }
    var q by remember { mutableStateOf(1.0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(chosen?.name ?: "Choisir un ingrédient") },
        text = {
            val c = chosen
            if (c == null) Column {
                if (ingredients.isEmpty()) Text("Tous les ingrédients sont déjà dans la recette.")
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(ingredients, key = { it.id }) { ing ->
                        Row(
                            Modifier.fillMaxWidth().clickable { chosen = ing; q = defaultDose(ing) }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(ing.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(ing.basePriceLabel(), color = C.Gris, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                NumberField(q, { q = it }, "Dose pour 1 portion", suffix = c.unit.base)
            }
        },
        confirmButton = {
            val c = chosen
            if (c != null) TextButton(onClick = { onAdd(c, q) }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

private fun defaultDose(i: Ingredient): Double = when (i.unit.base) {
    "g" -> 5.0
    "ml" -> 100.0
    "botte" -> 0.1
    else -> 1.0
}
