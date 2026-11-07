# 🏗️ COMPOSITOR — Finanzas-App: Todo lo que falta por construir

> **Propósito**: Este documento es una guía completa para que otra IA (o desarrollador) construya todo lo que falta en el proyecto Finanzas-App. Incluye especificaciones técnicas, diseño visual, arquitectura, base de datos MySQL, soporte para wearable (Wear OS), y una landing page inspirada en [ryoku.dev](https://ryoku.dev/).

> [!CAUTION]
> **BUGS CRÍTICOS QUE DEBEN CORREGIRSE PRIMERO** — El proyecto actual tiene errores de compilación que deben resolverse antes de construir cualquier feature nueva:
> 1. **Archivos `ui/auth/` sin declaración de package** — `AuthUiState.kt`, `AuthViewModel.kt`, y `LoginScreen.kt` no tienen `package com.example.finanzasapp.ui.auth`.
> 2. **Falta la dependencia `auth-kt` / `gotrue-kt` de Supabase** — `AuthViewModel` importa `io.github.jan.supabase.gotrue` pero la biblioteca NO está en `libs.versions.toml` ni en `app/build.gradle.kts`.
> 3. **`LoginScreen` no está conectada a `NavGraph.kt` ni a `MainActivity.kt`** — No hay flujo de autenticación integrado.
> 4. **`MainActivity` le falta `@AndroidEntryPoint`** — Sin esto, la inyección de Hilt en Compose falla.
> 5. **Falta `<uses-permission android:name="android.permission.INTERNET" />`** en `AndroidManifest.xml`.
> 6. **Conflicto de DI en Hilt** — `HomeViewModel` pide `ExpenseRepository` y `CategoryRepository` vía `@Inject constructor`, pero `RepositoryModule.kt` no los provee. Solo provee `FinanzasRepository`.
> 7. **`HomeFragment.kt` es código muerto** — Es un Fragment XML que infla `R.layout.fragment_home` que **no existe**. `MainActivity` usa Compose. Debe eliminarse y crear un `HomeScreen` Composable.
> 8. **`ExpenseRepository` usa datos hardcodeados in-memory** — Emite un `Flow<List<Expense>>` con 4 items estáticos, no conecta con Supabase ni ninguna DB.
> 9. **Credenciales de Supabase vacías** — `gradle.properties` no tiene `supabaseUrl` ni `supabaseAnonKey` configurados.

---

## 📋 ÍNDICE

1. [Estado Actual del Proyecto](#1-estado-actual-del-proyecto)
2. [Arquitectura Objetivo](#2-arquitectura-objetivo)
3. [Migración de Base de Datos a MySQL](#3-migración-de-base-de-datos-a-mysql)
4. [Backend API (Spring Boot / Ktor)](#4-backend-api)
5. [Funcionalidades Faltantes — App Android](#5-funcionalidades-faltantes--app-android)
6. [Módulo Wear OS (Wearable)](#6-módulo-wear-os-wearable)
7. [Landing Page — Estilo ryoku.dev](#7-landing-page--estilo-ryokudev)
8. [Sistema de Diseño y Temas](#8-sistema-de-diseño-y-temas)
9. [Checklist de Implementación](#9-checklist-de-implementación)

---

## 1. ESTADO ACTUAL DEL PROYECTO

### Ubicación
```
c:\Users\irvin\Documents\Finanzas-App
```

### Tech Stack Actual
| Componente | Tecnología |
|---|---|
| Plataforma | Android nativo (Kotlin) |
| UI | Jetpack Compose (BOM 2025.08.01) + Material 3 |
| Arquitectura | MVVM + Hilt 2.60.1 (DI via KSP 2.3.12) |
| Backend actual | Supabase Kotlin Client BOM 3.2.1 (`postgrest-kt`) — **credenciales vacías, NO funciona** |
| Auth | Supabase Auth (código existe pero **roto**: falta dependencia `auth-kt`) |
| HTTP Client | Ktor Client Android 3.1.3 |
| SDK Android | Min 24, Target/Compile 37 |
| Build System | Gradle 9.6, AGP 9.4.0, Kotlin 2.2.20, Version Catalog (`libs.versions.toml`) |
| Legacy (sin usar) | AppCompat 1.6.1, ConstraintLayout 2.1.4, RecyclerView 1.3.0, ViewBinding, Navigation Fragment XML |

### Lo que YA está implementado ✅ (con matices)

| Feature | Estado | Archivos clave | Notas |
|---|---|---|---|
| Sistema de diseño (tema) | ✅ Completo | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `Dimens.kt` | Paleta financiera con colores semánticos (Income, Expense, Warning), light/dark, shapes |
| Scaffold + Bottom Nav | ✅ Funcional | `ui/navigation/BottomNavBar.kt`, `MainScreen.kt` | 4 tabs: Home, Estadísticas, Categorías, Presupuestos. Backstack correcto |
| Rutas de navegación | ⚠️ Stubs | `ui/navigation/NavGraph.kt`, `Screen.kt` | 6 rutas definidas pero **TODAS renderizan texto placeholder** |
| Lógica de HomeViewModel | ⚠️ Solo lógica | `ui/home/HomeViewModel.kt`, `HomeUiState.kt` | Filtrado por periodo, agrupación por categoría, top 5 recientes — pero **sin UI Compose** |
| Auth (Login/Signup) | ❌ Roto | `ui/auth/LoginScreen.kt`, `AuthViewModel.kt`, `AuthUiState.kt` | Código existe pero no compila (falta package, dependencia, no está en NavGraph) |
| ExpenseRepository | ⚠️ Mock | `data/repository/ExpenseRepository.kt` | Retorna `Flow` con 4 gastos hardcodeados. No conecta con ninguna DB |
| CategoryRepository | ⚠️ Solo cálculo | `data/repository/CategoryRepository.kt` | Solo agrupa/suma categorías in-memory. Sin Hilt binding |
| FinanzasRepository | ❌ Vacío | `data/FinanzasRepository.kt` | Clase con `SupabaseClient` pero **cero métodos** |
| Hilt DI | ⚠️ Incompleto | `di/SupabaseModule.kt`, `di/RepositoryModule.kt` | Solo provee `FinanzasRepository` (vacío). No provee `ExpenseRepository` ni `CategoryRepository` |
| Application class | ✅ Completo | `FinanzasApplication.kt` | `@HiltAndroidApp` correcto |

### Categorías hardcodeadas actuales
```kotlin
val categories = listOf("Comida", "Transporte", "Entretenimiento", "Salud", "Educación", "Servicios", "Ropa", "Otros")
```

### Modelo de datos actual (`Expense`)
```kotlin
data class Expense(
    val id: Int,
    val userId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val date: String,
    val createdAt: String
)
```

### Lo que está como STUB/Placeholder ⚠️

**Rutas Compose activas (en `NavGraph.kt`) — todas muestran solo texto placeholder:**

| Ruta | Nombre en Bottom Nav | Estado |
|---|---|---|
| `home` | Inicio | Renderiza `DestinationScreen("Inicio")` — texto centrado |
| `add_expense` | (sin tab, via navegación) | Renderiza `DestinationScreen("Agregar Gasto")` |
| `edit_expense/{expenseId}` | (sin tab, via navegación) | Renderiza `DestinationScreen("Editar Gasto")` |
| `statistics` | Estadísticas | Renderiza `DestinationScreen("Estadísticas")` |
| `categories` | Categorías | Renderiza `DestinationScreen("Categorías")` |
| `budgets` | Presupuestos | Renderiza `DestinationScreen("Presupuestos")` |

**Código legacy MUERTO (Fragments XML del template de Android Studio — ELIMINAR):**

| Archivo | Por qué eliminarlo |
|---|---|
| `ui/reflow/ReflowFragment.kt` + `ReflowViewModel.kt` | Template boilerplate, no usado por Compose |
| `ui/slideshow/SlideshowFragment.kt` + `SlideshowViewModel.kt` | Template boilerplate, no usado por Compose |
| `ui/settings/SettingsFragment.kt` + `SettingsViewModel.kt` | Template boilerplate, no usado por Compose |
| `ui/transform/TransformFragment.kt` + `TransformViewModel.kt` | Template boilerplate con RecyclerView de 16 avatars, no usado |
| `ui/home/HomeFragment.kt` + `HomeViewModelFactory.kt` | Fragment que infla `R.layout.fragment_home` que **no existe**. Código muerto |
| `res/layout/*.xml` (todos) | Layouts XML del template, no usados por Compose |
| `res/navigation/mobile_navigation.xml` | Navegación XML del template, no usada |
| `res/menu/*.xml` (todos) | Menús XML del template, no usados |

### Lo que NO existe en absoluto ❌

- ❌ Base de datos MySQL (usa Supabase/PostgreSQL)
- ❌ Backend propio (API REST)
- ❌ Módulo Wear OS (wearable)
- ❌ Landing page / sitio web
- ❌ Tracking de ingresos (solo gastos)
- ❌ Gestión de presupuestos
- ❌ Cuentas financieras múltiples
- ❌ Gráficas y charts
- ❌ Reportes y analítica
- ❌ Configuración de perfil
- ❌ Transacciones recurrentes
- ❌ Metas financieras
- ❌ Notificaciones / recordatorios
- ❌ Exportación de datos (CSV/PDF)
- ❌ Almacenamiento offline (Room)
- ❌ Búsqueda de transacciones
- ❌ Multi-moneda
- ❌ Onboarding para nuevos usuarios

---

## 2. ARQUITECTURA OBJETIVO

### Diagrama de arquitectura completa

```
┌─────────────────────────────────────────────────────────────┐
│                    LANDING PAGE (WEB)                        │
│              Next.js / HTML+CSS+JS estático                  │
│              Estilo ryoku.dev (dark, glassmorphism)           │
│              Deploy: Vercel / Netlify / GitHub Pages          │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐
│   APP ANDROID (📱)    │     │   WEAR OS APP (⌚)    │
│   Jetpack Compose     │     │   Compose for Wear   │
│   Material 3          │◄───►│   Tiles API          │
│   MVVM + Hilt         │     │   Complications      │
│   Room (offline)      │     │   Data Layer API     │
│   Retrofit (API)      │     │   Horologist          │
└──────────┬───────────┘     └──────────┬───────────┘
           │                            │
           │         REST API           │
           ▼                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND API (🖥️)                          │
│              Ktor / Spring Boot (Kotlin)                     │
│              Endpoints REST + JWT Auth                       │
│              Exposed / Hibernate ORM                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL DATABASE (🗄️)                       │
│              Usuarios, Transacciones, Categorías,            │
│              Presupuestos, Cuentas, Metas                    │
└─────────────────────────────────────────────────────────────┘
```

### Estructura de carpetas objetivo

```
Finanzas-App/
├── app/                          # ← App Android existente (modificar)
│   ├── src/main/
│   │   ├── java/com/example/finanzasapp/
│   │   │   ├── data/
│   │   │   │   ├── local/        # ← NUEVO: Room DB para offline
│   │   │   │   │   ├── FinanzasDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── TransactionDao.kt
│   │   │   │   │   │   ├── AccountDao.kt
│   │   │   │   │   │   ├── BudgetDao.kt
│   │   │   │   │   │   └── CategoryDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── TransactionEntity.kt
│   │   │   │   │       ├── AccountEntity.kt
│   │   │   │   │       ├── BudgetEntity.kt
│   │   │   │   │       └── CategoryEntity.kt
│   │   │   │   ├── remote/       # ← NUEVO: API calls via Retrofit
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── AuthApi.kt
│   │   │   │   │   │   ├── TransactionApi.kt
│   │   │   │   │   │   ├── AccountApi.kt
│   │   │   │   │   │   ├── BudgetApi.kt
│   │   │   │   │   │   └── CategoryApi.kt
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── LoginRequest.kt
│   │   │   │   │   │   ├── RegisterRequest.kt
│   │   │   │   │   │   ├── AuthResponse.kt
│   │   │   │   │   │   └── ... (DTOs)
│   │   │   │   │   └── interceptor/
│   │   │   │   │       └── AuthInterceptor.kt
│   │   │   │   ├── repository/   # ← MODIFICAR: repos con offline-first
│   │   │   │   │   ├── TransactionRepository.kt
│   │   │   │   │   ├── AccountRepository.kt
│   │   │   │   │   ├── BudgetRepository.kt
│   │   │   │   │   ├── CategoryRepository.kt
│   │   │   │   │   └── AuthRepository.kt
│   │   │   │   └── model/        # ← EXPANDIR modelos
│   │   │   │       ├── Transaction.kt
│   │   │   │       ├── Account.kt
│   │   │   │       ├── Budget.kt
│   │   │   │       ├── Category.kt
│   │   │   │       ├── FinancialGoal.kt
│   │   │   │       └── User.kt
│   │   │   ├── di/               # ← EXPANDIR módulos Hilt
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   └── RepositoryModule.kt
│   │   │   └── ui/
│   │   │       ├── auth/         # ← MODIFICAR: mejorar login
│   │   │       ├── home/         # ← MODIFICAR: dashboard completo
│   │   │       ├── transactions/ # ← NUEVO: gestión de transacciones
│   │   │       ├── accounts/     # ← NUEVO: cuentas financieras
│   │   │       ├── budgets/      # ← NUEVO: presupuestos
│   │   │       ├── categories/   # ← NUEVO: gestión de categorías
│   │   │       ├── goals/        # ← NUEVO: metas financieras
│   │   │       ├── reports/      # ← NUEVO: reportes y gráficas
│   │   │       ├── settings/     # ← MODIFICAR: configuración real
│   │   │       ├── onboarding/   # ← NUEVO: flujo de bienvenida
│   │   │       ├── navigation/   # ← MODIFICAR: actualizar rutas
│   │   │       ├── theme/        # ← MODIFICAR: tema estilo ryoku
│   │   │       └── components/   # ← NUEVO: componentes reutilizables
│   │   └── res/
│   └── build.gradle.kts
│
├── wear/                         # ← NUEVO: módulo Wear OS
│   ├── src/main/
│   │   ├── java/com/example/finanzasapp/
│   │   │   ├── WearMainActivity.kt
│   │   │   ├── tile/
│   │   │   │   └── FinanzasTileService.kt
│   │   │   ├── complication/
│   │   │   │   └── BalanceComplicationService.kt
│   │   │   ├── presentation/
│   │   │   │   ├── WearApp.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── BalanceScreen.kt
│   │   │   │   │   ├── QuickExpenseScreen.kt
│   │   │   │   │   ├── RecentTransactionsScreen.kt
│   │   │   │   │   └── BudgetOverviewScreen.kt
│   │   │   │   └── theme/
│   │   │   │       └── WearTheme.kt
│   │   │   └── data/
│   │   │       └── WearDataSyncService.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── backend/                      # ← NUEVO: servidor backend
│   ├── src/main/kotlin/com/example/finanzasapi/
│   │   ├── Application.kt
│   │   ├── config/
│   │   │   ├── DatabaseConfig.kt
│   │   │   ├── SecurityConfig.kt
│   │   │   └── CorsConfig.kt
│   │   ├── routes/
│   │   │   ├── AuthRoutes.kt
│   │   │   ├── TransactionRoutes.kt
│   │   │   ├── AccountRoutes.kt
│   │   │   ├── BudgetRoutes.kt
│   │   │   ├── CategoryRoutes.kt
│   │   │   ├── GoalRoutes.kt
│   │   │   └── WearableRoutes.kt   # Endpoints simplificados para el reloj
│   │   ├── models/
│   │   │   ├── User.kt
│   │   │   ├── Transaction.kt
│   │   │   ├── Account.kt
│   │   │   ├── Budget.kt
│   │   │   ├── Category.kt
│   │   │   └── FinancialGoal.kt
│   │   ├── services/
│   │   │   ├── AuthService.kt
│   │   │   ├── TransactionService.kt
│   │   │   └── ...
│   │   └── utils/
│   │       ├── JwtUtil.kt
│   │       └── PasswordUtil.kt
│   ├── build.gradle.kts
│   └── application.conf
│
├── landing/                      # ← NUEVO: landing page web
│   ├── index.html
│   ├── css/
│   │   └── styles.css
│   ├── js/
│   │   └── main.js
│   └── assets/
│       ├── images/
│       └── fonts/
│
├── build.gradle.kts              # (raíz, ya existe)
├── settings.gradle.kts           # (modificar para incluir :wear y :backend)
└── README.md
```

---

## 3. MIGRACIÓN DE BASE DE DATOS A MySQL

### 3.1 Esquema completo MySQL

```sql
-- ============================================
-- FINANZAS-APP: Esquema MySQL completo
-- ============================================

CREATE DATABASE IF NOT EXISTS finanzas_app
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE finanzas_app;

-- ─── USUARIOS ───────────────────────────────
CREATE TABLE users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    avatar_url      VARCHAR(500)    NULL,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'MXN',
    locale          VARCHAR(10)     NOT NULL DEFAULT 'es-MX',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email)
) ENGINE=InnoDB;

-- ─── CATEGORÍAS ─────────────────────────────
CREATE TABLE categories (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    type            ENUM('INCOME', 'EXPENSE') NOT NULL DEFAULT 'EXPENSE',
    icon            VARCHAR(50)     NOT NULL DEFAULT 'category',
    color           VARCHAR(7)      NOT NULL DEFAULT '#6366F1',  -- hex color
    is_default      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_categories_user (user_id),
    UNIQUE KEY uk_category_name_user (user_id, name)
) ENGINE=InnoDB;

-- ─── CUENTAS FINANCIERAS ────────────────────
CREATE TABLE accounts (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    type            ENUM('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'CASH', 'INVESTMENT', 'DIGITAL_WALLET') NOT NULL,
    balance         DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'MXN',
    color           VARCHAR(7)      NOT NULL DEFAULT '#10B981',
    icon            VARCHAR(50)     NOT NULL DEFAULT 'account_balance',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_accounts_user (user_id)
) ENGINE=InnoDB;

-- ─── TRANSACCIONES ──────────────────────────
CREATE TABLE transactions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    account_id      BIGINT          NOT NULL,
    category_id     BIGINT          NULL,
    type            ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    note            TEXT            NULL,
    date            DATE            NOT NULL,
    is_recurring    BOOLEAN         NOT NULL DEFAULT FALSE,
    recurring_id    BIGINT          NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (recurring_id) REFERENCES recurring_transactions(id) ON DELETE SET NULL,
    INDEX idx_transactions_user (user_id),
    INDEX idx_transactions_date (user_id, date),
    INDEX idx_transactions_category (user_id, category_id),
    INDEX idx_transactions_account (user_id, account_id),
    INDEX idx_transactions_type (user_id, type)
) ENGINE=InnoDB;

-- ─── TRANSACCIONES RECURRENTES ──────────────
CREATE TABLE recurring_transactions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    account_id      BIGINT          NOT NULL,
    category_id     BIGINT          NULL,
    type            ENUM('INCOME', 'EXPENSE') NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    frequency       ENUM('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NULL,
    next_date       DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_recurring_user (user_id),
    INDEX idx_recurring_next (next_date, is_active)
) ENGINE=InnoDB;

-- ─── PRESUPUESTOS ───────────────────────────
CREATE TABLE budgets (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NULL,
    name            VARCHAR(100)    NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    period          ENUM('WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL DEFAULT 'MONTHLY',
    start_date      DATE            NOT NULL,
    end_date        DATE            NULL,
    alert_threshold DECIMAL(5,2)    NOT NULL DEFAULT 80.00,  -- porcentaje para alerta
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_budgets_user (user_id)
) ENGINE=InnoDB;

-- ─── METAS FINANCIERAS ─────────────────────
CREATE TABLE financial_goals (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
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
) ENGINE=InnoDB;

-- ─── TOKENS DE SESIÓN (para JWT refresh) ────
CREATE TABLE refresh_tokens (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(500)    NOT NULL UNIQUE,
    device_name     VARCHAR(100)    NULL,
    is_wearable     BOOLEAN         NOT NULL DEFAULT FALSE,
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_token (token)
) ENGINE=InnoDB;

-- ─── DATOS DE CATEGORÍAS POR DEFECTO ────────
-- Insertar al registrar un nuevo usuario
-- (ejecutar en el servicio de registro)

/*
Categorías default de GASTO:
  Comida        🍽️  #EF4444
  Transporte    🚗  #F59E0B
  Entretenimiento 🎮 #8B5CF6
  Salud         💊  #10B981
  Educación     📚  #3B82F6
  Servicios     🔌  #6366F1
  Ropa          👕  #EC4899
  Vivienda      🏠  #14B8A6
  Otros         📦  #6B7280

Categorías default de INGRESO:
  Salario       💰  #10B981
  Freelance     💻  #3B82F6
  Inversiones   📈  #F59E0B
  Ventas        🛒  #8B5CF6
  Otros         📦  #6B7280
*/
```

### 3.2 Pasos de migración

1. **Instalar MySQL 8.0+** en el servidor
2. **Ejecutar el script SQL** de arriba
3. **Crear usuario de app**: `CREATE USER 'finanzas_user'@'%' IDENTIFIED BY 'SECURE_PASSWORD';`
4. **Dar permisos**: `GRANT ALL PRIVILEGES ON finanzas_app.* TO 'finanzas_user'@'%';`
5. **Eliminar dependencia de Supabase** del proyecto Android
6. **Implementar backend propio** que conecte a MySQL (ver sección 4)

---

## 4. BACKEND API

### 4.1 Tecnología recomendada

**Opción A (Recomendada): Ktor + Exposed** — Mantiene Kotlin en todo el stack
```kotlin
// build.gradle.kts del backend
dependencies {
    implementation("io.ktor:ktor-server-core:2.3.+")
    implementation("io.ktor:ktor-server-netty:2.3.+")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.+")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.+")
    implementation("io.ktor:ktor-server-auth:2.3.+")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.+")
    implementation("io.ktor:ktor-server-cors:2.3.+")
    implementation("io.ktor:ktor-server-status-pages:2.3.+")
    implementation("org.jetbrains.exposed:exposed-core:0.44.+")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.+")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.+")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.+")
    implementation("mysql:mysql-connector-java:8.0.+")
    implementation("com.zaxxer:HikariCP:5.+")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.auth0:java-jwt:4.4.+")
}
```

### 4.2 Endpoints de la API REST

#### Auth
```
POST   /api/auth/register          → Registro de usuario
POST   /api/auth/login             → Login (retorna JWT access + refresh token)
POST   /api/auth/refresh           → Renovar access token
POST   /api/auth/logout            → Invalidar refresh token
GET    /api/auth/me                → Perfil del usuario actual
PUT    /api/auth/me                → Actualizar perfil (nombre, avatar, moneda)
PUT    /api/auth/password          → Cambiar contraseña
```

#### Transacciones
```
GET    /api/transactions           → Listar transacciones (con filtros: type, categoryId, accountId, startDate, endDate, search)
GET    /api/transactions/:id       → Detalle de transacción
POST   /api/transactions           → Crear transacción
PUT    /api/transactions/:id       → Actualizar transacción
DELETE /api/transactions/:id       → Eliminar transacción
GET    /api/transactions/summary   → Resumen: ingresos, gastos, balance (por rango de fechas)
GET    /api/transactions/by-category → Totales agrupados por categoría
GET    /api/transactions/trends    → Tendencias mes a mes (últimos 6-12 meses)
```

#### Cuentas
```
GET    /api/accounts               → Listar cuentas
POST   /api/accounts               → Crear cuenta
PUT    /api/accounts/:id           → Actualizar cuenta
DELETE /api/accounts/:id           → Eliminar cuenta
GET    /api/accounts/:id/balance   → Balance calculado de la cuenta
```

#### Presupuestos
```
GET    /api/budgets                → Listar presupuestos (con gasto actual calculado)
POST   /api/budgets                → Crear presupuesto
PUT    /api/budgets/:id            → Actualizar presupuesto
DELETE /api/budgets/:id            → Eliminar presupuesto
```

#### Categorías
```
GET    /api/categories             → Listar categorías del usuario
POST   /api/categories             → Crear categoría personalizada
PUT    /api/categories/:id         → Actualizar categoría
DELETE /api/categories/:id         → Eliminar categoría
```

#### Metas Financieras
```
GET    /api/goals                  → Listar metas
POST   /api/goals                  → Crear meta
PUT    /api/goals/:id              → Actualizar meta
DELETE /api/goals/:id              → Eliminar meta
PUT    /api/goals/:id/contribute   → Agregar dinero a una meta
```

#### Transacciones Recurrentes
```
GET    /api/recurring              → Listar transacciones recurrentes
POST   /api/recurring              → Crear transacción recurrente
PUT    /api/recurring/:id          → Actualizar
DELETE /api/recurring/:id          → Eliminar/desactivar
```

#### Wearable (Endpoints simplificados para Wear OS)
```
GET    /api/wearable/summary       → Balance total, gasto del día, gasto de la semana
GET    /api/wearable/recent        → Últimas 5 transacciones (solo descripción, monto, categoría)
POST   /api/wearable/quick-expense → Agregar gasto rápido (solo monto + categoría)
GET    /api/wearable/budgets       → Presupuestos con % de uso (datos mínimos)
```

### 4.3 Autenticación JWT

```
Access Token:  expira en 15 minutos
Refresh Token: expira en 30 días
Header:        Authorization: Bearer <access_token>
```

**Flujo:**
1. Login → recibe `{ accessToken, refreshToken, user }`
2. Todas las requests llevan `Authorization: Bearer <accessToken>`
3. Cuando expira → `POST /api/auth/refresh` con `{ refreshToken }`
4. Dispositivos wearable tienen su propio refresh token marcado con `is_wearable = true`

---

## 5. FUNCIONALIDADES FALTANTES — APP ANDROID

### 5.1 Pantalla Home → Dashboard completo

**Convertir la pantalla Home actual en un dashboard rico:**

```
┌──────────────────────────────────────┐
│  Hola, Irvin 👋                       │
│  Tu balance general                  │
│  ┌────────────────────────────────┐  │
│  │    $12,450.00 MXN              │  │
│  │    ↑ +$2,300 este mes          │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌──────────┐  ┌──────────┐         │
│  │ Ingresos │  │ Gastos   │         │
│  │ $15,000  │  │ $8,230   │         │
│  │ ↑ 12%    │  │ ↓ 5%     │         │
│  └──────────┘  └──────────┘         │
│                                      │
│  📊 Gastos por categoría            │
│  ┌────────────────────────────────┐  │
│  │  [Gráfica de donut/pie]       │  │
│  │  Comida 35% | Transporte 20%  │  │
│  │  Servicios 15% | ...          │  │
│  └────────────────────────────────┘  │
│                                      │
│  📈 Tendencia mensual               │
│  ┌────────────────────────────────┐  │
│  │  [Gráfica de líneas]          │  │
│  │  últimos 6 meses              │  │
│  └────────────────────────────────┘  │
│                                      │
│  🔔 Alertas                         │
│  ┌────────────────────────────────┐  │
│  │ ⚠️ Comida al 85% del budget   │  │
│  │ 📅 Pago de Netflix mañana     │  │
│  └────────────────────────────────┘  │
│                                      │
│  Últimas transacciones →             │
│  ┌────────────────────────────────┐  │
│  │ 🍽️ Comida     -$150   Hoy     │  │
│  │ 💰 Salario   +$15,000 Nov 1   │  │
│  │ 🚗 Uber      -$89    Nov 5    │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

**Dependencias para gráficas:**
```kotlin
// Charts - usar Vico (recomendado para Compose)
implementation("com.patrykandpatrick.vico:compose-m3:1.13.+")
```

### 5.2 Pantalla de Transacciones (NUEVO)

Reemplazar el "Reflow" stub por una pantalla de transacciones completa:

- Lista de todas las transacciones con scroll infinito
- Filtros: por tipo (ingreso/gasto), categoría, cuenta, rango de fechas
- Barra de búsqueda para buscar por descripción
- FAB para agregar nueva transacción
- Swipe para eliminar
- Tap para ver detalle/editar
- Tabs: "Todos" | "Ingresos" | "Gastos"

### 5.3 Pantalla de Cuentas (NUEVO)

Reemplazar el "Slideshow" stub:

- Lista de cuentas financieras como cards
- Cada card muestra: nombre, tipo, balance, ícono, color
- Agregar/editar/eliminar cuentas
- Tipos: Efectivo, Cuenta de cheques, Ahorro, Tarjeta de crédito, Inversión, Wallet digital
- Total consolidado de todas las cuentas

### 5.4 Pantalla de Presupuestos (NUEVO — agregar al nav)

- Lista de presupuestos con barra de progreso circular
- Porcentaje gastado vs presupuesto
- Alerta visual cuando se supera el umbral (80% por default)
- Crear/editar/eliminar presupuestos
- Asociar a categoría específica o general

### 5.5 Pantalla de Metas Financieras (NUEVO — agregar al nav)

- Cards con progreso visual (barra de progreso + porcentaje)
- Meta: nombre, monto objetivo, monto actual, fecha límite
- Botón "Aportar" para agregar dinero a la meta
- Celebración visual al completar una meta 🎉

### 5.6 Pantalla de Reportes/Analítica (NUEVO)

- Gráfica de líneas: tendencia de ingresos vs gastos (6-12 meses)
- Gráfica de dona: distribución por categoría
- Gráfica de barras: comparativa mensual
- Resumen semanal / mensual / anual
- Exportar a CSV y PDF

### 5.7 Pantalla de Settings (REEMPLAZAR stub)

- **Perfil**: editar nombre, avatar, email
- **Moneda**: selector de moneda preferida
- **Seguridad**: cambiar contraseña, biometría
- **Notificaciones**: toggles para alertas
- **Tema**: Dark/Light/Sistema
- **Datos**: exportar, importar, eliminar cuenta
- **Acerca de**: versión, créditos
- **Cerrar sesión**
- **Conectar wearable**: ver dispositivos vinculados

### 5.8 Onboarding (NUEVO)

Flujo de 3-4 pantallas para nuevos usuarios:
1. "Bienvenido a Finanzas" — breve intro
2. "Configura tu primera cuenta" — agregar cuenta principal
3. "Elige tu moneda" — selector
4. "¡Listo!" — ir al dashboard

### 5.9 Actualizar navegación

La bottom nav debe tener ahora **5 tabs** (o usar un drawer para más):

```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home)
    object Transactions : Screen("transactions", "Movimientos", Icons.Filled.Receipt)
    object Budgets : Screen("budgets", "Presupuestos", Icons.Filled.PieChart)
    object Accounts : Screen("accounts", "Cuentas", Icons.Filled.AccountBalance)
    object Settings : Screen("settings", "Más", Icons.Filled.MoreHoriz)
}
// Reportes, Metas y Categorías accesibles desde "Más" o desde el dashboard
```

### 5.10 Dependencias nuevas para app Android

```kotlin
// build.gradle.kts (app)
dependencies {
    // === ELIMINAR Supabase ===
    // Quitar: postgrest-kt, auth-kt, realtime-kt, ktor-client-cio

    // === AGREGAR ===
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.+")
    implementation("com.squareup.retrofit2:converter-gson:2.9.+")
    implementation("com.squareup.okhttp3:okhttp:4.12.+")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.+")

    // Local Database (offline)
    implementation("androidx.room:room-runtime:2.6.+")
    implementation("androidx.room:room-ktx:2.6.+")
    kapt("androidx.room:room-compiler:2.6.+")  // o ksp

    // Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.+")

    // DataStore (preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.+")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.+")

    // Work Manager (sync & notificaciones)
    implementation("androidx.work:work-runtime-ktx:2.9.+")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.+")

    // Coil (imágenes)
    implementation("io.coil-kt:coil-compose:2.5.+")

    // Wearable Data Layer (comunicación con reloj)
    implementation("com.google.android.gms:play-services-wearable:18.1.+")
}
```

---

## 6. MÓDULO WEAR OS (WEARABLE)

### 6.1 Configuración del módulo

**`settings.gradle.kts`** — agregar:
```kotlin
include(":app")
include(":wear")
```

**`wear/build.gradle.kts`**:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.example.finanzasapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finanzasapp"
        minSdk = 30  // Wear OS 3.0+
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Wear OS Compose
    implementation("androidx.wear.compose:compose-material3:1.0.+")
    implementation("androidx.wear.compose:compose-foundation:1.4.+")
    implementation("androidx.wear.compose:compose-navigation:1.4.+")
    
    // Horologist (Google's Wear OS helper library)
    implementation("com.google.android.horologist:horologist-compose-layout:0.6.+")
    implementation("com.google.android.horologist:horologist-compose-material:0.6.+")
    
    // Tiles
    implementation("androidx.wear.tiles:tiles:1.4.+")
    implementation("androidx.wear.tiles:tiles-material:1.4.+")
    implementation("androidx.wear.tiles:tiles-tooling-preview:1.4.+")
    
    // Complications
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.+")
    
    // Data Layer (sync con teléfono)
    implementation("com.google.android.gms:play-services-wearable:18.1.+")
    
    // Retrofit (API directa desde reloj cuando hay WiFi)
    implementation("com.squareup.retrofit2:retrofit:2.9.+")
    implementation("com.squareup.retrofit2:converter-gson:2.9.+")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.+")
    kapt("com.google.dagger:hilt-compiler:2.48.+")
}
```

### 6.2 Pantallas del Wear OS

#### Pantalla principal del reloj
```
    ┌──────────────┐
   │   ⌚ Finanzas  │
   │               │
   │  Balance:     │
   │  $12,450      │
   │               │
   │  Hoy: -$350   │
   │  Semana: -$1.2k│
   │               │
   │  [+ Gasto]    │
   └──────────────┘
```

#### Quick Expense (gasto rápido desde el reloj)
```
    ┌──────────────┐
   │  Gasto Rápido │
   │               │
   │  $___         │ ← Input numérico circular (crown)
   │               │
   │  🍽️ Comida    │ ← Selector de categoría (scroll)
   │  🚗 Transport │
   │  ☕ Café      │
   │               │
   │  [Guardar ✓]  │
   └──────────────┘
```

#### Últimas transacciones
```
    ┌──────────────┐
   │  Recientes    │
   │               │
   │  🍽️ -$150     │
   │  Comida · Hoy │
   │  ─────────── │
   │  🚗 -$89      │
   │  Uber · Ayer  │
   │  ─────────── │
   │  💰 +$15,000  │
   │  Salario · 1N │
   └──────────────┘
```

#### Vista de presupuestos
```
    ┌──────────────┐
   │  Presupuestos │
   │               │
   │  ┌─── 85% ─┐ │
   │  │ 🍽️      │ │  ← Arco circular de progreso
   │  │ Comida   │ │
   │  │ $4,250/  │ │
   │  │ $5,000   │ │
   │  └─────────┘ │
   │  ⚠️ Cerca     │
   └──────────────┘
```

### 6.3 Tile Service (widget en watch face)

El Tile muestra en la carátula del reloj:
- Balance total
- Gasto del día
- Acceso rápido a "Agregar gasto"

### 6.4 Complication

Un complication para watch faces que muestra:
- **Short text**: Balance total formateado (`$12.4k`)
- **Ranged value**: Porcentaje del presupuesto mensual usado
- Al tocar → abre la app en el reloj

### 6.5 Sincronización de datos

```
Estrategia: Dual-path

1. Si el reloj tiene WiFi/LTE → Llama directamente a la API REST
2. Si no tiene conexión propia → Usa Wearable Data Layer API para sincronizar con el teléfono
3. El teléfono actúa como proxy cuando el reloj no tiene internet propio
```

**Data Layer channels:**
```kotlin
// Paths para DataLayer
const val PATH_BALANCE = "/finanzas/balance"          // Teléfono → Reloj
const val PATH_RECENT = "/finanzas/recent"             // Teléfono → Reloj
const val PATH_BUDGETS = "/finanzas/budgets"            // Teléfono → Reloj
const val PATH_QUICK_EXPENSE = "/finanzas/quick-expense" // Reloj → Teléfono
const val PATH_SYNC_REQUEST = "/finanzas/sync"          // Reloj → Teléfono (pedir datos frescos)
```

---

## 7. LANDING PAGE — ESTILO RYOKU.DEV

### 7.1 Análisis del estilo ryoku.dev

La página de ryoku.dev tiene estas características clave:

| Elemento | Estilo |
|---|---|
| **Fondo** | Negro profundo (`#0a0a0a` a `#0f0f0f`) |
| **Texto principal** | Blanco/gris claro (`#f5f5f5`) |
| **Texto secundario** | Gris apagado (`#888`, `#666`) |
| **Acento** | Color vibrante (en nuestro caso: verde dinero `#10B981` o azul `#6366F1`) |
| **Cards** | Glassmorphism: `backdrop-filter: blur(12px)`, borde sutil `rgba(255,255,255,0.08)`, fondo `rgba(255,255,255,0.03)` |
| **Tipografía** | Sans-serif limpia (Inter / Space Grotesk) |
| **Layout** | Secciones verticales con mucho espaciado (`padding: 80px-120px`) |
| **Nav** | Fixed/sticky, minimalista, texto limpio |
| **Animaciones** | Fade-in suave al scroll, hover con glow sutil |
| **Bordes** | `border-radius: 12px-16px`, bordes de 1px con opacidad baja |
| **Iconografía** | Emojis japoneses / kanji como decoración (力, 美, etc.) |
| **Sombras** | Glow difuso en lugar de box-shadow tradicional |
| **Gradientes** | Sutiles en textos de hero y bordes de cards |
| **Navegación lateral** | Índice numérico de secciones (01, 02, 03...) |
| **Footer** | Multi-columna con links agrupados |

### 7.2 Estructura de secciones de la landing

```
┌─────────────────────────────────────────────────────────────┐
│  NAV (sticky)                                                │
│  [Finanzas]  Features  Wearable  Stack  FAQ  Descargar      │
│                                             [GitHub ★]       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  HERO (sección 00)                                           │
│                                                              │
│           Finanzas App                                       │
│           ─────────────                                      │
│     💰 Tus finanzas, en tu muñeca                            │
│     tu dinero, tu control, tu futuro                         │
│                                                              │
│     Control total de tus finanzas personales desde tu        │
│     Android y tu smartwatch. Gastos, ingresos, presupuestos  │
│     y metas — todo en un solo lugar.                         │
│                                                              │
│     [Descargar APK]  [★ GitHub]                              │
│                                                              │
│     ┌──────────────────────────────────┐                    │
│     │  [Mockup del teléfono + reloj]   │                    │
│     └──────────────────────────────────┘                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  GALERÍA (sección 01)                                        │
│  "Míralo en acción"                                          │
│                                                              │
│  [Screenshot 1] [Screenshot 2] [Screenshot 3] [Screenshot 4]│
│  Strip horizontal scrollable con capturas de la app          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  MANIFIESTO (sección 02)                                     │
│  "No es otra app de gastos. Es tu sistema financiero."       │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ 📱 Teléfono  │  │ ⌚ Muñeca   │  │ 🔒 Tu datos │         │
│  │ Dashboard    │  │ Gasto       │  │ MySQL       │         │
│  │ completo con │  │ rápido      │  │ propio,     │         │
│  │ gráficas y   │  │ desde tu    │  │ nada en     │         │
│  │ análisis     │  │ reloj       │  │ la nube     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                              │
│  Tres pilares al estilo ryoku:                               │
│  💱 Controla todo. Sin sorpresas.                            │
│  ⌚ Mismo flujo, desde el reloj.                             │
│  🔐 Tus datos son tuyos.                                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  FEATURES (sección 03) — "Todo incluido"                     │
│                                                              │
│  Grid de features con ícono + nombre + descripción:          │
│                                                              │
│  💰 Transacciones    📊 Dashboard       🎯 Metas            │
│  📈 Reportes         💳 Cuentas         📋 Presupuestos     │
│  🔄 Recurrentes      ⌚ Wear OS         🔔 Alertas          │
│  📤 Exportar         🌙 Dark Mode       🔐 Biometría        │
│  🌎 Multi-moneda     📱 Offline         🏷️ Categorías       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  WEARABLE (sección 04) — "En tu muñeca"                     │
│                                                              │
│  ┌──────────────────┐   ┌──────────────────┐                │
│  │  [Mockup reloj   │   │  Características │                │
│  │   con la app]    │   │  • Balance rápido │                │
│  │                  │   │  • Gasto en 3 seg │                │
│  │                  │   │  • Tile en carátu │                │
│  │                  │   │  • Sync automátic │                │
│  └──────────────────┘   └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  TECH STACK (sección 05) — "Construido con"                  │
│                                                              │
│  Badges/pills con las tecnologías:                           │
│  [Kotlin] [Jetpack Compose] [Wear OS] [Material 3]          │
│  [MySQL 8] [Ktor] [Hilt] [Room] [Retrofit]                  │
│  [JWT] [Vico Charts] [Horologist]                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  FAQ (sección 06) — "Preguntas comunes"                      │
│                                                              │
│  Estilo accordion/expandible:                                │
│  ▸ ¿Necesito un smartwatch?                                  │
│  ▸ ¿Mis datos están seguros?                                 │
│  ▸ ¿Funciona sin internet?                                   │
│  ▸ ¿Cómo instalo el backend?                                │
│  ▸ ¿Puedo cambiar la moneda?                                │
│  ▸ ¿Es gratis?                                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  FOOTER                                                      │
│                                                              │
│  Finanzas App                                                │
│  💰 Tu dinero, tu control.                                   │
│                                                              │
│  Explorar          Proyecto         Comunidad                │
│  • Features        • Docs           • GitHub                 │
│  • Wearable        • Source          • Issues                │
│  • FAQ             • Releases        • Contribuir            │
│                                                              │
│  © 2026 Finanzas App · Hecho con 💚 en México               │
└─────────────────────────────────────────────────────────────┘
```

### 7.3 Código base de la landing page

La landing debe ser un archivo HTML estático auto-contenido con:

```html
<!-- Estructura base -->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finanzas App — Tus finanzas, en tu muñeca</title>
    <meta name="description" content="Control total de tus finanzas personales desde Android y Wear OS. Gastos, ingresos, presupuestos y metas.">

    <!-- Fonts: Inter + Space Grotesk (como ryoku.dev) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Space+Grotesk:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
```

### CSS Variables del sistema de diseño (estilo ryoku.dev):

```css
:root {
    /* Fondos */
    --bg-primary: #0a0a0a;
    --bg-secondary: #111111;
    --bg-card: rgba(255, 255, 255, 0.03);
    --bg-card-hover: rgba(255, 255, 255, 0.06);

    /* Bordes */
    --border-subtle: rgba(255, 255, 255, 0.08);
    --border-hover: rgba(255, 255, 255, 0.15);

    /* Texto */
    --text-primary: #f5f5f5;
    --text-secondary: #a1a1a1;
    --text-muted: #666666;

    /* Acento — Verde dinero */
    --accent: #10B981;
    --accent-glow: rgba(16, 185, 129, 0.15);
    --accent-dim: rgba(16, 185, 129, 0.5);

    /* Acento secundario — Índigo */
    --accent-secondary: #6366F1;

    /* Glassmorphism */
    --glass-bg: rgba(255, 255, 255, 0.03);
    --glass-border: rgba(255, 255, 255, 0.08);
    --glass-blur: blur(12px);

    /* Tipografía */
    --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-display: 'Space Grotesk', var(--font-sans);

    /* Espaciado de secciones */
    --section-padding: 100px;

    /* Border radius */
    --radius-sm: 8px;
    --radius-md: 12px;
    --radius-lg: 16px;
    --radius-xl: 24px;
}
```

### Clases CSS esenciales para replicar el estilo:

```css
/* Card glassmorphism al estilo ryoku */
.glass-card {
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-lg);
    backdrop-filter: var(--glass-blur);
    -webkit-backdrop-filter: var(--glass-blur);
    padding: 24px;
    transition: all 0.3s ease;
}

.glass-card:hover {
    background: var(--bg-card-hover);
    border-color: var(--border-hover);
    box-shadow: 0 0 30px var(--accent-glow);
}

/* Texto con gradiente (hero title) */
.gradient-text {
    background: linear-gradient(135deg, var(--accent) 0%, var(--accent-secondary) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* Animación fade-in al scroll */
.fade-in {
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.6s ease, transform 0.6s ease;
}

.fade-in.visible {
    opacity: 1;
    transform: translateY(0);
}

/* Navegación sticky */
.nav {
    position: fixed;
    top: 0;
    width: 100%;
    z-index: 100;
    background: rgba(10, 10, 10, 0.8);
    backdrop-filter: blur(20px);
    border-bottom: 1px solid var(--border-subtle);
}

/* Pill/badge para tech stack */
.tech-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 14px;
    border-radius: 999px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 500;
    transition: all 0.2s;
}

.tech-badge:hover {
    border-color: var(--accent);
    color: var(--accent);
}

/* Índice de secciones lateral (como ryoku) */
.section-index {
    position: fixed;
    right: 32px;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 16px;
    z-index: 50;
}

.section-index a {
    color: var(--text-muted);
    font-size: 12px;
    font-family: var(--font-display);
    text-decoration: none;
    transition: color 0.2s;
}

.section-index a.active {
    color: var(--accent);
}
```

### JavaScript para animaciones:

```javascript
// Intersection Observer para fade-in al scroll
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
        }
    });
}, { threshold: 0.1 });

document.querySelectorAll('.fade-in').forEach(el => observer.observe(el));

// Smooth scroll para navegación
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// Actualizar índice de sección activa
const sections = document.querySelectorAll('section');
const navLinks = document.querySelectorAll('.section-index a');

window.addEventListener('scroll', () => {
    let current = '';
    sections.forEach(section => {
        const sectionTop = section.offsetTop - 200;
        if (scrollY >= sectionTop) {
            current = section.getAttribute('id');
        }
    });
    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${current}`) {
            link.classList.add('active');
        }
    });
});
```

---

## 8. SISTEMA DE DISEÑO Y TEMAS

### 8.1 Paleta de colores para la app Android

Adaptar el estilo dark de ryoku.dev al tema Material 3:

```kotlin
// ui/theme/Color.kt — REEMPLAZAR contenido
object FinanzasColors {
    // Primary (Verde dinero — acento principal)
    val Primary = Color(0xFF10B981)
    val PrimaryDark = Color(0xFF059669)
    val PrimaryLight = Color(0xFF34D399)
    val OnPrimary = Color(0xFF000000)

    // Secondary (Índigo)
    val Secondary = Color(0xFF6366F1)
    val SecondaryDark = Color(0xFF4F46E5)
    val OnSecondary = Color(0xFFFFFFFF)

    // Background (Dark theme)
    val Background = Color(0xFF0A0A0A)
    val Surface = Color(0xFF111111)
    val SurfaceVariant = Color(0xFF1A1A1A)
    val CardBackground = Color(0x08FFFFFF)  // 3% white

    // Text
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFFA1A1A1)
    val TextMuted = Color(0xFF666666)

    // Border
    val Border = Color(0x14FFFFFF)  // 8% white
    val BorderHover = Color(0x26FFFFFF)  // 15% white

    // Status colors
    val Income = Color(0xFF10B981)   // Verde
    val Expense = Color(0xFFEF4444)  // Rojo
    val Warning = Color(0xFFF59E0B)  // Amarillo
    val Info = Color(0xFF3B82F6)     // Azul

    // Category colors
    val CategoryFood = Color(0xFFEF4444)
    val CategoryTransport = Color(0xFFF59E0B)
    val CategoryEntertainment = Color(0xFF8B5CF6)
    val CategoryHealth = Color(0xFF10B981)
    val CategoryEducation = Color(0xFF3B82F6)
    val CategoryServices = Color(0xFF6366F1)
    val CategoryClothing = Color(0xFFEC4899)
    val CategoryHousing = Color(0xFF14B8A6)
    val CategoryOther = Color(0xFF6B7280)
}
```

### 8.2 Tipografía

```kotlin
// ui/theme/Type.kt
// Usar Inter como fuente principal (como ryoku.dev)
// Si no se quiere agregar fuentes custom, Material 3 default está bien
// pero lo ideal es usar la Google Fonts Inter:

val FinanzasTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, // o Inter si se agrega
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## 9. CHECKLIST DE IMPLEMENTACIÓN

### Orden recomendado de implementación:

#### Fase 1: Backend + MySQL (Prioridad ALTA)
- [ ] Crear módulo `backend/` con Ktor
- [ ] Configurar conexión a MySQL con HikariCP + Exposed
- [ ] Crear tablas MySQL (ejecutar script SQL de sección 3)
- [ ] Implementar endpoints de Auth (register, login, JWT)
- [ ] Implementar CRUD de transacciones
- [ ] Implementar CRUD de categorías
- [ ] Implementar CRUD de cuentas
- [ ] Implementar CRUD de presupuestos
- [ ] Implementar CRUD de metas financieras
- [ ] Implementar transacciones recurrentes
- [ ] Implementar endpoints de summary/analytics
- [ ] Implementar endpoints wearable (simplificados)
- [ ] Tests de los endpoints

#### Fase 2: Migrar App Android a backend propio
- [ ] Eliminar dependencias de Supabase
- [ ] Agregar Retrofit + OkHttp
- [ ] Crear interfaces API (AuthApi, TransactionApi, etc.)
- [ ] Crear AuthInterceptor para JWT
- [ ] Crear repositorios con lógica offline-first (Room + API)
- [ ] Configurar Room Database con DAOs y Entities
- [ ] Actualizar módulos Hilt (NetworkModule, DatabaseModule)
- [ ] Migrar AuthViewModel a usar backend propio
- [ ] Migrar HomeViewModel

#### Fase 3: Nuevas pantallas Android
- [ ] Rediseñar Home → Dashboard completo con gráficas
- [ ] Implementar pantalla de Transacciones (reemplazar Reflow)
- [ ] Implementar pantalla de Cuentas (reemplazar Slideshow)
- [ ] Implementar pantalla de Presupuestos
- [ ] Implementar pantalla de Metas Financieras
- [ ] Implementar pantalla de Reportes con Vico charts
- [ ] Implementar Settings completo (perfil, seguridad, tema, datos)
- [ ] Implementar Onboarding flow
- [ ] Implementar gestión de Categorías
- [ ] Actualizar navegación (5 tabs)
- [ ] Implementar búsqueda de transacciones
- [ ] Implementar notificaciones (budget alerts, recordatorios)
- [ ] Implementar exportación CSV/PDF
- [ ] Aplicar tema dark estilo ryoku.dev

#### Fase 4: Módulo Wear OS
- [ ] Crear módulo `:wear` en el proyecto
- [ ] Configurar `wear/build.gradle.kts`
- [ ] Crear tema Wear OS
- [ ] Implementar pantalla de Balance (principal)
- [ ] Implementar Quick Expense (gasto rápido)
- [ ] Implementar lista de transacciones recientes
- [ ] Implementar vista de presupuestos con arco circular
- [ ] Implementar Tile Service (widget en carátula)
- [ ] Implementar Complication (balance en watch face)
- [ ] Implementar Data Layer sync (teléfono ↔ reloj)
- [ ] Implementar llamadas API directas (WiFi/LTE)
- [ ] Tests en emulador Wear OS

#### Fase 5: Landing Page
- [ ] Crear carpeta `landing/`
- [ ] Implementar HTML/CSS/JS estilo ryoku.dev
- [ ] Sección Hero con mockups
- [ ] Sección Galería con screenshots
- [ ] Sección Manifiesto (3 pilares)
- [ ] Sección Features (grid)
- [ ] Sección Wearable
- [ ] Sección Tech Stack (badges)
- [ ] Sección FAQ (accordion)
- [ ] Footer multi-columna
- [ ] Navegación sticky con glassmorphism
- [ ] Índice lateral de secciones (01, 02, 03...)
- [ ] Animaciones fade-in al scroll
- [ ] Responsive design (mobile-first)
- [ ] Deploy a GitHub Pages / Vercel

#### Fase 6: Polish & QA
- [ ] Testing end-to-end
- [ ] Performance optimization
- [ ] Accesibilidad (a11y)
- [ ] Internacionalización (es/en)
- [ ] README.md completo
- [ ] Screenshots para Play Store
- [ ] Generar APK firmado

---

> **NOTA PARA LA IA QUE IMPLEMENTE ESTO:**
> - El proyecto actual es Android nativo en Kotlin con Jetpack Compose.
> - Actualmente usa Supabase — hay que migrar COMPLETAMENTE a MySQL con backend propio.
> - Los archivos stub (Reflow, Slideshow, Settings, Transform) deben ser REEMPLAZADOS, no modificados.
> - Las credenciales de Supabase están hardcodeadas en `SupabaseModule.kt` — eliminarlas.
> - La app tiene categorías hardcodeadas — deben ser dinámicas desde la DB.
> - NO existe ningún código de Wear OS — hay que crear el módulo desde cero.
> - La landing page es un sitio web independiente, NO parte de la app Android.
> - Seguir los patrones de arquitectura existentes (MVVM + Hilt).
> - Mantener compatibilidad con Min SDK 26 para el teléfono y Min SDK 30 para Wear OS.
