# Recetario Criollo

App Android 100% local para el cuaderno de recetas de casa, con escalado de porciones,
conversor de medidas de cocina, cronómetros múltiples y búsqueda por lo que hay en la heladera.

Sin backend, sin internet, sin cuentas: todo vive en el teléfono.

## Cómo compilar

En esta máquina `java` no está en el PATH, así que hay que apuntar `JAVA_HOME` al JDK:

```bash
export JAVA_HOME=/home/horacio/.java/jdk/jdk-25+36
./gradlew :app:assembleDebug        # APK de debug
./gradlew :app:testDebugUnitTest    # tests de la lógica con matemática
./gradlew installDebug              # instalar en el teléfono conectado
./gradlew :app:bundleRelease        # AAB para Play (ver docs/play/publicar.md)
```

Requiere `platforms;android-37.1` en el SDK (compileSdk 37).

## Cómo está armado

```
app/src/main/java/uy/horacio/recetariocriollo/
├── dominio/          Kotlin puro, sin Android: es donde viven las decisiones
│   ├── Escalador     recalculo de cantidades por porciones
│   ├── Fracciones    redondeo y formateo "de cocina" (1/2 taza, 2 1/3 huevos)
│   ├── Conversor     volumen/peso/densidad, horno y levadura
│   ├── BuscadorPorIngredientes   coincidencia contra lo disponible
│   ├── Plantillas    esqueletos de receta por tipo de plato
│   └── modelo/       Receta, Ingrediente, Unidad, reglas de escalado
├── datos/            Room: entidades, DAOs, repositorios, catálogo semilla, fotos
├── cronometro/       gestor de timers, alarma del sistema y notificación local
└── ui/               Compose (Material 3), un ViewModel por pantalla
```

La UI nunca toca DAOs ni entidades: habla con los repositorios y con modelos de dominio.

## Decisiones que conviene recordar

- **El catálogo de ingredientes es normalizado.** Las recetas referencian ingredientes por id;
  no hay texto libre. Por eso la búsqueda por ingredientes no tiene ambigüedad de nombres.
- **No todo escala igual.** Cada ingrediente de una receta tiene una regla: proporcional,
  atenuado (`cantidad × factor^0.75`, para sal, especias y levadura) o fijo (el aceite de freír).
- **Las cantidades se redondean para que se puedan medir**: fracciones de cocina en tazas y
  cucharadas, escalones redondos en gramos y mililitros. Nunca queda un ingrediente en cero.
- **Volumen ↔ peso necesita densidad.** Sin gramos por taza del ingrediente, la conversión
  devuelve `null` en vez de inventar un número.
- **Los cronómetros guardan el instante de fin, no los segundos restantes**, y programan
  `AlarmManager.setAlarmClock` (exacta y sin permiso especial). Siguen bien aunque se cierre la app.

## Publicación

Todo lo de la ficha de Play vive en `docs/play/`:

- `publicar.md` — crear la clave de firma, armar el AAB y el recorrido de prueba previo.
- `ficha.md` — textos de la ficha, respuestas de Seguridad de los datos y clasificación.
- `politica-privacidad.md` — hay que subirla a una URL pública; Play la exige.
- `generar_graficos.py` — regenera el ícono 512×512 y la gráfica destacada a partir del
  mismo dibujo que el ícono de la app.

El release va con R8 (`minify` + `shrinkResources`), así que **hay que probarlo instalado**
antes de subirlo: que compile no alcanza. El debug se instala con el sufijo `.debug` en el
applicationId, así que conviven los dos en el teléfono.

## Estado

Fases 1 a 6 del plan (ver `CLAUDE.md`) implementadas. De la Fase 7 queda la prueba a fondo
en dispositivo real, las capturas de pantalla y el alta en Play Console.
