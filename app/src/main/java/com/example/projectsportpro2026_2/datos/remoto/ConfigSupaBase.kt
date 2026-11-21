package com.example.projectsportpro2026_2.datos.remoto

// Supabase se usa SOLO como almacén de imágenes (Storage).
// La base de datos de la app sigue siendo Firestore.
object ConfigSupabase {

    const val URL = "https://xmhilrlpzeldtscfgeue.supabase.co"

    // Clave PUBLICABLE: está diseñada para ir dentro de la app.
    // Solo permite lo que dice la política del bucket (subir a publicaciones/ y jugadores/).
    // NUNCA poner aquí la clave secret ni service_role.
    const val CLAVE_PUBLICA = "sb_publishable_Q5smYl58fdxtAouwR7fd2Q_VZl4dwZZ"

    const val BUCKET = "imagenes"

    // Carpetas permitidas por la política del bucket
    const val CARPETA_PUBLICACIONES = "publicaciones"
    const val CARPETA_JUGADORES = "jugadores"

    // Dirección pública con la que cualquiera ve una imagen ya subida
    fun urlPublica(ruta: String): String =
        "$URL/storage/v1/object/public/$BUCKET/$ruta"
}