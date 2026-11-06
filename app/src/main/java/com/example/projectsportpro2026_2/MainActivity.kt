package com.example.projectsportpro2026_2

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.projectsportpro2026_2.datos.sincronizacion.IdentificadorDispositivo
import com.example.projectsportpro2026_2.datos.sincronizacion.RegistroRechazos
import com.example.projectsportpro2026_2.ui.navegacion.NavegacionApp
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HU16: id del dispositivo y eventos rechazados al resincronizar
        IdentificadorDispositivo.inicializar(applicationContext)
        RegistroRechazos.inicializar(applicationContext)

        // HU25/HU26: App Check protege la llamada a la IA (Firebase AI Logic).
        // Proveedor de depuración: el token aparece en Logcat ("DebugAppCheckProvider")
        // y se registra en Firebase → App Check → Administrar tokens de depuración.
        // Al publicar en Play Store se cambia por Play Integrity.
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        enableEdgeToEdge()
        setContent {
            ProjectSportPro2026_2Theme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    NavegacionApp()
                }
            }
        }
    }
}