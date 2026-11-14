package tn.costo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tn.costo.Platform
import tn.costo.Store
import tn.costo.applyMenu
import tn.costo.parseMenu

@Composable
fun SettingsScreen(store: Store, platform: Platform) {
    val s = store.data.settings
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncMsg by remember { mutableStateOf<String?>(null) }
    var backupMsg by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
        BrandHeader("Réglages", "Calcul, menu et sauvegarde")
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Section("Calcul du coût") {
                NumberField(s.fixedCost, { v -> store.settings { it.copy(fixedCost = v) } }, "Coût fixe par tasse", suffix = "DT")
                Text("Électricité, eau, gaz et nettoyage. Pour l'affiner : factures du mois ÷ tasses vendues dans le mois.",
                    fontSize = 13.sp, color = C.Gris)
                NumberField(s.lossPct, { v -> store.settings { it.copy(lossPct = v) } }, "Pertes", suffix = "%")
                NumberField(s.defaultMargin.toDouble(), { v -> store.settings { it.copy(defaultMargin = v.toInt().coerceIn(0, 95)) } },
                    "Marge visée par défaut", suffix = "%", decimals = false)
            }

            Section("Menu affiché") {
                TextInput(s.menuUrl, { v -> store.settings { it.copy(menuUrl = v.trim()) } }, "Adresse du menu")
                Button(
                    onClick = {
                        syncing = true; syncMsg = null
                        scope.launch {
                            syncMsg = try {
                                val items = parseMenu(platform.httpGet(store.data.settings.menuUrl))
                                if (items.isEmpty()) "Aucun produit trouvé à cette adresse."
                                else {
                                    var result: tn.costo.SyncResult? = null
                                    store.update { d ->
                                        val (nd, r) = applyMenu(d, items) { i -> "m${d.products.size + i + 1}" }
                                        result = r
                                        nd.copy(settings = nd.settings.copy(lastSync = platform.today()))
                                    }
                                    val r = result!!
                                    "${items.size} produits lus · ${r.updated} mis à jour · ${r.added} ajoutés"
                                }
                            } catch (e: Throwable) {
                                "Synchronisation impossible : ${e.message ?: "réseau indisponible"}"
                            }
                            syncing = false
                        }
                    },
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = C.Bleu, contentColor = Color.White),
                ) { Text(if (syncing) "Synchronisation…" else "Récupérer les prix de vente du menu") }
                Text(syncMsg ?: (s.lastSync?.let { "Dernière synchronisation : $it" } ?: "Met à jour les prix et ajoute les nouveaux produits du menu."),
                    fontSize = 13.sp, color = C.Gris)
            }

            Section("Sauvegarde") {
                OutlinedButton(
                    onClick = {
                        val name = "costo-sauvegarde-" + platform.today().replace('/', '-') + ".json"
                        platform.exportBackup(name, store.backupJson()) { ok ->
                            backupMsg = if (ok) "Sauvegarde enregistrée : $name" else "Sauvegarde annulée."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, C.BleuPale),
                ) { Text("Exporter une sauvegarde", color = C.Bleu, fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = { platform.importBackup { json -> if (json != null) pendingRestore = json } },
                    modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, C.BleuPale),
                ) { Text("Restaurer une sauvegarde", color = C.Bleu, fontWeight = FontWeight.Bold) }
                Text(backupMsg ?: "Garde une copie de tes prix et recettes, par exemple avant de changer de téléphone.",
                    fontSize = 13.sp, color = C.Gris)
            }

            Section("Application") {
                TextButton(onClick = { store.settings { it.copy(onboardingDone = false) } }) {
                    Text("Revoir l'écran de bienvenue", color = C.Bleu, fontWeight = FontWeight.Bold)
                }
                Text("Costo ${platform.versionName}", fontSize = 13.sp, color = C.Gris)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    pendingRestore?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restaurer la sauvegarde ?") },
            text = { Text("Tes ingrédients, recettes et réglages actuels seront remplacés par ceux du fichier.") },
            confirmButton = {
                TextButton(onClick = {
                    backupMsg = if (store.restore(json)) "Sauvegarde restaurée." else "Ce fichier n'est pas une sauvegarde Costo."
                    pendingRestore = null
                }) { Text("Restaurer") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Annuler") } },
        )
    }
}
