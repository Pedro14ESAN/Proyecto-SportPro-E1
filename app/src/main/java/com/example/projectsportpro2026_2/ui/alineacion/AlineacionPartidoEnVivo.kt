package com.example.projectsportpro2026_2.ui.alineacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import com.example.projectsportpro2026_2.datos.alineacion.calcularIndicadoresJugadores
import com.example.projectsportpro2026_2.datos.alineacion.calcularSustituciones
import com.example.projectsportpro2026_2.datos.modelo.Jugador
import com.example.projectsportpro2026_2.datos.modelo.evento.EventoPartido
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.projectsportpro2026_2.datos.repositorio.alineacion.AlineacionEnVivo
import com.example.projectsportpro2026_2.datos.repositorio.alineacion.RepositorioAlineaciones
import com.example.projectsportpro2026_2.ui.partidos.SportProNavy
import com.example.projectsportpro2026_2.ui.partidos.SportProTextoSecundario
import com.example.projectsportpro2026_2.ui.partidos.SportProVerde
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
internal fun AlineacionPartidoEnVivo(
    idPartido: String,
    idEquipo: String,
    nombreEquipo: String,
    eventos: List<EventoPartido>,
    modifier: Modifier = Modifier
) {
    val modelo: AlineacionEnVivoViewModel = viewModel(key = "alineacion-vivo:$idPartido:$idEquipo")
    val estado by modelo.estado.collectAsState()

    LaunchedEffect(modelo) {
        modelo.iniciar(idPartido, idEquipo)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alineación",
            style = MaterialTheme.typography.titleLarge,
            color = SportProNavy,
            fontWeight = FontWeight.Bold
        )

        when (val actual = estado) {
            EstadoAlineacionEnVivo.Cargando ->
                CircularProgressIndicator(color = SportProVerde)

            EstadoAlineacionEnVivo.Error -> {
                Text(
                    text = "No se pudo cargar la alineación.",
                    color = SportProTextoSecundario
                )
                TextButton(onClick = { modelo.cargar(idPartido, idEquipo) }) {
                    Text("Reintentar")
                }
            }

            EstadoAlineacionEnVivo.SinConfirmar ->
                Text(
                    text = "El equipo todavía no tiene una alineación confirmada disponible.",
                    color = SportProTextoSecundario
                )

            is EstadoAlineacionEnVivo.Disponible -> {
                val alineacion = actual.alineacion
                val titulares = remember(alineacion, eventos) {
                    calcularSustituciones(alineacion.alineados, eventos).titulares
                }
                val idsSinFicha = remember(alineacion, titulares) {
                    val conocidos = alineacion.plantilla.map { it.id }.toSet()
                    titulares.map { it.idJugador }.filter { it !in conocidos }
                }
                val fichasAdicionales by produceState<List<Jugador>>(emptyList(), modelo, idsSinFicha) {
                    value = modelo.leerFichas(idsSinFicha)
                }
                val plantilla = remember(alineacion, titulares, fichasAdicionales, eventos) {
                    val fichas = (alineacion.plantilla + fichasAdicionales).associateBy { it.id }
                    titulares.map { titular ->
                        fichas[titular.idJugador] ?: jugadorDesdeEvento(titular.idJugador, eventos)
                    }
                }
                Text(
                    text = "$nombreEquipo · ${alineacion.formato.etiqueta}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SportProTextoSecundario
                )
                CanchaAlineacionDerivada(alineacion, eventos, plantilla)
            }
        }
    }
}

internal sealed interface EstadoAlineacionEnVivo {
    data object Cargando : EstadoAlineacionEnVivo
    data object SinConfirmar : EstadoAlineacionEnVivo
    data object Error : EstadoAlineacionEnVivo
    data class Disponible(val alineacion: AlineacionEnVivo) : EstadoAlineacionEnVivo
}

internal class AlineacionEnVivoViewModel : ViewModel() {
    private val repositorio = RepositorioAlineaciones()
    private val _estado = MutableStateFlow<EstadoAlineacionEnVivo>(EstadoAlineacionEnVivo.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null

    suspend fun leerFichas(ids: List<String>): List<Jugador> =
        repositorio.leerFichasAlineacion(ids).getOrElse {
            if (it is CancellationException) throw it
            emptyList()
        }

    fun iniciar(idPartido: String, idEquipo: String) {
        if (carga == null) cargar(idPartido, idEquipo)
    }

    fun cargar(idPartido: String, idEquipo: String) {
        if (carga?.isActive == true) return
        carga = viewModelScope.launch {
            _estado.value = EstadoAlineacionEnVivo.Cargando
            _estado.value = repositorio.leerAlineacionEnVivo(idPartido, idEquipo).fold(
                onSuccess = { alineacion ->
                    if (alineacion == null) EstadoAlineacionEnVivo.SinConfirmar
                    else EstadoAlineacionEnVivo.Disponible(alineacion)
                },
                onFailure = {
                    if (it is CancellationException) throw it
                    EstadoAlineacionEnVivo.Error
                }
            )
        }
    }
}

// Los nombres estructurados permiten mostrar iniciales aunque la ficha ya no exista
// o no pueda descargarse. La identidad siempre se resuelve por ID.
private fun jugadorDesdeEvento(id: String, eventos: List<EventoPartido>): Jugador {
    val nombre = eventos.sortedWith(
        compareByDescending<EventoPartido> { it.minuto }.thenByDescending { it.fechaCaptura }
    ).firstNotNullOfOrNull { evento ->
        when (id) {
            evento.idJugadorEntra -> evento.nombreJugadorEntra.takeIf { it.isNotBlank() }
            evento.idJugadorSale -> evento.nombreJugadorSale.takeIf { it.isNotBlank() }
            else -> null
        }
    }?.trim().orEmpty()
    return Jugador(
        id = id,
        nombres = nombre.substringBefore(" ").ifBlank { "Jugador" },
        apellidos = nombre.substringAfter(" ", "")
    )
}

@Composable
internal fun CanchaAlineacionDerivada(
    base: AlineacionEnVivo,
    eventos: List<EventoPartido>,
    plantilla: List<Jugador>
) {
    val actuales = remember(base, eventos) {
        calcularSustituciones(base.alineados, eventos).titulares
    }
    val indicadores = remember(eventos) { calcularIndicadoresJugadores(eventos) }
    CanchaAlineacion(
        slots = slotsParaFormato(base.formato, base.formacion),
        titulares = actuales,
        plantilla = plantilla,
        indicadores = indicadores
    )
}
