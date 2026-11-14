// API de synchronisation Costo.
// GET  /api/costo  -> { updatedAt, data }   (dernière version enregistrée)
// PUT  /api/costo  <- { updatedAt, data }   (enregistrée seulement si plus récente)
// En-tête obligatoire : x-costo-key (variable COSTO_KEY). Base : DATABASE_URL (Neon).
const { neon } = require('@neondatabase/serverless');

const sql = neon(process.env.DATABASE_URL);

module.exports = async (req, res) => {
  res.setHeader('Cache-Control', 'no-store');
  if (!process.env.COSTO_KEY || req.headers['x-costo-key'] !== process.env.COSTO_KEY) {
    return res.status(401).json({ error: 'clé invalide' });
  }
  try {
    if (req.method === 'GET') {
      const rows = await sql`SELECT updated_at, data FROM costo_state WHERE id = 'main'`;
      if (!rows.length) return res.status(200).json({ updatedAt: 0, data: null });
      return res.status(200).json({ updatedAt: Number(rows[0].updated_at), data: rows[0].data });
    }
    if (req.method === 'PUT') {
      const body = typeof req.body === 'string' ? JSON.parse(req.body) : req.body;
      const updatedAt = Number(body && body.updatedAt);
      if (!updatedAt || !body.data || typeof body.data !== 'object') {
        return res.status(400).json({ error: 'données invalides' });
      }
      const rows = await sql`
        INSERT INTO costo_state (id, updated_at, data)
        VALUES ('main', ${updatedAt}, ${JSON.stringify(body.data)}::jsonb)
        ON CONFLICT (id) DO UPDATE
          SET updated_at = EXCLUDED.updated_at, data = EXCLUDED.data, saved_at = now()
          WHERE costo_state.updated_at < EXCLUDED.updated_at
        RETURNING updated_at`;
      if (!rows.length) {
        const cur = await sql`SELECT updated_at FROM costo_state WHERE id = 'main'`;
        return res.status(409).json({ updatedAt: Number(cur[0].updated_at) });
      }
      return res.status(200).json({ updatedAt });
    }
    res.setHeader('Allow', 'GET, PUT');
    return res.status(405).json({ error: 'méthode non prise en charge' });
  } catch (e) {
    return res.status(500).json({ error: 'erreur serveur' });
  }
};
