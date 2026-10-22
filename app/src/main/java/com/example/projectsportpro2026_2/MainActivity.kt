package com.example.projectsportpro2026_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.projectsportpro2026_2.ui.navegacion.NavegacionApp
import com.example.projectsportpro2026_2.ui.theme.ProjectSportPro2026_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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