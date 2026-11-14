# Serveur de synchronisation Costo

Petite API sur Vercel (projet `costo-api`) qui garde la dernière version des données Costo
dans la base Neon `menu-api-db` (table `costo_state`). Le téléphone et le PC envoient leurs
modifications et récupèrent la version la plus récente.

Variables Vercel : `DATABASE_URL` (Neon) et `COSTO_KEY` (la même clé que dans
`composeApp/src/commonMain/kotlin/tn/costo/SyncConfig.kt`).

Table :

    CREATE TABLE IF NOT EXISTS costo_state (
      id text PRIMARY KEY, updated_at bigint NOT NULL,
      data jsonb NOT NULL, saved_at timestamptz NOT NULL DEFAULT now());
