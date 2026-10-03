package pe.edu.esan.sportpro.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.esan.sportpro.data.model.Player

@Composable
fun PlayerProfileScreen(
    playerId: String,
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {

    val player by viewModel.selectedPlayer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(playerId) {
        viewModel.loadPlayerById(playerId)
    }

    when {

        isLoading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = errorMessage ?: "Ocurrió un error",
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = onBack
                    ) {
                        Text("Volver")
                    }
                }
            }
        }

        player == null -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró el jugador")
            }
        }

        else -> {

            val currentPlayer = player!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Button(
                    onClick = onBack
                ) {
                    Text("← Volver")
                }

                Text(
                    text = currentPlayer.fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "#${currentPlayer.number} · ${
                        getPlayerPositionName(currentPlayer.position)
                    }",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Información deportiva",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        ProfileRow(
                            label = "Posición",
                            value = getPlayerPositionName(
                                currentPlayer.position
                            )
                        )

                        ProfileRow(
                            label = "Número",
                            value = currentPlayer.number.toString()
                        )

                        ProfileRow(
                            label = "Equipo",
                            value = currentPlayer.teamId.ifBlank {
                                "Sin equipo"
                            }
                        )

                        ProfileRow(
                            label = "Academia",
                            value = currentPlayer.academyId.ifBlank {
                                "Sin academia"
                            }
                        )

                        ProfileRow(
                            label = "Estado",
                            value = if (currentPlayer.active) {
                                "Activo"
                            } else {
                                "Inactivo"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value
        )
    }
}

private fun getPlayerPositionName(
    position: String
): String {

    return when (position) {

        Player.POSITION_GOALKEEPER ->
            "Arquero"

        Player.POSITION_DEFENDER ->
            "Defensa"

        Player.POSITION_MIDFIELDER ->
            "Mediocampista"

        Player.POSITION_FORWARD ->
            "Delantero"

        else ->
            "Sin posición"
    }
}