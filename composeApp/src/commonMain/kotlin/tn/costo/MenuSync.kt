package tn.costo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import tn.costo.model.AppData
import tn.costo.model.Kind
import tn.costo.model.Product
import tn.costo.model.Seed
import tn.costo.model.cleanName

data class MenuItem(val id: String, val name: String, val category: String, val price: Double, val visible: Boolean)

data class SyncResult(val updated: Int, val added: Int)

/** Lit le menu du serveur d'affichage : liste de catégories contenant des produits. */
fun parseMenu(text: String): List<MenuItem> {
    val root = CostoJson.parseToJsonElement(text)
    val categories = findCategories(root) ?: return emptyList()
    val out = mutableListOf<MenuItem>()
    for (c in categories) {
        val cat = c as? JsonObject ?: continue
        val catName = cat.str("name") ?: ""
        val items = (cat["products"] ?: cat["items"]) as? JsonArray ?: continue
        for (it in items) {
            val o = it as? JsonObject ?: continue
            val id = o.str("id") ?: continue
            val name = o.str("name") ?: continue
            val price = (o["price"] as? JsonPrimitive)?.let { p -> p.doubleOrNull ?: p.contentOrNull?.replace(',', '.')?.toDoubleOrNull() } ?: 0.0
            val visible = (o["isVisible"] as? JsonPrimitive)?.booleanOrNull
                ?: (o["visible"] as? JsonPrimitive)?.booleanOrNull ?: true
            out += MenuItem(id, cleanName(name), cleanName(catName), price, visible)
        }
    }
    return out
}

private fun JsonObject.str(k: String): String? = (this[k] as? JsonPrimitive)?.contentOrNull

private fun findCategories(e: JsonElement): JsonArray? = when (e) {
    is JsonArray -> if (e.any { (it as? JsonObject)?.let { o -> "products" in o || "items" in o } == true }) e else null
    is JsonObject -> listOf("categories", "menu", "data").firstNotNullOfOrNull { k -> e[k]?.let { findCategories(it) } }
    else -> null
}

private fun categoryLabel(menuCat: String): String {
    val c = menuCat.lowercase()
    return when {
        "caf" in c -> Seed.CAFES
        "venoi" in c || "vienn" in c -> Seed.VIENNOISERIES
        "suppl" in c -> Seed.SUPPLEMENTS
        "boisson" in c -> Seed.BOISSONS
        else -> menuCat
    }
}

private fun norm(s: String) = s.lowercase().filter { it.isLetterOrDigit() }

/** Met à jour prix de vente et visibilité ; ajoute les produits nouveaux sans recette. */
fun applyMenu(d: AppData, items: List<MenuItem>, newId: (Int) -> String): Pair<AppData, SyncResult> {
    var updated = 0
    var added = 0
    val products = d.products.toMutableList()
    items.forEachIndexed { i, m ->
        val idx = products.indexOfFirst { it.menuId == m.id }
            .takeIf { it >= 0 } ?: products.indexOfFirst { it.menuId == null && norm(it.name) == norm(m.name) }
        if (idx >= 0) {
            val p = products[idx]
            if (p.salePrice != m.price || p.hidden == m.visible || p.menuId != m.id) updated++
            products[idx] = p.copy(salePrice = m.price, hidden = !m.visible, menuId = m.id)
        } else {
            val cat = categoryLabel(m.category)
            val kind = if (cat == Seed.VIENNOISERIES) Kind.REVENTE else Kind.BOISSON
            products += Product(newId(i), m.name, cat, kind, m.price, m.id, !m.visible)
            added++
        }
    }
    return d.copy(products = products) to SyncResult(updated, added)
}
