-- ====================================================================
-- FINANZAS-APP — ESQUEMA COMPLETO DE BASE DE DATOS MYSQL 8.0+
-- Compatible con Android App, Wear OS Smartwatch y API REST Backend
-- ====================================================================

CREATE DATABASE IF NOT EXISTS finanzas_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE finanzas_db;

-- Desactivar llaves foráneas temporalmente para recarga limpia
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS recurring_transactions;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS financial_goals;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ────────────────────────────────────────────────────────────────────
-- 1. TABLA: users (Usuarios registrados)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    name            VARCHAR(120)    NOT NULL,
    avatar_url      VARCHAR(500)    NULL,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'MXN',
    locale          VARCHAR(10)     NOT NULL DEFAULT 'es-MX',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 2. TABLA: categories (Categorías de ingresos y gastos)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE categories (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NULL, -- NULL indica categoría global/por defecto
    name            VARCHAR(100)    NOT NULL,
    type            ENUM('INCOME', 'EXPENSE') NOT NULL DEFAULT 'EXPENSE',
    icon            VARCHAR(50)     NOT NULL DEFAULT 'category',
    color           VARCHAR(7)      NOT NULL DEFAULT '#10B981',
    is_default      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 3. TABLA: accounts (Cuentas financieras y carteras)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE accounts (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    type            ENUM('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'CASH', 'INVESTMENT', 'DIGITAL_WALLET') NOT NULL DEFAULT 'CHECKING',
    balance         DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'MXN',
    color           VARCHAR(7)      NOT NULL DEFAULT '#10B981',
    icon            VARCHAR(50)     NOT NULL DEFAULT 'account_balance',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_accounts_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 4. TABLA: transactions (Movimientos registrados desde app o reloj)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE transactions (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    account_id      BIGINT UNSIGNED NOT NULL,
    category_id     BIGINT UNSIGNED NULL,
    type            ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    note            TEXT            NULL,
    date            DATE            NOT NULL,
    device_source   VARCHAR(50)     NOT NULL DEFAULT 'PHONE', -- 'PHONE', 'WEARABLE_WATCH', 'WEB'
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_transactions_user_date (user_id, date),
    INDEX idx_transactions_category (category_id),
    INDEX idx_transactions_account (account_id),
    INDEX idx_transactions_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 5. TABLA: budgets (Presupuestos con alertas de tope)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE budgets (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    category_id     BIGINT UNSIGNED NULL,
    name            VARCHAR(100)    NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    period          ENUM('WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL DEFAULT 'MONTHLY',
    start_date      DATE            NOT NULL,
    end_date        DATE            NULL,
    alert_threshold DECIMAL(5,2)    NOT NULL DEFAULT 80.00,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_budgets_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 6. TABLA: financial_goals (Metas de ahorro)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE financial_goals (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    name            VARCHAR(120)    NOT NULL,
    description     TEXT            NULL,
    target_amount   DECIMAL(15,2)   NOT NULL,
    current_amount  DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    deadline        DATE            NULL,
    icon            VARCHAR(50)     NOT NULL DEFAULT 'savings',
    color           VARCHAR(7)      NOT NULL DEFAULT '#F59E0B',
    is_completed    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_goals_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- 7. TABLA: refresh_tokens (Sesiones seguras para Teléfono y Wearable)
-- ────────────────────────────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    token           VARCHAR(500)    NOT NULL UNIQUE,
    device_name     VARCHAR(100)    NULL,
    is_wearable     BOOLEAN         NOT NULL DEFAULT FALSE,
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────────────
-- SEED DATA: Categorías por defecto del sistema
-- ────────────────────────────────────────────────────────────────────
INSERT INTO categories (name, type, icon, color, is_default) VALUES
('Comida y Alimentos', 'EXPENSE', 'restaurant', '#EF4444', TRUE),
('Transporte y Gasolina', 'EXPENSE', 'directions_car', '#F59E0B', TRUE),
('Servicios del Hogar', 'EXPENSE', 'bolt', '#6366F1', TRUE),
('Entretenimiento', 'EXPENSE', 'sports_esports', '#8B5CF6', TRUE),
('Salud y Farmacia', 'EXPENSE', 'medical_services', '#10B981', TRUE),
('Educación', 'EXPENSE', 'school', '#3B82F6', TRUE),
('Ropa y Calzado', 'EXPENSE', 'checkroom', '#EC4899', TRUE),
('Otros Gastos', 'EXPENSE', 'category', '#6B7280', TRUE),
('Salario Mensual', 'INCOME', 'payments', '#10B981', TRUE),
('Freelance y Proyectos', 'INCOME', 'laptop_chromebook', '#3B82F6', TRUE),
('Inversiones y Rendimientos', 'INCOME', 'trending_up', '#F59E0B', TRUE);

-- Usuario Demo
INSERT INTO users (email, password_hash, name, currency) VALUES
('demo@finanzas.app', '$2a$10$w4r0b.demoHashForDevelopmentTesting1234567890abcdef', 'Irvin (Demo)', 'MXN');

-- Cuenta demo
INSERT INTO accounts (user_id, name, type, balance, currency, color, icon) VALUES
(1, 'Cuenta Principal Débito', 'CHECKING', 14850.00, 'MXN', '#10B981', 'account_balance'),
(1, 'Efectivo Cartera', 'CASH', 1200.00, 'MXN', '#F59E0B', 'payments');

-- Transacciones demo
INSERT INTO transactions (user_id, account_id, category_id, type, amount, description, date, device_source) VALUES
(1, 1, 1, 'EXPENSE', 1250.00, 'Supermercado Semanal', '2026-11-06', 'PHONE'),
(1, 1, 2, 'EXPENSE', 450.00, 'Gasolina Shell', '2026-11-05', 'PHONE'),
(1, 1, 1, 'EXPENSE', 320.00, 'Cena Restaurante', '2026-11-04', 'WEARABLE_WATCH'),
(1, 1, 3, 'EXPENSE', 219.00, 'Suscripción Netflix', '2026-11-01', 'PHONE'),
(1, 1, 9, 'INCOME', 18000.00, 'Nómina Quincenal', '2026-11-01', 'PHONE');

-- Presupuesto demo
INSERT INTO budgets (user_id, category_id, name, amount, period, start_date) VALUES
(1, 1, 'Presupuesto Alimentos', 4500.00, 'MONTHLY', '2026-11-01'),
(1, 2, 'Presupuesto Movilidad', 1800.00, 'MONTHLY', '2026-11-01');
