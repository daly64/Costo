package tn.costo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val nav = Nav()
    private var exportJson: String? = null
    private var exportDone: ((Boolean) -> Unit)? = null
    private var importDone: ((String?) -> Unit)? = null

    private val createDoc = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val cb = exportDone
        exportDone = null
        val json = exportJson
        exportJson = null
        val ok = uri != null && json != null && runCatching {
            contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } != null
        }.getOrDefault(false)
        cb?.invoke(ok)
    }

    private val openDoc = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val cb = importDone
        importDone = null
        val text = uri?.let { u ->
            runCatching { contentResolver.openInputStream(u)?.use { it.readBytes().toString(Charsets.UTF_8) } }.getOrNull()
        }
        cb?.invoke(text)
    }

    private val platform = object : Platform {
        override val versionName: String
            get() = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: ""

        private val dataFile get() = File(filesDir, "costo-data.json")

        override fun loadData(): String? = dataFile.takeIf { it.exists() }?.readText()

        override fun saveData(json: String) {
            // Écriture sûre : fichier temporaire puis remplacement.
            val tmp = File(filesDir, "costo-data.json.tmp")
            tmp.writeText(json)
            if (!tmp.renameTo(dataFile)) {
                dataFile.writeText(json)
                tmp.delete()
            }
        }

        override fun today(): String = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())

        override suspend fun httpGet(url: String): String = withContext(Dispatchers.IO) {
            val c = URL(url).openConnection() as HttpURLConnection
            try {
                c.connectTimeout = 10_000
                c.readTimeout = 15_000
                c.setRequestProperty("Accept", "application/json")
                val code = c.responseCode
                if (code !in 200..299) throw IOException("le serveur a répondu $code")
                c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            } finally {
                c.disconnect()
            }
        }

        override fun exportBackup(fileName: String, json: String, onDone: (Boolean) -> Unit) {
            exportJson = json
            exportDone = onDone
            createDoc.launch(fileName)
        }

        override fun importBackup(onResult: (String?) -> Unit) {
            importDone = onResult
            openDoc.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        goFullScreen()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!nav.back()) finish()
            }
        })
        // Écran de départ forcé (utilisé pour les captures d'écran automatiques).
        val start = intent?.getStringExtra("costo.screen")
        setContent { App(platform, nav, start) }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goFullScreen()
    }

    /** Plein écran : barres système cachées, réapparaissent d'un glissement. */
    private fun goFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val c = WindowCompat.getInsetsController(window, window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
