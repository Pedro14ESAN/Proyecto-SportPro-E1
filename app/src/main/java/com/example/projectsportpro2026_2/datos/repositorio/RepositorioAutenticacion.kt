package com.example.projectsportpro2026_2.datos.repositorio

import com.example.projectsportpro2026_2.datos.modelo.EstadoCuenta
import com.example.projectsportpro2026_2.datos.modelo.PerfilUsuario
import com.example.projectsportpro2026_2.datos.modelo.PoliticaEdad
import com.example.projectsportpro2026_2.datos.modelo.ReglaEdad
import com.example.projectsportpro2026_2.datos.modelo.Rol
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeParseException

class RepositorioAutenticacion(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    bd: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuarios = bd.collection("usuarios")

    fun uidActual(): String? = auth.currentUser?.uid

    suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: Rol,
        fechaNacimiento: String
    ): Result<PerfilUsuario> {
        if (rol == Rol.ADM) {
            return Result.failure(
                IllegalArgumentException("El administrador no se puede registrar desde la app")
            )
        }

        var estado = EstadoCuenta.ACTIVA
        if (rol == Rol.JUG) {
            val fecha = try {
                LocalDate.parse(fechaNacimiento)
            } catch (e: DateTimeParseException) {
                return Result.failure(
                    IllegalArgumentException("La fecha de nacimiento no es válida (use aaaa-MM-dd)")
                )
            }
            if (fecha.isAfter(LocalDate.now())) {
                return Result.failure(
                    IllegalArgumentException("La fecha de nacimiento no puede ser futura")
                )
            }
            when (PoliticaEdad.reglaPara(PoliticaEdad.calcularEdad(fecha))) {
                ReglaEdad.SIN_CUENTA -> return Result.failure(
                    IllegalArgumentException(
                        "Los menores de 12 años no crean cuenta. " +
                                "Su ficha la registra la academia junto con su apoderado."
                    )
                )
                ReglaEdad.REQUIERE_AUTORIZACION -> estado = EstadoCuenta.PENDIENTE
                ReglaEdad.LIBRE -> Unit
            }
        }

        return try {
            val resultado = auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await()
            val usuario = resultado.user!!
            val perfil = PerfilUsuario(
                uid = usuario.uid,
                nombre = nombre.trim(),
                correo = correo.trim(),
                rol = rol,
                fechaNacimiento = if (rol == Rol.JUG) fechaNacimiento else "",
                estado = estado
            )
            try {
                usuarios.document(usuario.uid).set(perfil).await()
            } catch (e: Exception) {
                usuario.delete().await()   // evita dejar una cuenta sin perfil
                throw e
            }
            Result.success(perfil)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<PerfilUsuario> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo.trim(), contrasena).await()
            val perfil = obtenerPerfil(resultado.user!!.uid)
            if (perfil == null) {
                auth.signOut()
                Result.failure(IllegalStateException("Tu cuenta no tiene perfil. Contacta a tu academia."))
            } else {
                Result.success(perfil)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPerfil(uid: String): PerfilUsuario? =
        usuarios.document(uid).get().await().toObject(PerfilUsuario::class.java)

    fun cerrarSesion() = auth.signOut()
}