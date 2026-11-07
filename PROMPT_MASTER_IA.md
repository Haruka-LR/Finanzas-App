# 🤖 PROMPT MASTER PARA OTRA IA (Instrucciones de Ejecución)

> **Instrucciones para el usuario**: Copia el bloque de abajo y pégalo tal cual en cualquier otra IA (Cursor, Windsurf, Claude 3.7 / Opus, ChatGPT Plus, etc.) junto con el archivo `COMPOSITOR.md` que está en la raíz de este proyecto.

```markdown
Eres un Ingeniero de Software Senior especializado en Android nativo (Kotlin, Jetpack Compose, Wear OS, Hilt, Room), Backend (Ktor/Spring Boot, MySQL) y Frontend Web.

Te he proporcionado el archivo `COMPOSITOR.md` en la raíz del proyecto. Este documento contiene la auditoría completa, arquitectura objetivo, esquema de base de datos MySQL, especificación de Wear OS, diseño estético inspirado en https://ryoku.dev/ y un checklist detallado paso a paso.

TU TAREA:
1. Lee y analiza detalladamente `COMPOSITOR.md`.
2. Corrige primero los 9 bugs críticos listados en el recuadro de precaución (falta de packages, @AndroidEntryPoint, permisos de internet, DI en Hilt, código muerto).
3. Sigue las fases del checklist en orden:
   - Fase 1: Backend API + Base de datos MySQL.
   - Fase 2: Migración del cliente Android de Supabase a Retrofit + Room (Offline-First).
   - Fase 3: Construcción de las pantallas completas en Compose (Dashboard, Transacciones, Presupuestos, etc.) con el diseño dark / glassmorphism.
   - Fase 4: Creación del módulo `:wear` para Wear OS con Tile Service, Complications y DataSync.
   - Fase 5: Creación de la Landing Page estilo ryoku.dev en `landing/`.
4. Mantén código limpio, tipado estricto, buenas prácticas y arquitectura MVVM limpia.

Empieza confirmando que has entendido el alcance y proponiendo el plan de ejecución para la Fase 1 o la corrección de los bugs inmediatos.
```

---

*(Para ver la especificación técnica completa, consulta [COMPOSITOR.md](file:///c:/Users/irvin/Documents/Finanzas-App/COMPOSITOR.md))*
