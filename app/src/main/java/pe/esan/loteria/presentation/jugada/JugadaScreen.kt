package pe.esan.loteria.presentation.jugada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pe.esan.loteria.data.model.Jugada
import pe.esan.loteria.data.model.Sorteo
import pe.esan.loteria.data.remote.SorteoManager

@Composable
fun JugadaScreen(
    onIrASeguimiento: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val email = FirebaseAuth.getInstance().currentUser?.email ?: "Sin correo"

    var sorteo by remember { mutableStateOf<Sorteo?>(null) }
    var jugadaExistente by remember { mutableStateOf<Jugada?>(null) }

    var numerosTexto by remember {
        mutableStateOf(List(6) { "" })
    }

    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = SorteoManager.escucharSorteo { resultado ->
            if (resultado.isSuccess) {
                sorteo = resultado.getOrNull()
            } else {
                mensajeError =
                    resultado.exceptionOrNull()?.message ?: "Error al escuchar el sorteo"
            }
        }

        onDispose {
            listener.remove()
        }
    }

    LaunchedEffect(Unit) {
        val resultado = SorteoManager.obtenerJugada()

        if (resultado.isSuccess) {
            val jugada = resultado.getOrNull()

            if (jugada != null) {
                jugadaExistente = jugada

                numerosTexto = List(6) { indice ->
                    jugada.numeros.getOrNull(indice)?.toString() ?: ""
                }
            }
        } else {
            mensajeError =
                resultado.exceptionOrNull()?.message ?: "Error al recuperar la jugada"
        }
    }

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Registrar jugada",
                style = MaterialTheme.typography.headlineMedium
            )

            Text("Usuario: $email")

            Text("Sorteo: ${sorteo?.nombre ?: "Cargando..."}")
            Text("Fecha: ${sorteo?.fecha ?: "-"}")
            Text("Estado: ${sorteo?.estado ?: "-"}")

            Spacer(modifier = Modifier.height(8.dp))

            if (sorteo?.estado == "abierto") {

                numerosTexto.forEachIndexed { index, valor ->

                    OutlinedTextField(
                        value = valor,
                        onValueChange = { nuevoValor ->

                            val nuevaLista = numerosTexto.toMutableList()
                            nuevaLista[index] = nuevoValor
                            numerosTexto = nuevaLista
                        },
                        label = {
                            Text("Número ${index + 1}")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {

                        mensajeError = ""
                        mensajeExito = ""

                        val numeros = numerosTexto.mapNotNull {
                            it.toLongOrNull()
                        }

                        if (numeros.size != 6) {
                            mensajeError = "Debe ingresar seis números válidos"
                        } else {

                            scope.launch {

                                cargando = true

                                val resultado =
                                    SorteoManager.guardarJugada(numeros)

                                cargando = false

                                if (resultado.isSuccess) {

                                    jugadaExistente = Jugada(
                                        uid = FirebaseAuth.getInstance()
                                            .currentUser?.uid ?: "",
                                        sorteoId = SorteoManager.SORTEO_ID,
                                        numeros = numeros
                                    )

                                    mensajeExito =
                                        if (jugadaExistente != null)
                                            "Jugada guardada correctamente"
                                        else
                                            "Jugada registrada correctamente"

                                } else {

                                    mensajeError =
                                        resultado.exceptionOrNull()?.message
                                            ?: "Error al guardar la jugada"
                                }
                            }
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (jugadaExistente == null)
                            "Registrar jugada"
                        else
                            "Actualizar jugada"
                    )
                }

                if (cargando) {
                    CircularProgressIndicator()
                }

            } else {

                Text(
                    "El sorteo ya inició: no se pueden registrar ni modificar jugadas"
                )

                if (jugadaExistente != null) {

                    Text(
                        "Tu jugada: ${
                            jugadaExistente!!.numeros.joinToString(", ")
                        }"
                    )

                } else {

                    Text("No tienes una jugada registrada")
                }
            }

            if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (mensajeExito.isNotBlank()) {
                Text(
                    text = mensajeExito,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onIrASeguimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir al seguimiento")
            }

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}