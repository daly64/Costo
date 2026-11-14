package tn.costo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import tn.costo.model.AppData
import tn.costo.model.Ingredient
import tn.costo.model.Kind
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

    /** Appelé après chaque modification locale (pour la synchronisation). */
    var onLocalChange: (() -> Unit)? = null

    private fun load(): AppData {
        val text = runCatching { platform.loadData() }.getOrNull()
        if (text.isNullOrBlank()) return Seed.data()
        val d = runCatching { CostoJson.decodeFromString(AppData.serializer(), text) }.getOrElse { return Seed.data() }
        if (d.schema >= AppData.SCHEMA) return d
        val migrated = migrate(d)
        persist(migrated)
        return migrated
    }

    /**
     * Version 1.0.1 : coût fixe 100 millimes, pertes 20 %, main-d'œuvre 250 millimes,
     * 2 bâtonnets de sucre (10 g) dans les cafés. Les valeurs déjà modifiées à la main sont gardées.
     */
    private fun migrate(d: AppData): AppData {
        val s = d.settings
        val settings = s.copy(
            fixedCost = if (s.fixedCost == 0.080) 0.100 else s.fixedCost,
            lossPct = if (s.lossPct == 5.0) 20.0 else s.lossPct,
        )
        val products = d.products.map { p ->
            val isCoffee = p.kind == Kind.BOISSON && p.recipe.any { it.ingredientId == "cafe" }
            if (!isCoffee) p
            else p.copy(recipe = p.recipe.map { if (it.ingredientId == "sucre" && it.qty == 5.0) it.copy(qty = 10.0) else it })
        }
        return d.copy(schema = AppData.SCHEMA, settings = settings, products = products, updatedAt = platform.nowMillis())
    }

    private fun persist(d: AppData) {
        runCatching { platform.saveData(CostoJson.encodeToString(AppData.serializer(), d)) }
    }

    fun update(change: (AppData) -> AppData) {
        data = change(data).copy(updatedAt = platform.nowMillis())
        persist(data)
        onLocalChange?.invoke()
    }

    /** Remplace les données par la version reçue d'un autre appareil (sans la renvoyer). */
    fun replaceFromCloud(remote: AppData, remoteUpdatedAt: Long) {
        val keep = data.settings
        data = remote.copy(
            schema = AppData.SCHEMA,
            updatedAt = remoteUpdatedAt,
            settings = remote.settings.copy(onboardingDone = keep.onboardingDone),
        )
        persist(data)
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
        val restored = if (d.schema < AppData.SCHEMA) migrate(d) else d
        update { restored.copy(settings = restored.settings.copy(onboardingDone = true)) }
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
