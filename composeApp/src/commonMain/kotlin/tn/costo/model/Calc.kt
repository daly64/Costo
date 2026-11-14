package tn.costo.model

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToLong

data class LineCost(val line: RecipeLine, val ingredient: Ingredient?, val cost: Double)

data class CostBreakdown(
    val lines: List<LineCost>,
    val ingredients: Double,
    val losses: Double,
    val fixed: Double,
) {
    val total: Double get() = ingredients + losses + fixed
}

fun AppData.ingredient(id: String): Ingredient? = ingredients.firstOrNull { it.id == id }

fun AppData.breakdown(p: Product): CostBreakdown {
    val lines = p.recipe.map { l ->
        val ing = ingredient(l.ingredientId)
        LineCost(l, ing, (ing?.pricePerBase ?: 0.0) * l.qty)
    }
    val sum = lines.sumOf { it.cost }
    val loss = if (p.kind == Kind.REVENTE) 0.0 else sum * settings.lossPct / 100.0
    val fixed = if (p.kind == Kind.BOISSON) settings.fixedCost else 0.0
    return CostBreakdown(lines, sum, loss, fixed)
}

fun AppData.cost(p: Product): Double = breakdown(p).total

fun AppData.marginFor(p: Product): Int = p.margin ?: settings.defaultMargin

/** Prix de vente pour obtenir la marge voulue (marge sur le prix de vente). */
fun suggestedPrice(cost: Double, marginPct: Int): Double =
    if (marginPct >= 100) cost else cost / (1.0 - marginPct / 100.0)

/** Arrondi au 100 millimes supérieur. */
fun roundUpPrice(price: Double): Double = ceil(price * 10.0 - 1e-9) / 10.0

/** Marge réelle en % du prix de vente, null si pas de prix. */
fun realMargin(cost: Double, salePrice: Double): Double? =
    if (salePrice <= 0.0) null else (salePrice - cost) / salePrice * 100.0

/** 1.5 -> "1,500" (dinars avec millimes). */
fun dt(x: Double): String {
    val m = (x * 1000.0).roundToLong()
    val sign = if (m < 0) "-" else ""
    val a = abs(m)
    return sign + (a / 1000).toString() + "," + (a % 1000).toString().padStart(3, '0')
}

/** Nombre court : 8.0 -> "8", 0.1 -> "0,1", 7.5 -> "7,5". */
fun num(x: Double): String {
    val r = (x * 1000.0).roundToLong()
    val whole = r / 1000
    val frac =(abs(r) % 1000).toString().padStart(3, '0').trimEnd('0')
    val sign = if (r < 0 && whole == 0L) "-" else ""
    return if (frac.isEmpty()) sign + whole.toString() else "$sign$whole,$frac"
}

fun pct(x: Double): String = (x).roundToLong().toString()

/** Accepte "1,5", "1.5", " 2 " ; null si illisible. */
fun parseNumber(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

fun Ingredient.packLabel(): String =
    if (packQty == 1.0) "le ${unit.label}" else "les ${num(packQty)} ${unit.label}"

fun Ingredient.basePriceLabel(): String = when (unit) {
    Unite.KG, Unite.G -> "${dt(pricePerBase * 1000)} DT/kg"
    Unite.L, Unite.ML -> "${dt(pricePerBase * 1000)} DT/L"
    Unite.PIECE -> "${dt(pricePerBase)} DT/pièce"
    Unite.BOTTE -> "${dt(pricePerBase)} DT/botte"
}

fun doseLabel(qty: Double, ing: Ingredient?): String = "${num(qty)} ${ing?.unit?.base ?: ""}".trim()

fun cleanName(s: String): String {
    val t = s.trim().replace(Regex("\\s+"), " ")
    return if (t.isEmpty()) t else t.substring(0, 1).uppercase() + t.substring(1)
}
