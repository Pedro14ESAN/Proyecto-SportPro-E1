package com.example.projectsportpro2026_2.ui.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.projectsportpro2026_2.datos.modelo.Notificacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// HU20: muestra en la barra del celular los avisos nuevos mientras la app está abierta
// o en segundo plano. Sin plan Blaze no hay FCM desde servidor; cuando se active,
// una Cloud Function podrá enviar el push y este monitor sigue sirviendo.
object MonitorNotificaciones {

    private const val CANAL = "sportpro_avisos"

    private var iniciado = false
    private var escucha: ListenerRegistration? = null
    private var uidActual: String? = null

    fun iniciar(contexto: Context) {
        if (iniciado) return
        iniciado = true

        val app = contexto.applicationContext
        crearCanal(app)

        // Cambia la escucha cada vez que alguien inicia o cierra sesión
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == uidActual) return@addAuthStateListener

            uidActual = uid
            escucha?.remove()
            escucha = null
            if (uid != null) escucha = escuchar(app, uid)
        }
    }

    fun tienePermiso(contexto: Context): Boolean =
        ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    private fun escuchar(contexto: Context, uid: String): ListenerRegistration =
        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .whereEqualTo("idDestinatario", uid)
            .whereEqualTo("mostradaEnSistema", false)
            .addSnapshotListener { instantanea, error ->
                if (error != null || instantanea == null) return@addSnapshotListener

                instantanea.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .forEach { cambio ->
                        val notificacion = cambio.document
                            .toObject(Notificacion::class.java)
                            .copy(id = cambio.document.id)

                        mostrar(contexto, notificacion)

                        // No se vuelve a mostrar en la barra, pero sigue en la pantalla de notificaciones
                        cambio.document.reference.update("mostradaEnSistema", true)
                    }
            }

    private fun crearCanal(contexto: Context) {
        val canal = NotificationChannel(
            CANAL,
            "Avisos de SportPro",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Convocatorias, entrenamientos y anuncios"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        contexto.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(canal)
    }

    @SuppressLint("MissingPermission")
    private fun mostrar(contexto: Context, n: Notificacion) {
        // Criterio HU20: sin permiso del sistema, la app sigue funcionando y el aviso queda en la app
        if (!tienePermiso(contexto)) return

        val abrirApp = contexto.packageManager
            .getLaunchIntentForPackage(contexto.packageName) ?: return

        val intencion = PendingIntent.getActivity(
            contexto,
            n.id.hashCode(),
            abrirApp,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Versión para la pantalla bloqueada: sin ningún detalle (criterio HU20)
        val versionPublica = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SportPro")
            .setContentText("Tienes un nuevo aviso")
            .build()

        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${n.tipo.emoji} ${n.titulo}")
            .setContentText(n.mensaje)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(versionPublica)
            .setContentIntent(intencion)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(n.id.hashCode(), notificacion)
    }
}