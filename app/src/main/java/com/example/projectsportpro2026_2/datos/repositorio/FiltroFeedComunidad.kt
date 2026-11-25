package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.Publicacion

/**
 * Filtro del feed "Para ti" de Comunidad (HU21 / RC-03 / HU24), compartido por la pantalla
 * Comunidad (RepositorioPublicaciones.listarFeed) y la vista previa del inicio.
 *
 * Se extrajo tal cual de listarFeed, sin cambiar su comportamiento:
 *  1. Una publicación repetida (por ejemplo, una pública de mi academia que llega por las dos
 *     consultas) queda una sola vez.
 *  2. Solo lo que el usuario puede ver según la visibilidad Equipo / Academia / Comunidad y
 *     sin contenido eliminado u oculto por moderación (puedeVerPublicacion).
 *  3. Sin publicaciones de usuarios bloqueados, en ambos sentidos (HU24).
 *  4. De la más reciente a la más antigua.
 */
fun filtrarFeedComunidad(
    publicaciones: List<Publicacion>,
    pertenencia: Pertenencia,
    bloqueados: Set<String>
): List<Publicacion> =
    publicaciones
        .distinctBy { it.id }
        .filter { puedeVerPublicacion(it, pertenencia) }
        .filter { it.idAutor !in bloqueados }
        .sortedByDescending { it.fechaCreacion }
