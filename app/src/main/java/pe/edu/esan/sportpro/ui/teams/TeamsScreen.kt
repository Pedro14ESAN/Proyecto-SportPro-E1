package pe.edu.esan.sportpro.ui.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import pe.edu.esan.sportpro.data.model.Team
import pe.edu.esan.sportpro.data.model.TeamCategories
import pe.edu.esan.sportpro.data.model.User

/**
 * HU-03 · Punto de entrada de la pantalla de equipos.
 *
 * - Sin academia: el ADM puede crear una; los demás roles ven un aviso.
 * - Con academia: lista de equipos activos e inactivos, crear, editar y desactivar.
 *
 * onAcademyCreated recibe el id de la academia nueva, para que quien navega
 * actualice el usuario en memoria (ver AuthViewModel.updateAcademy).
 */
@Composable
fun TeamsScreen(
    academyId: String,
    role: String,
    onBack: () -> Unit,
    onAcademyCreated: (String) -> Unit
) {
    if (academyId.isBlank()) {
        NoAcademyScreen(
            role = role,
            onBack = onBack,
            onAcademyCreated = onAcademyCreated
        )
    } else {
        TeamsContent(
            academyId = academyId,
            role = role,
            onBack = onBack
        )
    }
}

// =====================================================================
// SIN ACADEMIA
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoAcademyScreen(
    role: String,
    onBack: () -> Unit,
    onAcademyCreated: (String) -> Unit,
    academyViewModel: AcademyViewModel = viewModel()
) {
    val state by academyViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val isAdmin = role == User.ROLE_ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isAdmin) {
                    "Aún no has creado tu academia. Crea una para empezar a registrar equipos."
                } else {
                    "Aún no perteneces a una academia. Pide al administrador que te asigne una."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (isAdmin) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        academyViewModel.clearError()
                        showDialog = true
                    }
                ) {
                    Text("Crear academia")
                }
            }
        }
    }

    if (showDialog) {
        CreateAcademyDialog(
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = {
                showDialog = false
                academyViewModel.clearError()
            },
            onSave = { name, description, address ->
                academyViewModel.createAcademy(name, description, address) { newAcademyId ->
                    showDialog = false
                    onAcademyCreated(newAcademyId)
                }
            }
        )
    }
}

@Composable
private fun CreateAcademyDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Crear academia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre de la academia") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (name.isBlank()) {
                        nameError = "El nombre de la academia es obligatorio"
                    } else {
                        onSave(name, description, address)
                    }
                }
            ) {
                Text(if (isSaving) "Guardando..." else "Crear")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =====================================================================
// CON ACADEMIA: LISTA DE EQUIPOS
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamsContent(
    academyId: String,
    role: String,
    onBack: () -> Unit,
    viewModel: TeamsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showForm by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf<Team?>(null) }
    var teamToDeactivate by remember { mutableStateOf<Team?>(null) }

    val canManage = role == User.ROLE_ADMIN || role == User.ROLE_COACH

    LaunchedEffect(academyId) { viewModel.load(academyId) }

    // Los errores salen en un Snackbar, salvo cuando el formulario está abierto:
    // ahí aparecen dentro del propio formulario.
    LaunchedEffect(state.errorMessage, showForm) {
        val message = state.errorMessage
        if (message != null && !showForm) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        },
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingTeam = null
                        viewModel.clearError()
                        showForm = true
                    }
                ) {
                    Text("+ Nuevo equipo")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("Equipos activos (${state.activeTeams.size})") }

                if (state.activeTeams.isEmpty()) {
                    item {
                        Text(
                            text = if (canManage) {
                                "Aún no hay equipos activos. Crea el primero con «+ Nuevo equipo»."
                            } else {
                                "Aún no hay equipos activos."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(state.activeTeams, key = { it.id }) { team ->
                    TeamCard(
                        team = team,
                        canManage = canManage,
                        onEdit = {
                            editingTeam = team
                            viewModel.clearError()
                            showForm = true
                        },
                        onToggleActive = { teamToDeactivate = team }
                    )
                }

                if (state.inactiveTeams.isNotEmpty()) {
                    item { SectionTitle("Equipos inactivos (${state.inactiveTeams.size})") }

                    items(state.inactiveTeams, key = { it.id }) { team ->
                        TeamCard(
                            team = team,
                            canManage = canManage,
                            onEdit = {
                                editingTeam = team
                                viewModel.clearError()
                                showForm = true
                            },
                            onToggleActive = { viewModel.activateTeam(team.id) }
                        )
                    }
                }

                // Espacio para que el botón flotante no tape la última tarjeta
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showForm) {
        TeamFormDialog(
            team = editingTeam,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = {
                showForm = false
                editingTeam = null
                viewModel.clearError()
            },
            onSave = { name, category, season ->
                val current = editingTeam
                if (current == null) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    viewModel.createTeam(academyId, name, category, uid, season) {
                        showForm = false
                    }
                } else {
                    viewModel.updateTeam(
                        current.copy(name = name, category = category, season = season)
                    ) {
                        showForm = false
                        editingTeam = null
                    }
                }
            }
        )
    }

    teamToDeactivate?.let { team ->
        AlertDialog(
            onDismissRequest = { teamToDeactivate = null },
            title = { Text("¿Desactivar equipo?") },
            text = {
                Text(
                    "«${team.name}» dejará de aparecer como activo, pero conservará su historial. " +
                            "Podrás reactivarlo cuando quieras."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deactivateTeam(team.id)
                        teamToDeactivate = null
                    }
                ) { Text("Desactivar") }
            },
            dismissButton = {
                TextButton(onClick = { teamToDeactivate = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun TeamCard(
    team: Team,
    canManage: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (team.active) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = team.name, style = MaterialTheme.typography.titleMedium)

            val details = listOf(team.category, team.season)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (details.isNotEmpty()) {
                Text(text = details, style = MaterialTheme.typography.bodyMedium)
            }
            if (!team.active) {
                Text(
                    text = "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (canManage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) { Text("Editar") }
                    TextButton(onClick = onToggleActive) {
                        Text(if (team.active) "Desactivar" else "Reactivar")
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamFormDialog(
    team: Team?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, season: String) -> Unit
) {
    var name by remember { mutableStateOf(team?.name ?: "") }
    var category by remember { mutableStateOf(team?.category ?: TeamCategories.defaults.first()) }
    var season by remember { mutableStateOf(team?.season ?: "2026") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (team == null) "Nuevo equipo" else "Editar equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre del equipo") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedButton(
                        onClick = { categoryExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $category")
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        TeamCategories.defaults.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Temporada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (name.isBlank()) {
                        nameError = "El nombre del equipo es obligatorio"
                    } else {
                        onSave(name, category, season.trim())
                    }
                }
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}