package pe.edu.esan.sportpro.ui.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ExerciseDark = Color(0xFF0F2C3A)
private val ExerciseBackground = Color(0xFFF8F9FA)

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

    var name by remember {
        mutableStateOf("")
    }

    var objective by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var duration by remember {
        mutableStateOf("")
    }

    var showError by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ExerciseBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(18.dp)
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← Volver",
                    color = ExerciseDark
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Crear ejercicio",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ExerciseDark
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Agrega un nuevo ejercicio a la biblioteca",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // NOMBRE

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                    showError = false
                },

                label = {
                    Text("Nombre *")
                },

                placeholder = {
                    Text("Ej. Rondo 4v2")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                shape =
                    RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // OBJETIVO

            OutlinedTextField(
                value = objective,

                onValueChange = {
                    objective = it
                    showError = false
                },

                label = {
                    Text("Objetivo *")
                },

                placeholder = {
                    Text("Ej. Posesión y presión")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                shape =
                    RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // DESCRIPCIÓN

            OutlinedTextField(
                value = description,

                onValueChange = {
                    description = it
                },

                label = {
                    Text("Descripción")
                },

                placeholder = {
                    Text(
                        "Describe brevemente el ejercicio"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                minLines = 4,

                shape =
                    RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // DURACIÓN

            OutlinedTextField(
                value = duration,

                onValueChange = { newValue ->

                    if (
                        newValue.all {
                            it.isDigit()
                        }
                    ) {

                        duration =
                            newValue

                        showError =
                            false
                    }
                },

                label = {
                    Text(
                        "Duración estimada (min) *"
                    )
                },

                placeholder = {
                    Text("Ej. 15")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                shape =
                    RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            if (showError) {

                Text(
                    text =
                        "Completa nombre, objetivo y una duración mayor a 0.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,

                    fontSize = 13.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }

            Button(
                onClick = {

                    val durationNumber =
                        duration.toLongOrNull()

                    if (
                        name.isBlank() ||
                        objective.isBlank() ||
                        durationNumber == null ||
                        durationNumber <= 0
                    ) {

                        showError =
                            true

                    } else {

                        showError =
                            false

                        onExerciseCreated(
                            name.trim(),
                            objective.trim(),
                            description.trim(),
                            durationNumber
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ExerciseDark
                    ),

                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "Crear ejercicio",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )
        }
    }
}
