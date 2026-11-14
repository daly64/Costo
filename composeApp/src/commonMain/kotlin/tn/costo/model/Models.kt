package tn.costo.model

import kotlinx.serialization.Serializable

/** Unité d'achat. Les doses des recettes sont toujours en unité de base (g, ml, pièce, botte). */
@Serializable
enum class Unite(val label: String, val base: String, val factor: Double) {
    KG("kg", "g", 1000.0),
    G("g", "g", 1.0),
    L("L", "ml", 1000.0),
    ML("ml", "ml", 1.0),
    PIECE("pièce", "pièce", 1.0),
    BOTTE("botte", "botte", 1.0),
}

/** Un prix d'achat noté à une date (historique). */
@Serializable
data class PricePoint(
    val date: String,
    val packPrice: Double,
    val packQty: Double,
    val unit: Unite,
)

/** Matière première : achetée en paquet (ex. 38 DT le paquet de 7 kg). */
@Serializable
data class Ingredient(
    val id: String,
    val name: String,
    val packPrice: Double,
    val packQty: Double,
    val unit: Unite,
    val history: List<PricePoint> = emptyList(),
) {
    /** Prix d'une unité de base (1 g, 1 ml, 1 pièce, 1 botte). */
    val pricePerBase: Double
        get() = if (packQty <= 0.0) 0.0 else packPrice / (packQty * unit.factor)
}

@Serializable
enum class Kind(val label: String, val help: String) {
    BOISSON("Boisson", "Pertes, coût fixe et main-d'œuvre inclus"),
    REVENTE("Revente", "Produit revendu tel quel, sans pertes ni coût fixe"),
    SUPPLEMENT("Supplément", "Pertes incluses, sans coût fixe"),
}

@Serializable
data class RecipeLine(
    val ingredientId: String,
    val qty: Double,
)

/** Un produit du menu avec sa recette pour 1 portion. */
@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val kind: Kind = Kind.BOISSON,
    val salePrice: Double = 0.0,
    val menuId: String? = null,
    val hidden: Boolean = false,
    val recipe: List<RecipeLine> = emptyList(),
    /** Marge visée en %, null = marge par défaut des réglages. */
    val margin: Int? = null,
)

@Serializable
data class Settings(
    /** Électricité, eau, gaz, nettoyage… par tasse, en DT. */
    val fixedCost: Double = 0.100,
    /** Pertes (café renversé, lait jeté…), en %. */
    val lossPct: Double = 20.0,
    /** Main-d'œuvre par tasse, en DT (salaires du mois ÷ tasses du mois). */
    val laborCost: Double = 0.250,
    val defaultMargin: Int = 70,
    val menuUrl: String = "https://menu-board-api-murex.vercel.app/menu",
    val onboardingDone: Boolean = false,
    val lastSync: String? = null,
    /** Synchronisation téléphone / PC par Internet. */
    val cloudSync: Boolean = true,
)

@Serializable
data class AppData(
    val schema: Int = AppData.SCHEMA,
    /** Heure de la dernière modification (ms), pour savoir quelle version est la plus récente. */
    val updatedAt: Long = 0,
    val ingredients: List<Ingredient> = emptyList(),
    val products: List<Product> = emptyList(),
    val settings: Settings = Settings(),
) {
    companion object {
        const val SCHEMA = 2
    }
}
