package pe.esan.loteria.presentation.seguimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.esan.loteria.data.model.Bolilla
import pe.esan.loteria.data.model.Jugada
import pe.esan.loteria.data.model.Sorteo
import pe.esan.loteria.data.remote.SorteoManager

@Composable
fun SeguimientoScreen(
    onVolver: () -> Unit
) {

    var sorteo by remember {
        mutableStateOf<Sorteo?>(null)
    }

    var bolillas by remember {
        mutableStateOf<List<Bolilla>>(emptyList())
    }

    var jugada by remember {
        mutableStateOf<Jugada?>(null)
    }

    var mensajeError by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {

        val listenerSorteo =
            SorteoManager.escucharSorteo { resultado ->

                if (resultado.isSuccess) {
                    sorteo = resultado.getOrNull()
                } else {
                    mensajeError =
                        resultado.exceptionOrNull()?.message
                            ?: "Error al escuchar el sorteo"
                }
            }

        val listenerBolillas =
            SorteoManager.escucharBolillas { resultado ->

                if (resultado.isSuccess) {
                    bolillas = resultado.getOrDefault(emptyList())
                } else {
                    mensajeError =
                        resultado.exceptionOrNull()?.message
                            ?: "Error al escuchar las bolillas"
                }
            }

        onDispose {
            listenerSorteo.remove()
            listenerBolillas.remove()
        }
    }

    LaunchedEffect(Unit) {

        val resultado = SorteoManager.obtenerJugada()

        if (resultado.isSuccess) {
            jugada = resultado.getOrNull()
        } else {
            mensajeError =
                resultado.exceptionOrNull()?.message
                    ?: "Error al recuperar la jugada"
        }
    }

    val numerosSorteados =
        bolillas.map { it.numero }.toSet()

    val aciertos =
        jugada
            ?.numeros
            ?.toSet()
            ?.intersect(numerosSorteados)
            ?.size
            ?: 0

    val bolillasPublicadas = bolillas.size

    val mensajeEstado = when {

        sorteo?.estado == "abierto" ->
            "Esperando el inicio del sorteo"

        sorteo?.estado == "en_curso" ->
            "Sorteo en curso"

        sorteo?.estado == "finalizado" &&
                bolillasPublicadas < 6 ->
            "Esperando resultados"

        sorteo?.estado == "finalizado" &&
                bolillasPublicadas == 6 &&
                aciertos == 6 ->
            "GANADOR"

        sorteo?.estado == "finalizado" &&
                bolillasPublicadas == 6 ->
            "SIGUE INTENTANDO"

        else ->
            "Cargando sorteo..."
    }

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Seguimiento del sorteo",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                "Sorteo: ${sorteo?.nombre ?: "Cargando..."}"
            )

            Text(
                "Fecha: ${sorteo?.fecha ?: "-"}"
            )

            Text(
                "Estado: ${sorteo?.estado ?: "-"}"
            )

            Text(
                text = mensajeEstado,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                "Bolillas publicadas: $bolillasPublicadas/6"
            )

            Text(
                "Aciertos: $aciertos"
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Bolillas sorteadas",
                style = MaterialTheme.typography.titleMedium
            )

            if (bolillas.isEmpty()) {

                Text("Todavía no se publicaron bolillas")

            } else {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    bolillas
                        .sortedBy { it.orden }
                        .forEach { bolilla ->

                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color =
                                    MaterialTheme.colorScheme.primaryContainer
                            ) {

                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.Center
                                ) {
                                    Text(
                                        bolilla.numero.toString()
                                    )
                                }
                            }
                        }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Tu jugada",
                style = MaterialTheme.typography.titleMedium
            )

            if (jugada == null) {

                Text("No tienes una jugada registrada")

            } else {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    jugada!!.numeros.forEach { numero ->

                        val acertado =
                            numero in numerosSorteados

                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color =
                                if (acertado)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                        ) {

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Center
                            ) {

                                Text(
                                    text = numero.toString(),
                                    color =
                                        if (acertado)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (mensajeError.isNotBlank()) {

                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}