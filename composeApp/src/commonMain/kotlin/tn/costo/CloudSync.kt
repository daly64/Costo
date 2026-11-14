package tn.costo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import tn.costo.model.AppData

/** Adresse et clé du serveur de synchronisation (projet Vercel costo-api, base Neon). */
object SyncConfig {
    const val URL = "https://costo-api-omega.vercel.app/api/costo"
    const val KEY = "_7W7xEvWQf2Uu9ws-8R8JQgfcFRpKu42"
}

@Serializable
private data class Envelope(val updatedAt: Long = 0, val data: AppData? = null)

/**
 * Synchronisation téléphone / PC : la version la plus récente gagne.
 * Au démarrage puis chaque minute, l'app compare sa version à celle du serveur ;
 * après une modification, elle l'envoie au bout de 2 secondes.
 */
class CloudSync(private val store: Store, private val platform: Platform, private val scope: CoroutineScope) {

    var status: String by mutableStateOf("En attente")
        private set
    var ok: Boolean by mutableStateOf(false)
        private set

    private val lock = Mutex()
    private var pending: Job? = null

    init {
        store.onLocalChange = {
            pending?.cancel()
            pending = scope.launch { delay(2_000); syncNow() }
        }
    }

    suspend fun syncNow() = lock.withLock {
        if (!store.data.settings.cloudSync) {
            status = "Désactivée"; ok = false
            return@withLock
        }
        val headers = mapOf("x-costo-key" to SyncConfig.KEY, "Content-Type" to "application/json")
        try {
            val (code, body) = platform.httpRequest("GET", SyncConfig.URL, null, headers)
            if (code != 200) error("le serveur a répondu $code")
            val remote = CostoJson.decodeFromString(Envelope.serializer(), body)
            val local = store.data
            when {
                remote.data != null && remote.updatedAt > local.updatedAt ->
                    store.replaceFromCloud(remote.data, remote.updatedAt)
                local.updatedAt > remote.updatedAt -> {
                    val out = CostoJson.encodeToString(Envelope.serializer(), Envelope(local.updatedAt, local))
                    val (c, _) = platform.httpRequest("PUT", SyncConfig.URL, out, headers)
                    if (c != 200 && c != 409) error("envoi refusé ($c)")
                }
            }
            status = "Synchronisé à ${platform.timeLabel()}"
            ok = true
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            status = "Hors ligne : ${e.message ?: "réseau indisponible"}"
            ok = false
        }
    }

    /** Synchronise au démarrage puis toutes les minutes tant que l'app est ouverte. */
    suspend fun loop() {
        while (true) {
            syncNow()
            delay(60_000)
        }
    }
}
