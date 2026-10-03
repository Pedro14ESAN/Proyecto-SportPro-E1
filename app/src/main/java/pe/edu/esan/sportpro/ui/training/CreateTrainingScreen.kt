package pe.edu.esan.sportpro.ui.training

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.esan.sportpro.data.model.Exercise
import java.util.Calendar
import java.util.Locale

private val TrainingDark = Color(0xFF0F2C3A)
private val TrainingBackground = Color(0xFFF8F9FA)

@Composable
fun CreateTrainingScreen(
    availableExercises: List<Exercise>,
    isLoadingExercises: Boolean,
    exerciseError: String?,
    onBack: () -> Unit,
    onTrainingCreated: (
        team: String,
        date: String,
        time: String,
        objective: String,
        durationMinutes: Long,
        exerciseIds: List<String>
    ) -> Unit
) {

    val context = LocalContext.current

    var team by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var objective by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var showExerciseDialog by remember { mutableStateOf(false) }

    val selectedExercises = remember {
        mutableStateListOf<Exercise>()
    }

    // =========================================================
    // CALENDARIO
    // =========================================================

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->

            date = String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                dayOfMonth,
                month + 1,
                year
            )

            showError = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // =========================================================
    // SELECTOR DE HORA
    // =========================================================

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->

            time = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hourOfDay,
                minute
            )

            showError = false
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrainingBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(18.dp)
        ) {

            // =====================================================
            // CABECERA
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "← Volver",
                        color = TrainingDark
                    )
                }

                Text(
                    text = "SPORTPRO",
                    fontWeight = FontWeight.Bold,
                    color = TrainingDark,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Crear entrenamiento",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TrainingDark
            )

            Text(
                text = "Planifica una nueva sesión para tu equipo",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =====================================================
            // EQUIPO
            // =====================================================

            OutlinedTextField(
                value = team,
                onValueChange = {
                    team = it
                    showError = false
                },
                label = {
                    Text("Equipo *")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =====================================================
            // FECHA
            // =====================================================

            Text(
                text = "Fecha *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrainingDark
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedButton(
                onClick = {
                    datePickerDialog.show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text =
                        if (date.isBlank())
                            "📅 Seleccionar fecha"
                        else
                            "📅 $date",
                    color = TrainingDark
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =====================================================
            // HORA
            // =====================================================

            Text(
                text = "Hora *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrainingDark
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedButton(
                onClick = {
                    timePickerDialog.show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text =
                        if (time.isBlank())
                            "🕒 Seleccionar hora"
                        else
                            "🕒 $time",
                    color = TrainingDark
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =====================================================
            // OBJETIVO
            // =====================================================

            OutlinedTextField(
                value = objective,
                onValueChange = {
                    objective = it
                    showError = false
                },
                label = {
                    Text("Objetivo de la sesión *")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // =====================================================
            // DURACIÓN TOTAL
            // =====================================================

            OutlinedTextField(
                value = duration,
                onValueChange = { newValue ->

                    if (newValue.all { it.isDigit() }) {
                        duration = newValue
                        showError = false
                    }
                },
                label = {
                    Text("Duración total (min) *")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =====================================================
            // EJERCICIOS
            // =====================================================

            Text(
                text = "Ejercicios",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TrainingDark
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (selectedExercises.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F2F4)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text = "Todavía no agregaste ejercicios",
                        color = Color.Gray,
                        modifier = Modifier.padding(18.dp)
                    )
                }

            } else {

                selectedExercises.forEachIndexed { index, exercise ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F2F4)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "${index + 1}. ${exercise.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = TrainingDark
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "${exercise.durationMinutes} min • ${exercise.objective}",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }

                            TextButton(
                                onClick = {
                                    selectedExercises.remove(exercise)
                                }
                            ) {

                                Text(
                                    text = "Quitar",
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = {
                    showExerciseDialog = true
                },
                enabled = !isLoadingExercises,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                if (isLoadingExercises) {

                    Text(
                        text = "Cargando ejercicios..."
                    )

                } else {

                    Text(
                        text = "+ Agregar ejercicio",
                        color = TrainingDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (exerciseError != null) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = exerciseError,
                    color =
                        MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =====================================================
            // DURACIÓN DE EJERCICIOS
            // =====================================================

            val totalExerciseMinutes =
                selectedExercises.sumOf {
                    it.durationMinutes
                }

            Text(
                text = "Duración ejercicios: $totalExerciseMinutes min",
                color = Color.Gray
            )

            val trainingDuration =
                duration.toLongOrNull() ?: 0

            if (
                trainingDuration > 0 &&
                totalExerciseMinutes > trainingDuration
            ) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "La duración de los ejercicios supera la duración total del entrenamiento.",
                    color =
                        MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =====================================================
            // ERROR
            // =====================================================

            if (showError) {

                Text(
                    text = "Completa los campos obligatorios y agrega al menos un ejercicio.",
                    color =
                        MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

            // =====================================================
            // GUARDAR
            // =====================================================

            Button(
                onClick = {

                    val durationNumber =
                        duration.toLongOrNull()

                    if (
                        team.isBlank() ||
                        date.isBlank() ||
                        time.isBlank() ||
                        objective.isBlank() ||
                        durationNumber == null ||
                        durationNumber <= 0 ||
                        selectedExercises.isEmpty()
                    ) {

                        showError = true

                    } else {

                        showError = false

                        onTrainingCreated(
                            team,
                            date,
                            time,
                            objective,
                            durationNumber,
                            selectedExercises.map {
                                it.id
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            TrainingDark
                    ),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "Guardar entrenamiento",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }

    // =========================================================
    // SELECTOR DE EJERCICIOS DE FIREBASE
    // =========================================================

    if (showExerciseDialog) {

        AlertDialog(
            onDismissRequest = {
                showExerciseDialog = false
            },

            title = {

                Text(
                    text = "Ejercicios de la biblioteca",
                    fontWeight = FontWeight.Bold,
                    color = TrainingDark
                )
            },

            text = {

                Column {

                    if (isLoadingExercises) {

                        CircularProgressIndicator()

                    } else if (
                        availableExercises.isEmpty()
                    ) {

                        Text(
                            text = "No hay ejercicios activos disponibles.",
                            color = Color.Gray
                        )

                    } else {

                        availableExercises
                            .filter {
                                it.active
                            }
                            .forEach { exercise ->

                                val alreadyAdded =
                                    selectedExercises.any {
                                        it.id == exercise.id
                                    }

                                TextButton(
                                    onClick = {

                                        if (!alreadyAdded) {

                                            selectedExercises.add(
                                                exercise
                                            )
                                        }

                                        showExerciseDialog =
                                            false
                                    },

                                    enabled =
                                        !alreadyAdded,

                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    ) {

                                        Text(
                                            text =
                                                exercise.name,
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                TrainingDark
                                        )

                                        Text(
                                            text =
                                                "${exercise.objective} • ${exercise.durationMinutes} min",
                                            fontSize =
                                                12.sp,
                                            color =
                                                Color.Gray
                                        )

                                        if (alreadyAdded) {

                                            Text(
                                                text =
                                                    "Ya agregado",
                                                fontSize =
                                                    11.sp,
                                                color =
                                                    Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showExerciseDialog = false
                    }
                ) {

                    Text(
                        text = "Cerrar",
                        color = TrainingDark
                    )
                }
            }
        )
    }
}