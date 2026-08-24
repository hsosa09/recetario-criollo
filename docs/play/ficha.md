# Ficha de Google Play — Recetario Criollo

Textos listos para pegar en Play Console. Idioma principal: **español (Latinoamérica)**.

## Datos de la app

| Campo | Valor |
| --- | --- |
| Nombre de la app (máx. 30) | `Recetario Criollo` |
| ID de aplicación | `uy.horacio.recetariocriollo` |
| Categoría | Comida y bebida |
| Etiquetas sugeridas | Recetas, Cocina, Conversor de unidades |
| Tipo | Aplicación (no juego), gratis, sin compras dentro de la app, sin anuncios |
| Público objetivo | 13 años o más (no dirigida a menores) |
| Contacto | horaciososa99@gmail.com |
| Sitio web | *(opcional, se puede dejar vacío)* |

## Descripción breve (máx. 80 caracteres)

```
Tus recetas de siempre, con escalado de porciones y conversor de medidas.
```

(73 caracteres.)

## Descripción completa (máx. 4000 caracteres)

```
Recetario Criollo es el cuaderno de recetas de casa, pasado al teléfono. Cargás tus
recetas una vez y las tenés siempre a mano, con las cuentas de la cocina ya resueltas.

Funciona 100% en el teléfono: no necesita internet, no pide cuenta y no manda nada
a ningún servidor. Tus recetas son tuyas y no salen del aparato.

QUÉ HACE

• Tu recetario. Nombre, categoría, ingredientes, pasos, tiempo, notas y foto. Marcás
  favoritas y las encontrás con el buscador o filtrando por categoría.

• Escalador de porciones. Ponés para cuántos vas a cocinar y las cantidades se ajustan
  solas. No todo escala igual: la sal, las especias y la levadura suben menos que el
  resto, y el aceite para freír queda fijo. Cada ingrediente tiene su regla.

• Cantidades que se pueden medir. Nada de "2,3333 huevos": las cantidades se redondean
  a fracciones de cocina (1/4, 1/3, 1/2 taza) y a escalones redondos en gramos y
  mililitros.

• Conversor de medidas. Cucharaditas, cucharadas, tazas, mililitros, litros, gramos,
  kilos, onzas y libras. Con densidad por ingrediente, porque una taza de harina y una
  de azúcar no pesan lo mismo.

• Horno y levadura. Grados centígrados a Fahrenheit y también las referencias de las
  recetas viejas: horno suave, moderado, fuerte. Y la equivalencia entre levadura
  fresca y seca.

• Cronómetros con nombre. Varios a la vez ("torta", "salsa"), con aviso aunque cierres
  la app o apagues la pantalla.

• Con lo que tengo. Tildás lo que hay en casa y te dice qué podés cocinar y a qué
  recetas les falta poco. Los ingredientes se eligen de una lista, así que no hay
  errores de tipeo ni nombres repetidos.

• Modo cocina. Letra grande y pantalla encendida mientras cocinás, para no andar
  tocando el teléfono con las manos sucias.

PENSADA PARA LA COCINA CRIOLLA

Las categorías, las medidas y el vocabulario son los de acá: guisos, panificados,
dulces, tazas y cucharadas. Viene con un catálogo de ingredientes ya cargado para
empezar rápido, y le podés agregar los tuyos.

PRIVACIDAD

Sin cuentas, sin publicidad, sin analítica, sin permisos raros. La app solo pide
permiso para avisarte cuando termina un cronómetro.
```

## Novedades de esta versión (máx. 500 caracteres)

```
Primera versión. Recetario propio con fotos y favoritos, escalado de porciones con
reglas por ingrediente, conversor de medidas (incluida densidad por ingrediente,
horno y levadura), cronómetros múltiples con aviso y búsqueda por lo que tenés en casa.
```

## Seguridad de los datos (formulario de Play Console)

| Pregunta | Respuesta |
| --- | --- |
| ¿La app recopila o comparte datos de usuario? | **No** |
| ¿Los datos se cifran en tránsito? | No aplica (no hay tránsito de datos) |
| ¿Se pueden solicitar la eliminación de datos? | No aplica; se borran desinstalando la app |
| ¿La app usa una biblioteca de anuncios o analítica? | No |
| Permisos declarados | `POST_NOTIFICATIONS` y `VIBRATE`, solo para el aviso de los cronómetros |

Nota: la app permite el backup de Android (recetas y fotos). Eso lo maneja el sistema
operativo con la cuenta de Google del usuario, no la app, y se declara como tal si Play
lo pregunta. Los cronómetros quedan excluidos del backup a propósito.

## Clasificación de contenido

Cuestionario IARC: app de utilidad/estilo de vida, sin violencia, sin contenido sexual,
sin lenguaje ofensivo, sin apuestas, sin interacción entre usuarios, sin compartir
ubicación ni datos personales. Resultado esperado: apta para todo público.

## Gráficos

| Recurso | Requisito de Play | Archivo |
| --- | --- | --- |
| Ícono | 512 × 512 PNG, 32 bits, sin transparencia | `icono-512.png` |
| Gráfico destacado | 1024 × 500 PNG o JPG | `grafica-1024x500.png` |
| Capturas de teléfono | mínimo 2, entre 320 y 3840 px de lado | *pendientes: sacar del Xiaomi* |

Los dos primeros se regeneran con `generar_graficos.py` (mismo dibujo que el ícono de
la app, así la ficha y el launcher no se desincronizan).

Capturas sugeridas, en este orden: lista de recetas, detalle con el escalador en un
número distinto al original, conversor por ingrediente, cronómetros andando, y
"Con lo que tengo" con resultados.
