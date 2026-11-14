package tn.costo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import tn.costo.ui.C
import tn.costo.ui.CostoTheme
import tn.costo.ui.CupIcon
import tn.costo.ui.DrinkDetailScreen
import tn.costo.ui.DrinksScreen
import tn.costo.ui.IngredientsScreen
import tn.costo.ui.Onboarding
import tn.costo.ui.SettingsScreen

enum class TabId { BOISSONS, INGREDIENTS, REGLAGES }

sealed interface Route {
    data class Tab(val tab: TabId) : Route
    data class Drink(val id: String) : Route
}

/** Navigation simple : une pile d'écrans. */
class Nav {
    var stack: List<Route> by mutableStateOf(listOf(Route.Tab(TabId.BOISSONS)))
        private set
    val current: Route get() = stack.last()
    val tab: TabId get() = stack.filterIsInstance<Route.Tab>().lastOrNull()?.tab ?: TabId.BOISSONS
    fun go(r: Route) { stack = stack + r }
    fun tab(t: TabId) { stack = listOf(Route.Tab(t)) }
    /** Retour en arrière ; false s'il n'y a plus rien (l'app peut se fermer). */
    fun back(): Boolean {
        if (stack.size > 1) { stack = stack.dropLast(1); return true }
        if (tab != TabId.BOISSONS) { tab(TabId.BOISSONS); return true }
        return false
    }
}

/**
 * Point d'entrée commun.
 * startScreen (captures d'écran) : bienvenue, boissons, fiche, ingredients, reglages.
 */
@Composable
fun App(platform: Platform, nav: Nav, startScreen: String? = null) {
    val store = remember { Store(platform) }
    val scope = rememberCoroutineScope()
    val sync = remember { CloudSync(store, platform, scope) }
    LaunchedEffect(sync) { sync.loop() }
    var welcomeClosed by remember { mutableStateOf(false) }
    LaunchedEffect(startScreen) {
        when (startScreen) {
            "ingredients" -> nav.tab(TabId.INGREDIENTS)
            "reglages" -> nav.tab(TabId.REGLAGES)
            "fiche" -> nav.go(Route.Drink("p47"))
        }
    }
    val showWelcome = when (startScreen) {
        null -> !store.data.settings.onboardingDone
        "bienvenue" -> !welcomeClosed
        else -> false
    }
    CostoTheme {
        Surface(Modifier.fillMaxSize(), color = C.Fond) {
            if (showWelcome) {
                Onboarding(onDone = {
                    store.settings { it.copy(onboardingDone = true) }
                    welcomeClosed = true
                })
            } else {
                MainScreen(store, nav, platform, sync)
            }
        }
    }
}

@Composable
private fun MainScreen(store: Store, nav: Nav, platform: Platform, sync: CloudSync) {
    val route = nav.current
    Scaffold(
        containerColor = C.Fond,
        bottomBar = {
            if (route is Route.Tab) {
                NavigationBar(containerColor = C.Carte) {
                    val colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = C.Bleu, selectedTextColor = C.Bleu, indicatorColor = C.BleuClair,
                        unselectedIconColor = C.Gris, unselectedTextColor = C.Gris,
                    )
                    NavigationBarItem(
                        selected = nav.tab == TabId.BOISSONS, onClick = { nav.tab(TabId.BOISSONS) },
                        icon = { CupIcon(if (nav.tab == TabId.BOISSONS) C.Bleu else C.Gris) },
                        label = { Text("Boissons") }, colors = colors,
                    )
                    NavigationBarItem(
                        selected = nav.tab == TabId.INGREDIENTS, onClick = { nav.tab(TabId.INGREDIENTS) },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("Ingrédients") }, colors = colors,
                    )
                    NavigationBarItem(
                        selected = nav.tab == TabId.REGLAGES, onClick = { nav.tab(TabId.REGLAGES) },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Réglages") }, colors = colors,
                    )
                }
            }
        },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when (route) {
                is Route.Drink -> DrinkDetailScreen(store, route.id, onBack = { nav.back() })
                is Route.Tab -> when (route.tab) {
                    TabId.BOISSONS -> DrinksScreen(store, onOpen = { nav.go(Route.Drink(it)) })
                    TabId.INGREDIENTS -> IngredientsScreen(store)
                    TabId.REGLAGES -> SettingsScreen(store, platform, sync)
                }
            }
        }
    }
}
