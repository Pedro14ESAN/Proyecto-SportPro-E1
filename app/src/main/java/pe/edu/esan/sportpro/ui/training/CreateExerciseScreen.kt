package pe.edu.esan.sportpro.ui.training

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CreateExerciseDark = Color(0xFF0F2C3A)
private val CreateExerciseGreen = Color(0xFF8CE093)
private val CreateExerciseBackground = Color(0xFFF8F9FA)

@Composable
fun CreateExerciseScreen(
    onBack: () -> Unit,
    onExerciseCreated: (
        name: String,
        objective: String,
        description: String,
        durationMinutes: Long
    ) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var objective by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var objectiveError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CreateExerciseBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "← Volver",
                        color = CreateExerciseDark
                    )
                }

                Text(
                    text = "SPORTPRO",
                    fontWeight = FontWeight.Bold,
                    color = CreateExerciseDark,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Crear ejercicio",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = CreateExerciseDark
            )

            Text(
                text = "Agrega un ejercicio a la biblioteca",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = {
                    Text("Nombre *")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("El nombre es obligatorio")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = objective,
                onValueChange = {
                    objective = it
                    objectiveError = false
                },
                label = {
                    Text("Objetivo *")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = objectiveError,
                supportingText = {
                    if (objectiveError) {
                        Text("El objetivo es obligatorio")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = {
                    Text("Descripción")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { newValue ->

                    if (newValue.all { it.isDigit() }) {
                        duration = newValue
                        durationError = false
                    }
                },
                label = {
                    Text("Duración estimada (min) *")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = durationError,
                supportingText = {
                    if (durationError) {
                        Text("La duración debe ser mayor a 0")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Button(
                onClick = {

                    nameError = name.isBlank()
                    objectiveError = objective.isBlank()

                    val durationNumber =
                        duration.toLongOrNull()

                    durationError =
                        durationNumber == null ||
                                durationNumber <= 0

                    if (
                        !nameError &&
                        !objectiveError &&
                        !durationError
                    ) {

                        onExerciseCreated(
                            name.trim(),
                            objective.trim(),
                            description.trim(),
                            durationNumber!!
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CreateExerciseDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "Crear ejercicio",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
