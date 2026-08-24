# Reglas de R8 para el build de release (minify + shrinkResources activados).
#
# La app no usa reflexion propia: lo unico que necesita ayuda es kotlinx.serialization,
# que genera serializadores que R8 no ve referenciados desde el codigo.

# --- kotlinx.serialization -------------------------------------------------
# Los cronometros se guardan como JSON en SharedPreferences.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class uy.horacio.recetariocriollo.**$$serializer {
    *;
}

# --- Room ------------------------------------------------------------------
# Room genera las implementaciones en tiempo de compilacion; solo hay que
# conservar las entidades y sus constructores, que se instancian desde el
# codigo generado.
-keep class uy.horacio.recetariocriollo.datos.*Entity { *; }
-keep class uy.horacio.recetariocriollo.datos.IngredienteDeRecetaConCatalogo { *; }
-keepclassmembers class uy.horacio.recetariocriollo.datos.** {
    <init>(...);
}

# --- Enums usados en la base y en el estado de UI ---------------------------
# valueOf(String) se usa al leer de Room y al restaurar cronometros.
-keepclassmembers enum uy.horacio.recetariocriollo.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Utilidad ---------------------------------------------------------------
# Deja los nombres de archivo/linea en los stacktraces (con el mapping.txt al lado).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
