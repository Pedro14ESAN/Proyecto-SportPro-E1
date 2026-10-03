package pe.edu.esan.sportpro.ui.teams

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.esan.sportpro.data.model.Team
import pe.edu.esan.sportpro.data.repository.DuplicateTeamException
import pe.edu.esan.sportpro.data.repository.TeamNotFoundException
import pe.edu.esan.sportpro.data.repository.TeamRepository

data class TeamsUiState(
    val activeTeams: List<Team> = emptyList(),
    val inactiveTeams: List<Team> = emptyList(), // CA-06: activos e inactivos se muestran por separado
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class TeamsViewModel(
    private val repository: TeamRepository = TeamRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamsUiState())
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    /** Llamar al abrir la pantalla con la academia seleccionada. */
    fun load(academyId: String) {
        listener?.remove()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        listener = repository.observeTeamsByAcademy(academyId) { result ->
            result
                .onSuccess { teams ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            activeTeams = teams.filter { team -> team.active },
                            inactiveTeams = teams.filterNot { team -> team.active }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun createTeam(
        academyId: String,
        name: String,
        category: String,
        coachUid: String,
        season: String,
        onSuccess: () -> Unit = {}
    ) {
        startSaving()
        repository.createTeam(academyId, name, category, coachUid, season) { result ->
            finishSaving(result.map { }, onSuccess)
        }
    }

    fun updateTeam(team: Team, onSuccess: () -> Unit = {}) {
        startSaving()
        repository.updateTeam(team) { result -> finishSaving(result, onSuccess) }
    }

    fun deactivateTeam(teamId: String) {
        startSaving()
        repository.deactivateTeam(teamId) { result -> finishSaving(result) }
    }

    fun activateTeam(teamId: String) {
        startSaving()
        repository.activateTeam(teamId) { result -> finishSaving(result) }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun startSaving() =
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

    private fun finishSaving(result: Result<Unit>, onSuccess: () -> Unit = {}) {
        result
            .onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }
            .onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) }
            }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }

    private fun Throwable.toUserMessage(): String = when {
        this is DuplicateTeamException ||
                this is TeamNotFoundException ||
                this is IllegalArgumentException ||
                this is IllegalStateException -> message ?: "No se pudo completar la acción"

        this is FirebaseFirestoreException &&
                code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "No tienes permiso para realizar esta acción"

        this is FirebaseFirestoreException &&
                code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sin conexión. Inténtalo de nuevo cuando vuelva Internet"

        else -> "Ocurrió un error. Inténtalo de nuevo"
    }
}