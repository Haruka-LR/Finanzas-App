/**
 * FINANZAS-APP — BACKEND REST API (MySQL 8.0+)
 * Soporta App Android nativa y Wear OS Smartwatch
 */

const express = require('express');
const cors = require('cors');
const mysql = require('mysql2/promise');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Pool de conexión a MySQL
const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'finanzas_db',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Middleware de autenticación simple para dev
const authenticate = (req, res, next) => {
    // Para simplificar integración inicial, asignamos usuario demo (ID 1) si no viene header
    req.userId = 1;
    next();
};

// ─── AUTH ENDPOINTS ─────────────────────────────────────────────
app.post('/api/auth/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const [rows] = await pool.query('SELECT id, email, name, currency FROM users WHERE email = ? LIMIT 1', [email]);
        if (rows.length === 0) {
            return res.status(401).json({ error: 'Usuario no encontrado' });
        }
        res.json({
            token: 'jwt_mock_token_for_app_and_watch',
            user: rows[0]
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post('/api/auth/register', async (req, res) => {
    const { email, password, name } = req.body;
    try {
        const [result] = await pool.query(
            'INSERT INTO users (email, password_hash, name) VALUES (?, ?, ?)',
            [email, 'hash_' + password, name || 'Usuario']
        );
        res.status(201).json({ id: result.insertId, email, name });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ─── TRANSACTIONS ENDPOINTS ─────────────────────────────────────
app.get('/api/transactions', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query(
            `SELECT t.*, c.name as category_name, c.icon as category_icon, c.color as category_color 
             FROM transactions t 
             LEFT JOIN categories c ON t.category_id = c.id 
             WHERE t.user_id = ? 
             ORDER BY t.date DESC, t.id DESC`,
            [req.userId]
        );
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post('/api/transactions', authenticate, async (req, res) => {
    const { account_id, category_id, type, amount, description, date, device_source } = req.body;
    try {
        const [result] = await pool.query(
            `INSERT INTO transactions (user_id, account_id, category_id, type, amount, description, date, device_source)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
            [req.userId, account_id || 1, category_id || 1, type || 'EXPENSE', amount, description, date || new Date().toISOString().split('T')[0], device_source || 'PHONE']
        );
        res.status(201).json({ id: result.insertId, message: 'Transacción registrada' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.delete('/api/transactions/:id', authenticate, async (req, res) => {
    try {
        await pool.query('DELETE FROM transactions WHERE id = ? AND user_id = ?', [req.params.id, req.userId]);
        res.json({ message: 'Transacción eliminada exitosamente' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ─── CATEGORIES & BUDGETS ───────────────────────────────────────
app.get('/api/categories', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM categories WHERE user_id = ? OR is_default = TRUE', [req.userId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.get('/api/budgets', authenticate, async (req, res) => {
    try {
        const [rows] = await pool.query(
            `SELECT b.*, c.name as category_name,
                    COALESCE(SUM(t.amount), 0) as spent
             FROM budgets b
             LEFT JOIN categories c ON b.category_id = c.id
             LEFT JOIN transactions t ON t.category_id = b.category_id 
                  AND t.user_id = b.user_id 
                  AND t.type = 'EXPENSE'
                  AND MONTH(t.date) = MONTH(CURRENT_DATE())
             WHERE b.user_id = ?
             GROUP BY b.id`,
            [req.userId]
        );
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ─── WEARABLE (WEAR OS) SPECIALIZED ENDPOINTS ───────────────────
app.get('/api/wearable/summary', authenticate, async (req, res) => {
    try {
        // Balance total consolidado
        const [balanceRows] = await pool.query('SELECT COALESCE(SUM(balance), 0) as total FROM accounts WHERE user_id = ?', [req.userId]);
        
        // Gasto del día
        const [todayRows] = await pool.query(
            `SELECT COALESCE(SUM(amount), 0) as todaySpent 
             FROM transactions 
             WHERE user_id = ? AND type = 'EXPENSE' AND date = CURRENT_DATE()`,
            [req.userId]
        );

        // Gasto de la semana
        const [weekRows] = await pool.query(
            `SELECT COALESCE(SUM(amount), 0) as weekSpent 
             FROM transactions 
             WHERE user_id = ? AND type = 'EXPENSE' AND date >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY)`,
            [req.userId]
        );

        // 3 transacciones recientes para la pantalla circular pequeña
        const [recent] = await pool.query(
            `SELECT t.description, t.amount, c.name as category 
             FROM transactions t 
             LEFT JOIN categories c ON t.category_id = c.id 
             WHERE t.user_id = ? 
             ORDER BY t.date DESC, t.id DESC LIMIT 3`,
            [req.userId]
        );

        res.json({
            totalBalance: parseFloat(balanceRows[0].total),
            todaySpent: parseFloat(todayRows[0].todaySpent),
            weekSpent: parseFloat(weekRows[0].weekSpent),
            recent
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Registro express de gasto en 1 toque desde el reloj
app.post('/api/wearable/quick-expense', authenticate, async (req, res) => {
    const { amount, category, description } = req.body;
    try {
        const [catRows] = await pool.query('SELECT id FROM categories WHERE name LIKE ? LIMIT 1', [`%${category || 'Comida'}%`]);
        const categoryId = catRows.length > 0 ? catRows[0].id : 1;

        const [result] = await pool.query(
            `INSERT INTO transactions (user_id, account_id, category_id, type, amount, description, date, device_source)
             VALUES (?, 1, ?, 'EXPENSE', ?, ?, CURRENT_DATE(), 'WEARABLE_WATCH')`,
            [req.userId, categoryId, amount, description || category || 'Gasto Rápido Reloj']
        );

        res.status(201).json({
            success: true,
            id: result.insertId,
            message: 'Gasto registrado desde Wear OS'
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.listen(PORT, () => {
    console.log(`[Finanzas Backend] Escuchando en http://localhost:${PORT}`);
    console.log(`[Wearable API] Endpoints listos en http://localhost:${PORT}/api/wearable/*`);
});
