package pe.edu.esan.sportpro.ui.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

private val ExercisePrimaryDark = Color(0xFF0F2C3A)
private val ExerciseAccentGreen = Color(0xFF8CE093)
private val ExerciseBackground = Color(0xFFF8F9FA)

private data class ExerciseUi(
    val name: String,
    val objective: String,
    val description: String,
    val durationMinutes: Int,
    val active: Boolean
)

@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    onCreateExercise: () -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("Todos")
    }

    val exercises = listOf(

        ExerciseUi(
            name = "Rondo 4v2",
            objective = "Posesión y presión",
            description = "Mantener la posesión en espacio reducido.",
            durationMinutes = 15,
            active = true
        ),

        ExerciseUi(
            name = "Conducción en slalom",
            objective = "Control de balón",
            description = "Conducción entre conos utilizando ambos perfiles.",
            durationMinutes = 10,
            active = true
        ),

        ExerciseUi(
            name = "Pase en triángulo",
            objective = "Precisión de pase",
            description = "Ejercicio de pase y movimiento.",
            durationMinutes = 12,
            active = true
        ),

        ExerciseUi(
            name = "Circuito físico",
            objective = "Resistencia",
            description = "Circuito de trabajo físico general.",
            durationMinutes = 20,
            active = false
        )
    )

    val filteredExercises = exercises.filter { exercise ->

        val matchesSearch =
            exercise.name.contains(
                searchText,
                ignoreCase = true
            ) ||
                    exercise.objective.contains(
                        searchText,
                        ignoreCase = true
                    )

        val matchesFilter = when (selectedFilter) {

            "Activos" ->
                exercise.active

            "Inactivos" ->
                !exercise.active

            else ->
                true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(

        floatingActionButton = {

            FloatingActionButton(
                onClick = onCreateExercise,
                containerColor = ExerciseAccentGreen
            ) {

                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExercisePrimaryDark
                )
            }
        }

    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = ExerciseBackground
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    TextButton(
                        onClick = onBack
                    ) {

                        Text(
                            text = "← Volver",
                            color = ExercisePrimaryDark
                        )
                    }

                    Text(
                        text = "SPORTPRO",
                        fontWeight = FontWeight.Bold,
                        color = ExercisePrimaryDark,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Biblioteca de ejercicios",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExercisePrimaryDark
                )

                Text(
                    text = "Ejercicios reutilizables para tus entrenamientos",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = {
                        Text("Buscar por nombre u objetivo")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        "Todos",
                        "Activos",
                        "Inactivos"
                    ).forEach { filter ->

                        FilterChip(
                            selected =
                                selectedFilter == filter,

                            onClick = {
                                selectedFilter = filter
                            },

                            label = {
                                Text(filter)
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(filteredExercises) { exercise ->

                        ExerciseCard(
                            exercise = exercise
                        )
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(80.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseUi
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ExercisePrimaryDark,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text =
                        if (exercise.active)
                            "ACTIVO"
                        else
                            "INACTIVO",

                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,

                    color =
                        if (exercise.active)
                            Color(0xFF388E3C)
                        else
                            Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = exercise.objective,
                color = ExercisePrimaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = exercise.description,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${exercise.durationMinutes} min",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}