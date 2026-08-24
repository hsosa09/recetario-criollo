# Traspaso a otra máquina

Notas para retomar el desarrollo de Recetario Criollo en una PC distinta.
Escrito el 2026-08-24, con las Fases 1 a 6 terminadas y la Fase 7 a medias.

## 1. Qué hay que instalar en la máquina nueva

| Cosa | Versión / ruta acá | Nota |
| --- | --- | --- |
| JDK | 25 (`/home/horacio/.java/jdk/jdk-25+36`) | En la PC vieja `java` no estaba en el PATH. Si en la nueva sí está, mejor. |
| Android SDK | `/home/horacio/Android/Sdk` | Hace falta `platforms;android-37.1` (el proyecto compila contra `compileSdk 37`). |
| Gradle | lo trae el wrapper (9.5) | No instalar nada aparte. |
| Python + Pillow | solo para regenerar los gráficos de Play | `python3 -m venv` + `pip install pillow`, ver `docs/play/generar_graficos.py`. |

Instalar la plataforma del SDK, si falta:

```bash
sdkmanager "platforms;android-37.1"
```

Primer build para verificar que quedó todo bien:

```bash
export JAVA_HOME=/ruta/al/jdk        # ajustar a la máquina nueva
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Tienen que pasar los tests de `Escalador`, `Fracciones`, `Conversor` y
`BuscadorPorIngredientes`, y salir el APK de debug.

## 2. Lo que NO viene en el repo (a propósito)

- **`local.properties`** — la ruta del SDK. Android Studio lo genera solo al abrir el
  proyecto; si no, crearlo con `sdk.dir=/ruta/al/Android/Sdk`.
- **`.claude/settings.local.json`** — ajustes de Claude Code con rutas absolutas de esta
  máquina (`JAVA_HOME`, `ANDROID_HOME` y los permisos de `adb`). Hay que rehacerlo con las
  rutas nuevas; `.claude/settings.json` (permisos del proyecto) sí viene en el repo.
- **La clave de firma** (`keystore.properties` + el `.jks`). **Todavía no existe**: nunca se
  generó. Cuando se genere, va fuera del repo y con copia de respaldo aparte — si se pierde
  no se puede volver a publicar la app en Play. Los pasos están en `docs/play/publicar.md`.

## 3. En qué quedó cada cosa

**Terminado y compilando:** Fases 1 a 6 completas — CRUD de recetas con foto y favoritos,
escalador de porciones con reglas por ingrediente, conversor (medidas, densidad, horno,
levadura), cronómetros múltiples con alarma del sistema, búsqueda por ingredientes
disponibles y generador guiado con plantillas. Detalle en `CLAUDE.md` y `README.md`.

**Fase 7, hecho el 2026-08-24:**

- Release con R8 (`minify` + `shrinkResources`) y reglas propias en `app/proguard-rules.pro`
  para kotlinx.serialization y las entidades de Room. Verificado que los `$$serializer`
  sobreviven al shrink. APK 1,9 MB / AAB 4,6 MB.
- Firma de release leída de `keystore.properties`; sin ese archivo el release igual se arma,
  sin firmar. Ver `keystore.properties.ejemplo`.
- Debug con `applicationIdSuffix = ".debug"`, para tener las dos versiones instaladas juntas.
- Ícono definitivo (vector propio) con fondo en degradado y variante monocroma para los
  iconos temáticos de Android 13+.
- Reglas de backup: se respalda la base y las fotos, se excluyen los cronómetros.
- `windowBackground` claro/oscuro, sin flash blanco al abrir.
- Ficha de Play completa en `docs/play/`: textos, política de privacidad, checklist de
  publicación y los dos gráficos (ícono 512 y gráfica destacada) con su script generador.

## 4. Qué sigue, en orden

1. **Probar el APK de release instalado en un teléfono.** Es lo más importante y lo único
   que quedó bloqueado: el ADB inalámbrico del Xiaomi se desconectó y no se pudo hacer.
   El release lleva R8, así que puede romper cosas que el compilador no ve (Room,
   serialización, navegación type-safe). Que compile **no alcanza**. El recorrido mínimo
   está en `docs/play/publicar.md`, punto 3.
2. **Sacar las 5 capturas** para la ficha (lista, detalle con escalador, conversor,
   cronómetros, "Con lo que tengo"). Comando en `publicar.md`, punto 4.
3. **Generar la clave de firma** y completar `keystore.properties`.
4. **Publicar la política de privacidad** (`docs/play/politica-privacidad.md`) en una URL
   pública: Play exige un enlace, no un archivo.
5. **Alta en Play Console**: pegar los textos de `docs/play/ficha.md`, subir el AAB junto
   con `mapping.txt`, prueba interna primero y recién después producción.

## 5. Cómo llevar el repo a la otra máquina

El historial vive en un repo privado **local** (bare), que hace de `origin`:

```
/home/horacio/git-privados/recetario-criollo.git
```

Para el traspaso hay además un bundle con todo el historial en un solo archivo:

```
/home/horacio/recetario-criollo.bundle
```

Copiar ese archivo por pendrive o nube y, en la máquina nueva:

```bash
git clone recetario-criollo.bundle Recetario
cd Recetario
git remote remove origin        # el bundle no sirve como remoto vivo
```

Para traer cambios nuevos desde la máquina vieja, se regenera el bundle allá
(`git bundle create recetario-criollo.bundle --all`) y acá se hace
`git pull /ruta/al/recetario-criollo.bundle main`.

### Si más adelante se quiere un repo privado en GitHub

Eso necesita una cuenta y un login que hay que hacer a mano una sola vez:

```bash
sudo apt install gh
gh auth login
gh repo create recetario-criollo --private --source=. --push
```

Desde ahí, las dos máquinas trabajan contra el mismo `origin` y se termina el ida y
vuelta de bundles. **El repo tiene que ser privado**: no por el código, sino porque es
el lugar donde uno se descuida y termina subiendo el `keystore.properties`.

## 6. Detalles del build que conviene no olvidar

- `gradle.properties` lleva `android.disallowKotlinSourceSets=false`: AGP 9 trae Kotlin
  incorporado y KSP registra sus fuentes generadas por el DSL viejo. Sin ese flag no compila.
- Los esquemas de Room quedan versionados en `app/schemas/`. Si se toca una entidad, hay que
  subir la versión de la base y escribir la migración; el esquema viejo está ahí para eso.
- La configuración de caché de Gradle está activada (`org.gradle.configuration-cache=true`).
