package tn.costo.model

/**
 * Données de départ : prix d'achat donnés par le gérant (nov. 2026),
 * produits repris du menu Zicafe, et doses standard.
 * Doses : espresso 7 à 9 g de café (8 g retenus), cappuccino 100 à 150 ml de lait,
 * 1 bâtonnet de sucre = 5 g. Tout reste modifiable dans l'app.
 */
object Seed {
    private fun ing(id: String, name: String, price: Double, qty: Double, unit: Unite) =
        Ingredient(id, name, price, qty, unit)

    val ingredients = listOf(
        ing("cafe", "Café en grains", 38.0, 1.0, Unite.KG),
        ing("sucre", "Sucre en bâtonnets", 38.0, 7.0, Unite.KG),
        ing("lait", "Lait", 1.3, 1.0, Unite.L),
        ing("the_vert", "Thé vert", 3.0, 150.0, Unite.G),
        ing("the_rouge", "Thé rouge", 3.0, 150.0, Unite.G),
        ing("menthe", "Menthe fraîche", 0.5, 1.0, Unite.BOTTE),
        ing("chocolat", "Chocolat en poudre", 1.5, 100.0, Unite.G),
        ing("sirop", "Sirop", 15.0, 1.0, Unite.L),
        ing("gobelet", "Gobelets", 2.0, 50.0, Unite.PIECE),
        ing("jus_citron", "Jus de citron", 10.0, 1.0, Unite.L),
        ing("jus_fraise", "Jus de fraise", 10.0, 1.0, Unite.L),
        ing("jus_poire", "Jus de poire", 10.0, 1.0, Unite.L),
        ing("jus_citron_menthe", "Jus citron menthe", 10.0, 1.0, Unite.L),
        ing("eau_1l", "Eau minérale 1 L", 4.5, 6.0, Unite.PIECE),
        ing("eau_05l", "Eau minérale 0,5 L", 4.0, 6.0, Unite.PIECE),
        ing("canette", "Canettes", 30.0, 24.0, Unite.PIECE),
        ing("viennoiserie", "Viennoiserie", 1.5, 1.0, Unite.PIECE),
        ing("gateau", "Gâteau du jour", 2.5, 1.0, Unite.PIECE),
    )

    private fun r(vararg l: Pair<String, Double>) = l.map { RecipeLine(it.first, it.second) }

    private fun p(
        menuId: String, name: String, cat: String, price: Double, hidden: Boolean,
        kind: Kind, recipe: List<RecipeLine>,
    ) = Product("p$menuId", name, cat, kind, price, menuId, hidden, recipe)

    const val CAFES = "Cafés"
    const val BOISSONS = "Boissons"
    const val VIENNOISERIES = "Viennoiseries"
    const val SUPPLEMENTS = "Suppléments"

    val products = listOf(
        p("14", "Express", CAFES, 2.0, false, Kind.BOISSON, r("cafe" to 8.0, "sucre" to 5.0)),
        p("15", "Demi", CAFES, 2.0, false, Kind.BOISSON, r("cafe" to 8.0, "lait" to 40.0, "sucre" to 5.0)),
        p("17", "Allongé", CAFES, 2.0, false, Kind.BOISSON, r("cafe" to 8.0, "sucre" to 5.0)),
        p("18", "Américain", CAFES, 2.0, false, Kind.BOISSON, r("cafe" to 10.0, "sucre" to 5.0)),
        p("47", "Cappuccino", CAFES, 2.5, false, Kind.BOISSON, r("cafe" to 8.0, "lait" to 120.0, "sucre" to 5.0)),
        p("19", "Direct", CAFES, 2.5, false, Kind.BOISSON, r("cafe" to 8.0, "lait" to 150.0, "sucre" to 5.0)),
        p("20", "Café au lait", CAFES, 2.0, false, Kind.BOISSON, r("cafe" to 8.0, "lait" to 120.0, "sucre" to 5.0)),
        p("21", "Chocolat chaud", CAFES, 3.0, true, Kind.BOISSON, r("chocolat" to 25.0, "lait" to 200.0, "sucre" to 5.0)),
        p("48", "Café latte glacé", CAFES, 5.0, true, Kind.BOISSON, r("cafe" to 16.0, "lait" to 200.0, "sucre" to 10.0)),
        p("49", "Thé à la menthe", CAFES, 2.0, true, Kind.BOISSON, r("the_vert" to 3.0, "menthe" to 0.1, "sucre" to 15.0)),
        p("43", "Eau minérale 1 L", BOISSONS, 2.0, false, Kind.REVENTE, r("eau_1l" to 1.0)),
        p("33", "Canette", BOISSONS, 2.5, false, Kind.REVENTE, r("canette" to 1.0)),
        p("36", "Jus de citron", BOISSONS, 3.0, true, Kind.BOISSON, r("jus_citron" to 250.0, "sucre" to 5.0)),
        p("37", "Jus de fraise", BOISSONS, 3.0, true, Kind.BOISSON, r("jus_fraise" to 250.0)),
        p("38", "Jus citron menthe", BOISSONS, 3.0, true, Kind.BOISSON, r("jus_citron_menthe" to 250.0)),
        p("39", "Jus de poire", BOISSONS, 3.0, true, Kind.BOISSON, r("jus_poire" to 250.0)),
        p("34", "Gâteau du jour", VIENNOISERIES, 4.0, true, Kind.REVENTE, r("gateau" to 1.0)),
        p("40", "Croissant", VIENNOISERIES, 2.0, true, Kind.REVENTE, r("viennoiserie" to 1.0)),
        p("41", "Pain au chocolat", VIENNOISERIES, 2.0, true, Kind.REVENTE, r("viennoiserie" to 1.0)),
        p("42", "Muffin", VIENNOISERIES, 2.0, true, Kind.REVENTE, r("viennoiserie" to 1.0)),
        p("d10d4264-6771-4513-845b-df242af807b5", "Mille-feuille", VIENNOISERIES, 2.0, true, Kind.REVENTE, r("viennoiserie" to 1.0)),
        p("61c8d643-74e4-4fc4-9f12-5c166faea461", "Cake", VIENNOISERIES, 2.0, false, Kind.REVENTE, r("viennoiserie" to 1.0)),
        p("44", "Eau minérale 0,5 L", SUPPLEMENTS, 1.0, true, Kind.REVENTE, r("eau_05l" to 1.0)),
        p("45", "Dose de sirop", SUPPLEMENTS, 1.0, false, Kind.SUPPLEMENT, r("sirop" to 20.0)),
        p("46", "Gobelet", SUPPLEMENTS, 0.1, false, Kind.REVENTE, r("gobelet" to 1.0)),
    )

    fun data() = AppData(ingredients = ingredients, products = products)
}
