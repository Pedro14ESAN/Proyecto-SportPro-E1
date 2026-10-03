package pe.edu.esan.sportpro.ui.players

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.esan.sportpro.data.model.Player
import pe.edu.esan.sportpro.data.repository.PlayerRepository

class PlayerViewModel(
    private val repository: PlayerRepository = PlayerRepository()
) : ViewModel() {

    // Lista de jugadores
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    // Jugador seleccionado para ver su perfil
    private val _selectedPlayer = MutableStateFlow<Player?>(null)
    val selectedPlayer: StateFlow<Player?> = _selectedPlayer.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mensajes de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()


    // Obtener jugadores de una academia
    fun loadPlayersByAcademy(academyId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getPlayersByAcademy(academyId) { result ->

            _isLoading.value = false

            result
                .onSuccess { playerList ->
                    _players.value = playerList
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al obtener jugadores"
                }
        }
    }


    // Obtener jugadores de un equipo
    fun loadPlayersByTeam(teamId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getPlayersByTeam(teamId) { result ->

            _isLoading.value = false

            result
                .onSuccess { playerList ->
                    _players.value = playerList
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al obtener jugadores"
                }
        }
    }


    // Obtener un jugador para visualizar su perfil
    fun loadPlayerById(playerId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getPlayerById(playerId) { result ->

            _isLoading.value = false

            result
                .onSuccess { player ->
                    _selectedPlayer.value = player
                }
                .onFailure { exception ->
                    _errorMessage.value =
                        exception.message ?: "Error al obtener el jugador"
                }
        }
    }


    // Registrar un nuevo jugador
    fun createPlayer(
        player: Player,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        repository.createPlayer(player) { result ->

            _isLoading.value = false

            result
                .onSuccess { newPlayer ->

                    _players.value = _players.value + newPlayer

                    _successMessage.value =
                        "Jugador registrado correctamente"

                    onSuccess()
                }
                .onFailure { exception ->

                    _errorMessage.value =
                        exception.message ?: "No se pudo registrar al jugador"
                }
        }
    }


    // Actualizar información del jugador
    fun updatePlayer(
        player: Player,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        repository.updatePlayer(player) { result ->

            _isLoading.value = false

            result
                .onSuccess {

                    _players.value = _players.value.map {
                        if (it.id == player.id) player else it
                    }

                    _selectedPlayer.value = player

                    _successMessage.value =
                        "Jugador actualizado correctamente"

                    onSuccess()
                }
                .onFailure { exception ->

                    _errorMessage.value =
                        exception.message ?: "No se pudo actualizar al jugador"
                }
        }
    }


    // Desactivar jugador
    fun deactivatePlayer(
        playerId: String,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        repository.deactivatePlayer(playerId) { result ->

            _isLoading.value = false

            result
                .onSuccess {

                    _players.value =
                        _players.value.filterNot { it.id == playerId }

                    _successMessage.value =
                        "Jugador desactivado correctamente"

                    onSuccess()
                }
                .onFailure { exception ->

                    _errorMessage.value =
                        exception.message ?: "No se pudo desactivar al jugador"
                }
        }
    }


    // Seleccionar jugador directamente
    fun selectPlayer(player: Player) {
        _selectedPlayer.value = player
    }


    // Limpiar mensajes de pantalla
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}