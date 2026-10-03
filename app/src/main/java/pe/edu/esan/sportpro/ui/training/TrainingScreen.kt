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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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

private val PrimaryDark = Color(0xFF0F2C3A)
private val AccentGreen = Color(0xFF8CE093)
private val LightBackground = Color(0xFFF8F9FA)

private data class TrainingUi(
    val title: String,
    val team: String,
    val date: String,
    val time: String,
    val duration: String,
    val status: String
)

@Composable
fun TrainingScreen(
    onBack: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onCreateTraining: () -> Unit
) {

    var selectedFilter by remember {
        mutableStateOf("Todos")
    }

    val trainings = listOf(
        TrainingUi(
            title = "Mejora de posesión y presión alta",
            team = "SportPro Norte A",
            date = "09/09/2026",
            time = "18:30",
            duration = "100 min",
            status = "PROGRAMADO"
        ),
        TrainingUi(
            title = "Definición y contragolpe",
            team = "SportPro Norte A",
            date = "12/09/2026",
            time = "17:30",
            duration = "90 min",
            status = "PROGRAMADO"
        ),
        TrainingUi(
            title = "Técnica individual base",
            team = "SportPro Norte A",
            date = "11/09/2026",
            time = "16:00",
            duration = "80 min",
            status = "CANCELADO"
        )
    )

    val filteredTrainings = when (selectedFilter) {

        "Programados" -> trainings.filter {
            it.status == "PROGRAMADO"
        }

        "Cancelados" -> trainings.filter {
            it.status == "CANCELADO"
        }

        else -> trainings
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTraining,
                containerColor = AccentGreen
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }
        }
    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = LightBackground
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                            color = PrimaryDark
                        )
                    }

                    Text(
                        text = "SPORTPRO",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Entrenamientos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )

                Text(
                    text = "Organiza y administra las sesiones de tu equipo",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = onNavigateToExerciseLibrary,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = "Biblioteca de ejercicios",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Sesiones de entrenamiento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        "Todos",
                        "Programados",
                        "Cancelados"
                    ).forEach { filter ->

                        FilterChip(
                            selected = selectedFilter == filter,
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
                    modifier = Modifier.height(16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(filteredTrainings) { training ->

                        TrainingCard(
                            training = training
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
private fun TrainingCard(
    training: TrainingUi
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = training.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryDark,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = training.status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (training.status == "CANCELADO") {
                        Color.Red
                    } else {
                        Color(0xFF388E3C)
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = training.team,
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${training.date} • ${training.time}",
                fontSize = 13.sp,
                color = PrimaryDark
            )

            Text(
                text = training.duration,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}