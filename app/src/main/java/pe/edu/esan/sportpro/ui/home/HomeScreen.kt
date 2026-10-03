package pe.edu.esan.sportpro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryDark = Color(0xFF0F2C3A)      // Azul oscuro
val AccentGreen = Color(0xFF8CE093)      // Verde menta
val LightBackground = Color(0xFFF8F9FA)  // Fondo gris claro

@Composable
fun HomeScreen(
    onNavigateToTeams: () -> Unit,
    onNavigateToPlayers: () -> Unit,
    onNavigateToTrainings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedRole by remember { mutableStateOf("DT") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Encabezado simple: Logo, Título y Salir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚽", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SPORTPRO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryDark
                    )
                }
                TextButton(onClick = onLogout) {
                    Text("Cerrar Sesión", color = Color.Red, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Saludo y Usuario
            Text("Buenos días,", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = "Piero Jair",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Rol dinámico
            Text("Rol seleccionado:", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("DT", "ADMIN", "JUGADOR").forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Tarjeta resumen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Panel de Control · $selectedRole",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Club Deportivo Norte",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Temporada 2026",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Botones de Acceso a las pantallas del grupo
            Text(
                text = "Acciones rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNavigateToTrainings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White // <-- Esto hace el texto visible en blanco
                    )
                ) {
                    Text("Gestión de Entrenamientos", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNavigateToPlayers,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White // <-- Texto blanco
                    )
                ) {
                    Text("Gestión de Jugadores", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onNavigateToTeams,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White // <-- Texto blanco
                    )
                ) {
                    Text("Gestión de Equipos", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}