#!/usr/bin/env python3
"""Genera los graficos de la ficha de Play Store a partir del mismo dibujo
que el icono de la app (app/src/main/res/drawable/ic_launcher_*.xml).

Salida (en docs/play/):
    icono-512.png       icono de la ficha, 512x512 sin transparencia
    grafica-1024x500.png  grafica destacada de la ficha

Necesita Pillow. Como en esta maquina no esta instalado a nivel sistema:

    python3 -m venv /tmp/venv-graficos
    /tmp/venv-graficos/bin/pip install pillow
    /tmp/venv-graficos/bin/python docs/play/generar_graficos.py
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

SALIDA = Path(__file__).resolve().parent

# Paleta: la misma del tema y del icono adaptativo.
FONDO_CLARO = (168, 72, 36)     # #A84824
FONDO_OSCURO = (110, 43, 20)    # #6E2B14
CREMA = (255, 243, 230)         # #FFF3E6 cuerpo de la olla
DORADO = (242, 191, 72)         # #F2BF48 tapa y asas
VAPOR = (255, 231, 201)         # #FFE7C9

SUPER = 4  # supermuestreo: se dibuja 4x y se reduce, para que quede con antialias

FUENTE_TITULO = "/usr/share/fonts/truetype/lato/Lato-Black.ttf"
FUENTE_TEXTO = "/usr/share/fonts/truetype/lato/Lato-Regular.ttf"


def degradado(ancho, alto):
    """Degradado diagonal claro -> oscuro, como el fondo del icono adaptativo."""
    fondo = Image.new("RGB", (ancho, alto))
    pixeles = fondo.load()
    for y in range(alto):
        for x in range(ancho):
            t = (x / max(ancho - 1, 1) + y / max(alto - 1, 1)) / 2
            pixeles[x, y] = tuple(
                round(FONDO_CLARO[i] + (FONDO_OSCURO[i] - FONDO_CLARO[i]) * t)
                for i in range(3)
            )
    return fondo


def bezier(p0, p1, p2, p3, pasos=48):
    puntos = []
    for i in range(pasos + 1):
        t = i / pasos
        u = 1 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        puntos.append((x, y))
    return puntos


def trazo(lienzo, puntos, grosor, color):
    """Traza una curva estampando circulos: da puntas redondas y sin costuras."""
    radio = grosor / 2
    for x, y in puntos:
        lienzo.ellipse([x - radio, y - radio, x + radio, y + radio], fill=color)


def dibujar_olla(lienzo, centro_x, centro_y, alto_util):
    """Dibuja la olla con la misma geometria que ic_launcher_foreground.xml
    (viewport 108). `alto_util` es cuantos pixeles ocupa la zona visible del
    icono adaptativo, o sea 72 unidades del viewport."""
    escala = alto_util / 72.0

    def px(ux, uy):
        return (centro_x + (ux - 54) * escala, centro_y + (uy - 54) * escala)

    def rect(x0, y0, x1, y1, radio, color, esquinas=None):
        a, b = px(x0, y0)
        c, d = px(x1, y1)
        lienzo.rounded_rectangle(
            [a, b, c, d], radius=radio * escala, fill=color, corners=esquinas
        )

    # Vapor: el del medio sale por encima de la perilla, los otros dos a los costados
    for curva in (
        ((42, 40), (38, 36), (46, 33), (42, 29)),
        ((54, 32), (50, 28), (58, 26), (54, 23)),
        ((66, 40), (62, 36), (70, 33), (66, 29)),
    ):
        puntos = [px(*p) for p in bezier(*curva, pasos=160)]
        trazo(lienzo, puntos, 3 * escala, VAPOR)

    # Asas, redondeadas solo del lado de afuera
    rect(25, 54, 32, 62, 4, DORADO, esquinas=(True, False, False, True))
    rect(76, 54, 83, 62, 4, DORADO, esquinas=(False, True, True, False))

    # Cuerpo de la olla: tronco de cono con la base redondeada
    lienzo.polygon(
        [
            px(30, 51), px(78, 51), px(74, 79),
            px(72.5, 82.4), px(69.4, 84), px(68, 84),
            px(40, 84), px(38.6, 84), px(35.5, 82.4), px(34, 79),
        ],
        fill=CREMA,
    )

    # Tapa y perilla
    rect(26, 44, 82, 51, 3.5, DORADO)
    rect(49.5, 33.5, 58.5, 42.5, 4.5, DORADO)


def icono(lado=512):
    imagen = degradado(lado * SUPER, lado * SUPER)
    lienzo = ImageDraw.Draw(imagen)
    # El icono de la ficha muestra lo mismo que se ve enmascarado en el launcher.
    dibujar_olla(lienzo, lado * SUPER / 2, lado * SUPER / 2, lado * SUPER)
    return imagen.resize((lado, lado), Image.LANCZOS)


def grafica(ancho=1024, alto=500):
    """Grafica destacada: la olla a la izquierda y el nombre a la derecha.
    Play recorta los bordes en algunas ubicaciones, asi que todo el contenido
    queda holgado adentro."""
    imagen = degradado(ancho * SUPER, alto * SUPER)
    lienzo = ImageDraw.Draw(imagen)
    dibujar_olla(lienzo, 205 * SUPER, 250 * SUPER, 330 * SUPER)

    titulo = ImageFont.truetype(FUENTE_TITULO, 88 * SUPER)
    bajada = ImageFont.truetype(FUENTE_TEXTO, 38 * SUPER)
    lineas = (
        ((400, 118), "Recetario", titulo, CREMA),
        ((400, 212), "Criollo", titulo, DORADO),
        ((404, 330), "Tu cuaderno de recetas,", bajada, VAPOR),
        ((404, 378), "siempre a mano en la cocina", bajada, VAPOR),
    )
    for (x, y), texto, fuente, color in lineas:
        lienzo.text((x * SUPER, y * SUPER), texto, font=fuente, fill=color)
        derecha = lienzo.textbbox((x * SUPER, y * SUPER), texto, font=fuente)[2] / SUPER
        if derecha > ancho - 40:
            raise SystemExit(f"«{texto}» se pasa del margen: llega a {derecha:.0f}px")

    return imagen.resize((ancho, alto), Image.LANCZOS)


if __name__ == "__main__":
    icono().save(SALIDA / "icono-512.png")
    grafica().save(SALIDA / "grafica-1024x500.png")
    print("listo:", SALIDA / "icono-512.png", "y", SALIDA / "grafica-1024x500.png")
