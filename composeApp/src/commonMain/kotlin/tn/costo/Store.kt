package tn.costo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import tn.costo.model.AppData
import tn.costo.model.Ingredient
import tn.costo.model.PricePoint
import tn.costo.model.Product
import tn.costo.model.Seed
import tn.costo.model.Settings

val CostoJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

/** État de l'app : chargé au démarrage, enregistré à chaque modification. */
class Store(private val platform: Platform) {

    var data: AppData by mutableStateOf(load())
        private set

    private fun load(): AppData {
        val text = runCatching { platform.loadData() }.getOrNull()
        if (text.isNullOrBlank()) return Seed.data()
        return runCatching { CostoJson.decodeFromString(AppData.serializer(), text) }
            .getOrElse { Seed.data() }
    }

    fun update(change: (AppData) -> AppData) {
        data = change(data)
        runCatching { platform.saveData(CostoJson.encodeToString(AppData.serializer(), data)) }
    }

    fun settings(change: (Settings) -> Settings) = update { it.copy(settings = change(it.settings)) }

    fun saveProduct(p: Product) = update { d ->
        val exists = d.products.any { it.id == p.id }
        d.copy(products = if (exists) d.products.map { if (it.id == p.id) p else it } else d.products + p)
    }

    fun deleteProduct(id: String) = update { d -> d.copy(products = d.products.filterNot { it.id == id }) }

    /** Enregistre un ingrédient ; garde l'ancien prix dans l'historique s'il a changé. */
    fun saveIngredient(ing: Ingredient) = update { d ->
        val old = d.ingredients.firstOrNull { it.id == ing.id }
        val withHistory = if (old != null &&
            (old.packPrice != ing.packPrice || old.packQty != ing.packQty || old.unit != ing.unit)
        ) {
            val start = if (old.history.isEmpty())
                listOf(PricePoint("Avant", old.packPrice, old.packQty, old.unit)) else old.history
            ing.copy(history = start + PricePoint(platform.today(), ing.packPrice, ing.packQty, ing.unit))
        } else ing.copy(history = old?.history ?: ing.history)
        d.copy(ingredients = if (old != null) d.ingredients.map { if (it.id == ing.id) withHistory else it } else d.ingredients + withHistory)
    }

    fun deleteIngredient(id: String) = update { d ->
        d.copy(
            ingredients = d.ingredients.filterNot { it.id == id },
            products = d.products.map { p -> p.copy(recipe = p.recipe.filterNot { it.ingredientId == id }) },
        )
    }

    fun backupJson(): String = CostoJson.encodeToString(AppData.serializer(), data)

    /** Remplace toutes les données par une sauvegarde ; false si le fichier n'est pas lisible. */
    fun restore(json: String): Boolean {
        val d = runCatching { CostoJson.decodeFromString(AppData.serializer(), json) }.getOrNull() ?: return false
        if (d.ingredients.isEmpty() && d.products.isEmpty()) return false
        update { d.copy(settings = d.settings.copy(onboardingDone = true)) }
        return true
    }

    fun newId(prefix: String): String {
        var i = data.products.size + data.ingredients.size + 1
        val used = (data.products.map { it.id } + data.ingredients.map { it.id }).toSet()
        while ("$prefix$i" in used) i++
        return "$prefix$i"
    }

    val today: String get() = platform.today()
}
