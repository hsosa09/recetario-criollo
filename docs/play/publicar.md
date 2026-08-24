# Cómo firmar y publicar Recetario Criollo

Pasos concretos para la Fase 7. Todo se corre desde la raíz del proyecto con el JDK
apuntado a mano:

```bash
export JAVA_HOME=/home/horacio/.java/jdk/jdk-25+36
```

## 1. Crear la clave de firma (una sola vez)

La clave se guarda **fuera del repo**. Si se pierde, no se puede volver a subir una
actualización de la app a Play: conviene tener una copia en otro lado.

```bash
mkdir -p ~/claves
$JAVA_HOME/bin/keytool -genkeypair -v \
  -keystore ~/claves/recetario-release.jks \
  -alias recetario -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=<tu nombre y apellido>, C=UY"
```

Pide una contraseña; con la misma para el almacén y para la clave alcanza.

Después, copiar `keystore.properties.ejemplo` a `keystore.properties` (está en el
`.gitignore`) y completar:

```properties
storeFile=/home/horacio/claves/recetario-release.jks
storePassword=...
keyAlias=recetario
keyPassword=...
```

Sin ese archivo el build de release igual funciona, pero sale sin firmar.

## 2. Armar el artefacto

```bash
./gradlew :app:testDebugUnitTest        # que la matemática siga bien
./gradlew :app:bundleRelease            # AAB para Play
./gradlew :app:assembleRelease          # APK, para probar en el teléfono a mano
```

Salidas:

- `app/build/outputs/bundle/release/app-release.aab` → esto es lo que se sube a Play.
- `app/build/outputs/apk/release/app-release.apk` → para instalar y probar.
- `app/build/outputs/mapping/release/mapping.txt` → **subir a Play junto con el AAB**,
  es lo que permite leer los stacktraces con R8 activado.

Verificar la firma antes de subir:

```bash
~/Android/Sdk/build-tools/*/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

## 3. Probar el release en el teléfono

El release lleva R8 (minify + shrink), así que **hay que probarlo instalado**, no alcanza
con que compile. El debug tiene el sufijo `.debug` en el applicationId, así que las dos
versiones conviven en el teléfono.

```bash
adb connect <ip-del-teléfono>:<puerto>      # depuración inalámbrica
adb install -r app/build/outputs/apk/release/app-release.apk
```

Recorrido mínimo antes de publicar:

- [ ] Crear una receta con foto, editarla y borrarla.
- [ ] Escalar porciones para arriba y para abajo; mirar que la sal no se duplique y que
      las cantidades queden en fracciones medibles.
- [ ] Conversor: las cuatro pestañas, con un ingrediente sin densidad cargada.
- [ ] Arrancar dos cronómetros, cerrar la app y esperar el aviso.
- [ ] "Con lo que tengo": tildar ingredientes y ver el orden por coincidencia.
- [ ] Dar de alta un ingrediente nuevo desde el formulario guiado.
- [ ] Rotar la pantalla y revisar modo oscuro.
- [ ] Reiniciar el teléfono con un cronómetro andando.

## 4. Capturas para la ficha

Con el teléfono conectado:

```bash
adb exec-out screencap -p > docs/play/capturas/01-recetas.png
```

Las cinco sugeridas están listadas al final de `ficha.md`.

## 5. Play Console

1. Crear la app: nombre, idioma español (Latinoamérica), gratis.
2. Pegar los textos de `ficha.md` y subir `icono-512.png` y `grafica-1024x500.png`.
3. Publicar `politica-privacidad.md` en una URL accesible (un Gist público o una página
   de GitHub Pages alcanza) y pegar el enlace en la ficha. Play exige una URL, no un
   archivo.
4. Completar Seguridad de los datos y la clasificación de contenido con las respuestas
   de `ficha.md`.
5. Subir el AAB y el `mapping.txt` a una prueba interna primero; instalar desde Play y
   repetir el recorrido del punto 3.
6. Recién ahí, promover a producción.

## 6. Siguientes versiones

Subir `versionCode` (entero, +1 cada subida) y `versionName` en `app/build.gradle.kts`.
Play rechaza un `versionCode` repetido.
