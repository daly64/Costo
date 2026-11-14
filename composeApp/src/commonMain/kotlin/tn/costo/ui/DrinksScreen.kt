package tn.costo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.costo.Store
import tn.costo.model.Product
import tn.costo.model.Seed
import tn.costo.model.cost
import tn.costo.model.marginFor
import tn.costo.model.realMargin

@Composable
fun DrinksScreen(store: Store, onOpen: (String) -> Unit) {
    val d = store.data
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    val categories = remember(d.products) {
        val known = listOf(Seed.CAFES, Seed.BOISSONS, Seed.VIENNOISERIES, Seed.SUPPLEMENTS)
        val all = d.products.map { it.category }.distinct()
        known.filter { it in all } + all.filterNot { it in known }
    }
    val list = d.products.filter {
        (category == null || it.category == category) &&
            (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true))
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { BrandHeader("Boissons", "Coût de revient par tasse et marge") }
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Rechercher une boisson") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = C.Gris) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = C.Bord, focusedBorderColor = C.Bleu,
                        unfocusedContainerColor = C.Carte, focusedContainerColor = C.Carte,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryChip("Tout", category == null) { category = null }
                    categories.forEach { c -> CategoryChip(c, category == c) { category = if (category == c) null else c } }
                }
            }
            items(list, key = { it.id }) { p -> DrinkRow(store, p) { onOpen(p.id) } }
            if (list.isEmpty()) item {
                Text("Aucune boisson trouvée.", color = C.Gris, modifier = Modifier.padding(20.dp))
            }
        }
        ExtendedFloatingActionButton(
            onClick = {
                val id = store.newId("n")
                store.saveProduct(Product(id, "Nouvelle boisson", category ?: Seed.CAFES))
                onOpen(id)
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nouvelle boisson") },
            containerColor = C.Bleu, contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label, fontWeight = FontWeight.Bold) },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = C.Carte, labelColor = C.Texte,
            selectedContainerColor = C.Bleu, selectedLabelColor = Color.White,
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = C.Bord, selectedBorderColor = C.Bleu),
    )
}

@Composable
private fun DrinkRow(store: Store, p: Product, onClick: () -> Unit) {
    val d = store.data
    val c = d.cost(p)
    val m = if (p.recipe.isEmpty()) null else realMargin(c, p.salePrice)
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 64.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = C.Carte),
        border = BorderStroke(1.dp, C.Bord),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false))
                    if (p.hidden) {
                        Spacer(Modifier.width(6.dp))
                        Text("masqué", fontSize = 11.sp, color = C.Gris, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    if (p.recipe.isEmpty()) "Recette à compléter · Prix ${money(p.salePrice)}"
                    else "Coût ${money(c)} · Prix ${money(p.salePrice)}",
                    fontSize = 13.sp, color = C.Gris,
                )
            }
            Spacer(Modifier.width(8.dp))
            MarginBadge(m, d.marginFor(p))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = C.Gris)
        }
    }
}
