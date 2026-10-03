package pe.edu.esan.sportpro.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.esan.sportpro.data.model.Player
import androidx.compose.material3.TextButton

@Composable
fun CreatePlayerScreen(
    viewModel: PlayerViewModel,
    academyId: String,
    onBack: () -> Unit,
    onPlayerCreated: () -> Unit
) {

    var fullName by remember { mutableStateOf("") }
    var numberText by remember { mutableStateOf("") }
    var teamId by remember { mutableStateOf("") }

    var selectedPosition by remember {
        mutableStateOf(Player.POSITION_FORWARD)
    }

    var positionMenuExpanded by remember {
        mutableStateOf(false)
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("← Volver")
        }

        Text(
            text = "Registrar jugador",
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                validationError = null
            },
            label = {
                Text("Nombre completo")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = numberText,
            onValueChange = { value ->

                if (value.all { it.isDigit() }) {
                    numberText = value
                }

                validationError = null
            },
            label = {
                Text("Número de camiseta")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = teamId,
            onValueChange = {
                teamId = it
                validationError = null
            },
            label = {
                Text("ID del equipo")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "Posición",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(
            onClick = {
                positionMenuExpanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = getPositionDisplayName(selectedPosition)
            )
        }

        DropdownMenu(
            expanded = positionMenuExpanded,
            onDismissRequest = {
                positionMenuExpanded = false
            }
        ) {

            positionOptions.forEach { position ->

                DropdownMenuItem(
                    text = {
                        Text(
                            text = getPositionDisplayName(position)
                        )
                    },
                    onClick = {
                        selectedPosition = position
                        positionMenuExpanded = false
                    }
                )
            }
        }

        validationError?.let { message ->

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {

                val number = numberText.toLongOrNull()

                when {

                    fullName.isBlank() -> {
                        validationError =
                            "Ingresa el nombre del jugador"
                    }

                    number == null || number <= 0 -> {
                        validationError =
                            "Ingresa un número de camiseta válido"
                    }

                    teamId.isBlank() -> {
                        validationError =
                            "Ingresa el equipo del jugador"
                    }

                    academyId.isBlank() -> {
                        validationError =
                            "No se encontró la academia"
                    }

                    else -> {

                        val player = Player(
                            academyId = academyId,
                            teamId = teamId.trim(),
                            fullName = fullName.trim(),
                            position = selectedPosition,
                            number = number,
                            active = true
                        )

                        viewModel.createPlayer(
                            player = player,
                            onSuccess = {
                                onPlayerCreated()
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Guardar jugador")
        }
    }
}

private val positionOptions = listOf(
    Player.POSITION_GOALKEEPER,
    Player.POSITION_DEFENDER,
    Player.POSITION_MIDFIELDER,
    Player.POSITION_FORWARD
)

private fun getPositionDisplayName(
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