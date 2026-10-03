package pe.edu.esan.sportpro.ui.teams

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.esan.sportpro.data.repository.AcademyRepository

data class AcademyUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class AcademyViewModel(
    private val repository: AcademyRepository = AcademyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademyUiState())
    val uiState: StateFlow<AcademyUiState> = _uiState.asStateFlow()

    /** onSuccess recibe el id de la academia creada. */
    fun createAcademy(
        name: String,
        description: String,
        address: String,
        onSuccess: (String) -> Unit
    ) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        repository.createAcademy(name, description, address) { result ->
            result
                .onSuccess { academy ->
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess(academy.id)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun Throwable.toUserMessage(): String = when {
        this is IllegalArgumentException || this is IllegalStateException ->
            message ?: "No se pudo crear la academia"

        this is FirebaseFirestoreException &&
                code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Solo el administrador puede crear una academia"

        this is FirebaseFirestoreException &&
                code == FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Sin conexión. Inténtalo de nuevo cuando vuelva Internet"

        else -> "Ocurrió un error. Inténtalo de nuevo"
    }
}