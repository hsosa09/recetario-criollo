# CLAUDE.md — Recetario Criollo y Conversor de Medidas de Cocina

Este archivo es el contexto persistente del proyecto para guiar el desarrollo con Claude Code. Léelo antes de retomar trabajo en este repo.

## 1. Visión general

App Android nativa, 100% local (sin backend, sin internet requerido), para gestionar un cuaderno de recetas propio con foco en cocina criolla/tradicional uruguaya, con herramientas de escalado de porciones, conversión de medidas y búsqueda por ingredientes disponibles.

**Público:** uso personal/familiar, planificación diaria de almuerzos y cenas.

**Filosofía de diseño:** simple, rápida de abrir, sin fricción. Es una herramienta de uso diario en la cocina (posiblemente con las manos sucias/ocupadas), así que la UI debe priorizar botones grandes, poco scroll para lo frecuente, y modo "manos libres" donde sea posible (texto grande, cronómetro accesible).

## 2. Entorno de desarrollo

- Desarrollo sobre Ubuntu 26.04 LTS
- Kotlin + Jetpack Compose (Material 3)
- minSdk 26 (Android 8.0) — mismo piso que otros proyectos del usuario, buena cobertura de dispositivos reales sin complejizar compatibilidad
- targetSdk: el más reciente estable al momento de compilar
- Arquitectura: MVVM (ViewModel + StateFlow/UiState), capa de dominio simple, Repository pattern sobre Room
- Sin dependencias de red. Sin Firebase. Sin analytics.

## 3. Funcionalidades (por prioridad)

### 3.1 Núcleo — Recetario (MVP)
- Alta, edición y borrado de recetas propias
- Cada receta: nombre, categoría (entrada/plato principal/postre/panificados/etc.), porciones base, lista de ingredientes (cantidad + unidad + nombre), pasos de preparación, tiempo estimado, notas/tips
- Fotos opcionales por receta (guardadas localmente, no en la nube)
- Favoritos / marcado rápido

### 3.2 Escalador de porciones
- Dado un número de porciones deseado, recalcular automáticamente todas las cantidades de ingredientes de forma proporcional
- Casos especiales a contemplar: ingredientes que no escalan linealmente (ej. levadura, sal, especias en recetas de panadería no siempre escalan 1:1) — permitir marcar un ingrediente como "no escalable" o con regla propia
- Redondeo sensato para cocina real (ej. no mostrar "2.3333 huevos"; sugerir redondeo a fracciones útiles: 1/4, 1/2, 1/3)

### 3.3 Conversor de medidas
- Conversiones rápidas independientes de cualquier receta (pantalla/calculadora standalone):
  - Volumen: cucharaditas ↔ cucharadas ↔ tazas ↔ mililitros ↔ litros
  - Peso: gramos ↔ kilogramos ↔ onzas ↔ libras
  - Densidad específica por ingrediente común (harina, azúcar, manteca no pesan igual por taza) — tabla de equivalencias por ingrediente, no solo conversión genérica de volumen a volumen
  - Levadura fresca ↔ seca (proporción estándar ~1:3)
  - Temperaturas de horno: °C ↔ °F ↔ referencias de "horno suave/moderado/fuerte" (útil para recetas criollas viejas que usan esa terminología)

### 3.4 Cronómetro múltiple
- Varios timers simultáneos con etiqueta (ej. "torta" / "salsa"), notificación local al finalizar cada uno
- Accesible desde cualquier pantalla (posible FAB o acceso rápido persistente)

### 3.5 Búsqueda por ingredientes disponibles
- **Selección por lista, no texto libre.** El usuario elige ingredientes desde un listado del catálogo normalizado (chips o checklist con buscador tipo "filtro" para ubicar rápido dentro de la lista, pero la selección final siempre es de una opción existente del catálogo, nunca texto tipeado a mano)
- El listado se puede agrupar por categoría (verduras, lácteos, condimentos, etc.) para facilitar el tildado rápido
- La app filtra/ordena recetas por porcentaje de coincidencia (ej. "te faltan solo 2 ingredientes")
- Como todo ingrediente proviene del catálogo (ver modelo de datos), el matching es exacto por `ingredienteId`, sin ambigüedad de nombres ni errores de tipeo

### 3.6 Generador de nuevas recetas
- Alcance definido para el desarrollo completo: **formulario guiado** para crear recetas propias desde cero de forma estructurada, con plantillas por tipo de plato (entrada/plato principal/postre/panificados)
- Al cargar ingredientes en el formulario, se usa el mismo selector por lista del catálogo (punto 3.5) — si el ingrediente no existe todavía, se lo puede dar de alta ahí mismo y queda disponible en el catálogo para el resto de la app
- Sugerencias de combinaciones o variantes automáticas quedan fuera de este desarrollo (posible evolución futura, no forma parte del plan actual)

## 4. Modelo de datos (borrador inicial)

```
Receta
- id, nombre, categoria, porcionesBase, tiempoMinutos, notas, fotoPath, esFavorita

Ingrediente (catálogo normalizado, reutilizable entre recetas)
- id, nombre, densidadGramosPorTaza (nullable), esSalOEspecia (bool, afecta escalado)

RecetaIngrediente (tabla intermedia)
- recetaId, ingredienteId, cantidad, unidad, escalable (bool)

PasoPreparacion
- id, recetaId, orden, texto, timerSugeridoSegundos (nullable)
```

