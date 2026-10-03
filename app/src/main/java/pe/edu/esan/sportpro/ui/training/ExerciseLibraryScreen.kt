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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.esan.sportpro.data.model.Exercise


private val ExerciseDark = Color(0xFF0F2C3A)

private val ExerciseBackground = Color(0xFFF8F9FA)

// Gris suave para las tarjetas
private val ExerciseCardBackground = Color(0xFFF0F2F4)

// Gris un poco más claro para las burbujas internas
private val ExerciseBubbleBackground = Color(0xFFE4E8EB)

// Verde suave para ejercicios activos
private val ActiveBackground = Color(0xFFDFF3E4)
private val ActiveText = Color(0xFF287A3E)

// Gris para inactivos
private val InactiveBackground = Color(0xFFE1E3E5)
private val InactiveText = Color(0xFF666666)


@Composable
fun ExerciseLibraryScreen(
    exercises: List<Exercise>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onCreateExercise: () -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("Todos")
    }


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

        val matchesStatus =
            when (selectedFilter) {

                "Activos" ->
                    exercise.active

                "Inactivos" ->
                    !exercise.active

                else ->
                    true
            }

        matchesSearch && matchesStatus
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ExerciseBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 12.dp
                )
        ) {

            // =====================================================
            // CABECERA
            // =====================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBack
                ) {

                    Text(
                        text = "← Volver",
                        color = ExerciseDark
                    )
                }

                Text(
                    text = "SPORTPRO",
                    fontWeight = FontWeight.Bold,
                    color = ExerciseDark
                )
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Text(
                text = "Biblioteca de ejercicios",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ExerciseDark
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = "Ejercicios disponibles para tu academia",
                fontSize = 13.sp,
                color = Color.Gray
            )


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // =====================================================
            // BUSCADOR
            // =====================================================

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = {
                    Text("Buscar ejercicio")
                },
                placeholder = {
                    Text("Nombre u objetivo")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // =====================================================
            // FILTROS
            // =====================================================

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
                        },

                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    ExerciseDark,

                                selectedLabelColor =
                                    Color.White
                            )
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // =====================================================
            // CONTENIDO
            // =====================================================

            when {

                isLoading -> {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text = "Cargando ejercicios...",
                            color = Color.Gray
                        )
                    }
                }


                errorMessage != null -> {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    ExerciseCardBackground
                            ),

                        shape =
                            RoundedCornerShape(18.dp)
                    ) {

                        Text(
                            text = errorMessage,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,

                            modifier =
                                Modifier.padding(18.dp)
                        )
                    }
                }


                filteredExercises.isEmpty() -> {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    ExerciseCardBackground
                            ),

                        shape =
                            RoundedCornerShape(18.dp)
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(20.dp)
                        ) {

                            Text(
                                text =
                                    "No hay ejercicios disponibles",
                                fontWeight =
                                    FontWeight.Bold,
                                color = ExerciseDark
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(
                                text =
                                    "Crea un ejercicio para comenzar a llenar la biblioteca.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }


                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.weight(1f),

                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        items(
                            items =
                                filteredExercises,

                            key = { exercise ->
                                exercise.id
                            }
                        ) { exercise ->

                            ExerciseBubbleCard(
                                exercise = exercise
                            )
                        }


                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )
                        }
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // =====================================================
            // CREAR EJERCICIO
            // =====================================================

            Button(
                onClick = onCreateExercise,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ExerciseDark
                    ),

                shape =
                    RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "+ Crear ejercicio",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


// =============================================================
// TARJETA / BURBUJA DE EJERCICIO
// =============================================================

@Composable
private fun ExerciseBubbleCard(
    exercise: Exercise
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    ExerciseCardBackground
            ),

        shape =
            RoundedCornerShape(20.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            // Nombre + Estado

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = exercise.name,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ExerciseDark,

                    modifier =
                        Modifier.weight(1f)
                )


                Spacer(
                    modifier =
                        Modifier.padding(4.dp)
                )


                // Burbuja ACTIVO / INACTIVO

                Surface(
                    color =
                        if (exercise.active)
                            ActiveBackground
                        else
                            InactiveBackground,

                    shape =
                        RoundedCornerShape(50.dp)
                ) {

                    Text(
                        text =
                            if (exercise.active)
                                "ACTIVO"
                            else
                                "INACTIVO",

                        fontSize = 10.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            if (exercise.active)
                                ActiveText
                            else
                                InactiveText,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // Objetivo en burbuja

            Surface(
                color =
                    ExerciseBubbleBackground,

                shape =
                    RoundedCornerShape(50.dp)
            ) {

                Text(
                    text =
                        exercise.objective,

                    color = ExerciseDark,

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                )
            }


            if (
                exercise.description
                    .isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                Text(
                    text =
                        exercise.description,

                    color =
                        Color(0xFF5F6368),

                    fontSize = 13.sp,

                    lineHeight = 18.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            // Duración

            Surface(
                color =
                    Color.White,

                shape =
                    RoundedCornerShape(50.dp)
            ) {

                Text(
                    text =
                        "⏱ ${exercise.durationMinutes} min",

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color = ExerciseDark,

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                )
            }
        }
    }
}