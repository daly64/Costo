package tn.costo

/** Ce que chaque plateforme (Android, PC) fournit à l'app commune. */
interface Platform {
    val versionName: String

    /** Lit les données enregistrées (JSON), null au premier lancement. */
    fun loadData(): String?

    /** Enregistre les données (JSON) dans le stockage privé de l'app. */
    fun saveData(json: String)

    /** Date du jour, ex. "14/11/2026". */
    fun today(): String

    /** Requête GET, appelée hors du fil principal. */
    suspend fun httpGet(url: String): String

    /** Propose d'enregistrer un fichier de sauvegarde ; onDone(true) si réussi. */
    fun exportBackup(fileName: String, json: String, onDone: (Boolean) -> Unit)

    /** Propose de choisir un fichier de sauvegarde ; onResult(contenu) ou null si annulé. */
    fun importBackup(onResult: (String?) -> Unit)
}
