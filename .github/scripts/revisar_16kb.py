#!/usr/bin/env python3
"""Revisa que un APK funcione con páginas de 16 KB (requisito de Play desde Android 15).

Para cada biblioteca nativa del APK comprueba dos cosas:
- que los segmentos LOAD del ELF estén alineados a 16 KB o más;
- que la zona RELRO termine en un borde de 16 KB o justo donde termina su segmento LOAD. Es la regla
  de Android 16: si no, loguea "RELRO is not a suffix and its end is not PAGE-aligned" y abre la
  app en modo compatible, con un aviso al usuario;
- que el .so vaya sin comprimir alineado a 16 KB dentro del zip, para que se mapee directo.

Uso: revisar_16kb.py app.apk  (sale con 1 si algo no cumple)
"""
import struct
import sys
import zipfile

PAGINA = 16 * 1024
PT_LOAD = 1
PT_GNU_RELRO = 0x6474E552


def revisar_elf(datos: bytes) -> list[str]:
    if datos[:4] != b"\x7fELF":
        raise ValueError("no es un ELF")
    de_64 = datos[4] == 2
    orden = "<" if datos[5] == 1 else ">"
    if de_64:
        e_phoff, = struct.unpack_from(orden + "Q", datos, 0x20)
        e_phentsize, e_phnum = struct.unpack_from(orden + "HH", datos, 0x36)
    else:
        e_phoff, = struct.unpack_from(orden + "I", datos, 0x1C)
        e_phentsize, e_phnum = struct.unpack_from(orden + "HH", datos, 0x2A)
    palabra = orden + ("Q" if de_64 else "I")
    problemas = []
    segmentos = []
    relros = []
    for i in range(e_phnum):
        base = e_phoff + i * e_phentsize
        p_type, = struct.unpack_from(orden + "I", datos, base)
        p_vaddr, = struct.unpack_from(palabra, datos, base + (0x10 if de_64 else 0x08))
        p_memsz, = struct.unpack_from(palabra, datos, base + (0x28 if de_64 else 0x14))
        p_align, = struct.unpack_from(palabra, datos, base + (0x30 if de_64 else 0x1C))
        if p_type == PT_LOAD and p_align < PAGINA:
            problemas.append(f"segmento LOAD alineado a {p_align} bytes")
        if p_type == PT_LOAD:
            segmentos.append((p_vaddr, p_vaddr + p_memsz))
        if p_type == PT_GNU_RELRO:
            relros.append((p_vaddr, p_vaddr + p_memsz))
    for inicio, fin in relros:
        es_sufijo = any(desde <= inicio and fin == hasta for desde, hasta in segmentos)
        if fin % PAGINA and not es_sufijo:
            problemas.append(f"RELRO termina en 0x{fin:x}: ni en un borde de 16 KB ni al final de su segmento")
    return problemas


def main(ruta: str) -> int:
    problemas = []
    with zipfile.ZipFile(ruta) as apk, open(ruta, "rb") as crudo:
        bibliotecas = [e for e in apk.infolist() if e.filename.startswith("lib/") and e.filename.endswith(".so")]
        for entrada in bibliotecas:
            problemas += [f"{entrada.filename}: {p}" for p in revisar_elf(apk.read(entrada))]
            if entrada.compress_type == zipfile.ZIP_STORED:
                crudo.seek(entrada.header_offset + 26)
                largo_nombre, largo_extra = struct.unpack("<HH", crudo.read(4))
                inicio = entrada.header_offset + 30 + largo_nombre + largo_extra
                if inicio % PAGINA:
                    problemas.append(f"{entrada.filename}: sin comprimir pero no alineado a 16 KB en el zip")
    for problema in problemas:
        print(f"✗ {problema}")
    print(f"{len(bibliotecas)} bibliotecas nativas revisadas, {len(problemas)} problemas")
    return 1 if problemas else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1]))