Room + relaciones (`@Relation`) para Receta ↔ Ingredientes ↔ Pasos. Sin cifrado (a diferencia del proyecto de salud) — no hay datos sensibles acá.

## 5. Fases de desarrollo sugeridas

1. **Fase 1 — Fundaciones:** setup del proyecto, esquema Room, CRUD básico de recetas (sin escalado ni conversor todavía)
2. **Fase 2 — Escalador de porciones:** lógica de recalculo proporcional + manejo de ingredientes no escalables + redondeo
3. **Fase 3 — Conversor de medidas:** pantalla standalone + tabla de densidades por ingrediente común
4. **Fase 4 — Cronómetro múltiple:** notificaciones locales, persistencia de timers activos si se cierra la app
5. **Fase 5 — Búsqueda por ingredientes:** normalización del catálogo de ingredientes + componente reutilizable de selector por lista (chips/checklist agrupado por categoría) + algoritmo de coincidencia
6. **Fase 6 — Generador de recetas:** formulario guiado de creación con plantillas por tipo de plato (ver 3.6), reutilizando el selector por lista de la Fase 5
7. **Fase 7 — Pulido y publicación:** ícono, nombre definitivo, ficha de Play Store, testing en dispositivo real (Xiaomi 11T)

## 6. Convenciones de código

- Nombres de paquete: `uy.horacio.recetariocriollo` (definido, en línea con los otros proyectos)
- Un ViewModel por pantalla principal, sin lógica de negocio en Composables
- Strings en `strings.xml` (español rioplatense/uruguayo, no neutro) para poder traducir a futuro si se desea
- Tests unitarios mínimos obligatorios para: lógica de escalado y lógica de conversión de medidas (son las partes con matemática, más propensas a bugs silenciosos)

## 7. Estado actual

**Fases 1 a 6 implementadas y compilando** (2026-08-23). Fase 7 en curso (2026-08-24): el proyecto ya está listo para armar el release; falta la prueba en dispositivo real, las capturas y el alta en Play Console.

Estructura: módulo único `app`, paquete `uy.horacio.recetariocriollo`, con
`dominio/` (lógica pura, sin Android), `datos/` (Room + repositorios), `cronometro/`
(timers + alarmas + notificación) y `ui/` (pantallas Compose, un ViewModel por pantalla).

Build: AGP 9.3.1, Kotlin 2.2.10, KSP, Gradle 9.5, compileSdk/targetSdk 37, minSdk 26.
En esta máquina `java` no está en el PATH: exportar
`JAVA_HOME=/home/horacio/.java/jdk/jdk-25+36` antes de correr `./gradlew`.
AGP 9 trae Kotlin incorporado, por eso `gradle.properties` lleva
`android.disallowKotlinSourceSets=false` (KSP registra sus fuentes por el DSL viejo).

Lo que ya anda:
- CRUD de recetas con foto local, favoritos, filtros por categoría y buscador.
- Escalador de porciones con reglas por ingrediente (proporcional / atenuado / fijo) y
  redondeo a fracciones de cocina.
- Conversor standalone: medidas, densidad por ingrediente, horno (°C/°F + referencias
  criollas) y levadura fresca ↔ seca.
- Cronómetros múltiples con etiqueta, persistidos y con alarma del sistema
  (`setAlarmClock`, no necesita permiso de alarmas exactas) + notificación local.
- Búsqueda por ingredientes disponibles con selección por lista del catálogo normalizado.
- Generador guiado con plantillas por tipo de plato y alta de ingredientes al catálogo.

Tests unitarios: `Escalador`, `Fracciones`, `Conversor` y `BuscadorPorIngredientes`
(`./gradlew :app:testDebugUnitTest`).

Fase 7, hecho:
- Ícono definitivo: vector propio (olla criolla) con fondo en degradado y variante
  monocroma para los iconos temáticos de Android 13+. `docs/play/generar_graficos.py`
  regenera desde el mismo dibujo el ícono 512×512 y la gráfica destacada de la ficha.
- Release con R8 (`minify` + `shrinkResources`) y reglas propias en `proguard-rules.pro`
  para kotlinx.serialization y las entidades de Room. APK 1.9 MB, AAB 4.6 MB.
- Firma leída de `keystore.properties` (fuera del repo, ver `keystore.properties.ejemplo`);
  sin ese archivo el release se arma igual pero sin firmar.
- El debug se instala con applicationId `.debug`, así conviven la de desarrollo y la de Play.
- Reglas de backup: se respalda la base y las fotos, se excluyen los cronómetros (guardan
  el instante exacto de fin y restaurados no significan nada).
- `windowBackground` por tema claro/oscuro para que no haya flash blanco al abrir.
- Ficha de Play, política de privacidad y checklist de publicación en `docs/play/`.

Para retomar en otra máquina, leer `TRASPASO.md` (qué instalar, qué no viene en el repo
y en qué orden seguir).

Fase 7, pendiente:
- Probar el APK de release **instalado** en el Xiaomi: R8 rompe cosas que el compilador no
  ve. El recorrido mínimo está en `docs/play/publicar.md`.
- Sacar las 5 capturas de pantalla para la ficha.
- Crear la clave de firma, subir la política de privacidad a una URL pública y dar de alta
  la app en Play Console.
