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
import androidx.compose.material3.OutlinedButton
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
import pe.edu.esan.sportpro.data.model.Training
import java.text.SimpleDateFormat
import java.util.Locale

private val TrainingDark = Color(0xFF0F2C3A)
private val TrainingBackground = Color(0xFFF8F9FA)

private val TrainingCardBackground = Color(0xFFF0F2F4)
private val TrainingBubbleBackground = Color(0xFFE4E8EB)

private val ScheduledBackground = Color(0xFFE1F1FF)
private val ScheduledText = Color(0xFF246A96)

private val CancelledBackground = Color(0xFFFFE3E3)
private val CancelledText = Color(0xFFB33A3A)

private val FinishedBackground = Color(0xFFDFF3E4)
private val FinishedText = Color(0xFF287A3E)

@Composable
fun TrainingScreen(
    trainings: List<Training>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onCreateTraining: () -> Unit
) {

    var selectedFilter by remember {
        mutableStateOf("Todos")
    }

    val filteredTrainings = trainings.filter { training ->

        when (selectedFilter) {

            "Programados" ->
                training.status == Training.STATUS_SCHEDULED

            "Cancelados" ->
                training.status == Training.STATUS_CANCELLED

            else ->
                true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrainingBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 18.dp,
                    vertical = 12.dp
                )
        ) {

            // CABECERA

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    color = TrainingDark
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Entrenamientos",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TrainingDark
            )

            Text(
                text = "Sesiones creadas por el Director Técnico",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // BIBLIOTECA

            OutlinedButton(
                onClick = onNavigateToExerciseLibrary,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "Biblioteca de ejercicios",
                    color = TrainingDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // FILTROS

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "Todos",
                    "Programados",
                    "Cancelados"
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
                                    TrainingDark,

                                selectedLabelColor =
                                    Color.White
                            )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // CONTENIDO

            when {

                isLoading -> {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                "Cargando entrenamientos...",
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
                                    TrainingCardBackground
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

                filteredTrainings.isEmpty() -> {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    TrainingCardBackground
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
                                    "No tienes entrenamientos creados",
                                fontWeight =
                                    FontWeight.Bold,
                                color = TrainingDark
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "Crea una nueva sesión de entrenamiento para comenzar.",
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
                            items = filteredTrainings,
                            key = { training ->
                                training.id
                            }
                        ) { training ->

                            TrainingCard(
                                training = training
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

            // CREAR ENTRENAMIENTO

            Button(
                onClick = onCreateTraining,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            TrainingDark
                    ),

                shape =
                    RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "+ Crear entrenamiento",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun TrainingCard(
    training: Training
) {

    val formattedDate = training.date?.let {

        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(it.toDate())

    } ?: "Sin fecha"


    val statusText =
        when (training.status) {

            Training.STATUS_SCHEDULED ->
                "PROGRAMADO"

            Training.STATUS_IN_PROGRESS ->
                "EN CURSO"

            Training.STATUS_FINISHED ->
                "FINALIZADO"

            Training.STATUS_CANCELLED ->
                "CANCELADO"

            else ->
                training.status
        }


    val statusBackground =
        when (training.status) {

            Training.STATUS_CANCELLED ->
                CancelledBackground

            Training.STATUS_FINISHED ->
                FinishedBackground

            else ->
                ScheduledBackground
        }


    val statusColor =
        when (training.status) {

            Training.STATUS_CANCELLED ->
                CancelledText

            Training.STATUS_FINISHED ->
                FinishedText

            else ->
                ScheduledText
        }


    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    TrainingCardBackground
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

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        if (training.title.isNotBlank())
                            training.title
                        else
                            "Entrenamiento",

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        TrainingDark,

                    modifier =
                        Modifier.weight(1f)
                )


                Surface(
                    color =
                        statusBackground,

                    shape =
                        RoundedCornerShape(50.dp)
                ) {

                    Text(
                        text =
                            statusText,

                        fontSize = 10.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            statusColor,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                    )
                }
            }


            if (training.objective.isNotBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                Surface(
                    color =
                        TrainingBubbleBackground,

                    shape =
                        RoundedCornerShape(50.dp)
                ) {

                    Text(
                        text =
                            training.objective,

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            TrainingDark,

                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Surface(
                    color = Color.White,
                    shape =
                        RoundedCornerShape(50.dp)
                ) {

                    Text(
                        text =
                            "📅 $formattedDate",

                        fontSize = 12.sp,

                        color =
                            TrainingDark,

                        modifier =
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 6.dp
                            )
                    )
                }


                if (
                    training.startTime
                        .isNotBlank()
                ) {

                    Surface(
                        color = Color.White,
                        shape =
                            RoundedCornerShape(50.dp)
                    ) {

                        Text(
                            text =
                                "⏰ ${training.startTime}",

                            fontSize = 12.sp,

                            color =
                                TrainingDark,

                            modifier =
                                Modifier.padding(
                                    horizontal = 11.dp,
                                    vertical = 6.dp
                                )
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Surface(
                color = Color.White,
                shape =
                    RoundedCornerShape(50.dp)
            ) {

                Text(
                    text =
                        "⏱ ${training.durationMinutes} min",

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        TrainingDark,

                    modifier =
                        Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 6.dp
                        )
                )
            }


            if (
                training.location
                    .isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text =
                        "Lugar: ${training.location}",

                    color =
                        Color(0xFF5F6368),

                    fontSize = 13.sp
                )
            }
        }
    }
}