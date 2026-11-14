package tn.costo

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Version PC : à compiler sur l'ordinateur avec `gradlew :composeApp:run`. */
object DesktopPlatform : Platform {
    override val versionName = "1.0.1 (PC)"

    private val dir = File(System.getProperty("user.home"), ".costo").apply { mkdirs() }
    private val dataFile = File(dir, "costo-data.json")

    override fun loadData(): String? = dataFile.takeIf { it.exists() }?.readText()

    override fun saveData(json: String) = dataFile.writeText(json)

    override fun today(): String = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())

    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun timeLabel(): String = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())

    override suspend fun httpRequest(method: String, url: String, body: String?, headers: Map<String, String>): Pair<Int, String> =
        withContext(Dispatchers.IO) {
            val c = URI(url).toURL().openConnection() as HttpURLConnection
            try {
                c.connectTimeout = 10_000
                c.readTimeout = 20_000
                c.requestMethod = method
                headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
                if (body != null) {
                    c.doOutput = true
                    c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                }
                val code = c.responseCode
                val stream = if (code in 200..299) c.inputStream else c.errorStream
                code to (stream?.use { it.readBytes().toString(Charsets.UTF_8) } ?: "")
            } finally {
                c.disconnect()
            }
        }

    override suspend fun httpGet(url: String): String = withContext(Dispatchers.IO) {
        val c = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 10_000
            c.readTimeout = 15_000
            val code = c.responseCode
            if (code !in 200..299) throw IOException("le serveur a répondu $code")
            c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } finally {
            c.disconnect()
        }
    }

    override fun exportBackup(fileName: String, json: String, onDone: (Boolean) -> Unit) {
        val ch = JFileChooser().apply { selectedFile = File(fileName) }
        if (ch.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            onDone(runCatching { ch.selectedFile.writeText(json) }.isSuccess)
        } else onDone(false)
    }

    override fun importBackup(onResult: (String?) -> Unit) {
        val ch = JFileChooser().apply { fileFilter = FileNameExtensionFilter("Sauvegarde Costo (.json)", "json") }
        onResult(if (ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) runCatching { ch.selectedFile.readText() }.getOrNull() else null)
    }
}

fun main() = application {
    val nav = remember { Nav() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Costo",
        state = rememberWindowState(width = 420.dp, height = 860.dp),
    ) {
        App(DesktopPlatform, nav)
    }
}
