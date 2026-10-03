package pe.edu.esan.sportpro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryDark = Color(0xFF0F2C3A)
val AccentGreen = Color(0xFF8CE093)
val LightBackground = Color(0xFFF8F9FA)

@Composable
fun HomeScreen(
    userName: String,
    role: String,
    onNavigateToTeams: () -> Unit,
    onNavigateToPlayers: () -> Unit,
    onNavigateToTrainings: () -> Unit,
    onLogout: () -> Unit
) {

    val roleName = when (role) {
        "ADM" -> "Administrador"
        "DT" -> "Director Técnico"
        "JUG" -> "Jugador"
        "PAD" -> "Padre / Apoderado"
        else -> "Usuario"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                PrimaryDark,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚽",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = "SPORTPRO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryDark
                    )
                }

                TextButton(
                    onClick = onLogout
                ) {
                    Text(
                        text = "Cerrar Sesión",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Usuario
            Text(
                text = "Bienvenido,",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Rol real del usuario
            AssistChip(
                onClick = {},
                label = {
                    Text(roleName)
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Tarjeta resumen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryDark
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Panel de Control",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = roleName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "SportPro · Temporada 2026",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Acciones rápidas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    onClick = onNavigateToTrainings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Gestión de Entrenamientos",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onNavigateToPlayers,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Gestión de Jugadores",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onNavigateToTeams,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Gestión de Equipos",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}