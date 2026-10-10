package pe.esan.loteria.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import pe.esan.loteria.data.model.Bolilla
import pe.esan.loteria.data.model.Jugada
import pe.esan.loteria.data.model.Sorteo

object SorteoManager {

    const val SORTEO_ID = "sorteo-prueba"

    private val firestore = FirebaseFirestore.getInstance()

    private val sorteoRef = firestore.collection("sorteos").document(SORTEO_ID)

    fun escucharSorteo(
        onCambio: (Result<Sorteo?>) -> Unit
    ): ListenerRegistration {

        return sorteoRef.addSnapshotListener { snapshot, error ->

            if (error != null) {
                onCambio(Result.failure(error))
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                onCambio(Result.success(null))
                return@addSnapshotListener
            }

            val sorteo = Sorteo(
                id = snapshot.id,
                nombre = snapshot.getString("nombre") ?: "",
                fecha = snapshot.getString("fecha") ?: "",
                estado = snapshot.getString("estado") ?: ""
            )

            onCambio(Result.success(sorteo))
        }
    }

    fun escucharBolillas(
        onCambio: (Result<List<Bolilla>>) -> Unit
    ): ListenerRegistration {

        return sorteoRef
            .collection("bolillas")
            .orderBy("orden")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onCambio(Result.failure(error))
                    return@addSnapshotListener
                }

                val bolillas = snapshot?.documents?.mapNotNull { documento ->

                    val numero =
                        (documento.get("numero") as? Number)?.toLong()

                    val orden =
                        (documento.get("orden") as? Number)?.toLong()

                    if (numero != null && orden != null) {
                        Bolilla(numero, orden)
                    } else {
                        null
                    }
                } ?: emptyList()

                onCambio(Result.success(bolillas))
            }
    }

    suspend fun obtenerJugada(): Result<Jugada?> {

        return try {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw Exception("Usuario no autenticado")

            val snapshot = sorteoRef
                .collection("jugadas")
                .document(uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                Result.success(null)
            } else {

                val numeros =
                    (snapshot.get("numeros") as? List<*>)
                        ?.mapNotNull {
                            (it as? Number)?.toLong()
                        }
                        ?: emptyList()

                val jugada = Jugada(
                    uid = snapshot.getString("uid") ?: uid,
                    sorteoId = snapshot.getString("sorteoId") ?: SORTEO_ID,
                    numeros = numeros
                )

                Result.success(jugada)
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarJugada(
        numeros: List<Long>
    ): Result<Unit> {

        return try {

            if (numeros.size != 6) {
                throw Exception("Debe ingresar exactamente 6 números")
            }

            if (numeros.distinct().size != 6) {
                throw Exception("Los números deben ser distintos")
            }

            if (numeros.any { it !in 1L..45L }) {
                throw Exception("Los números deben estar entre 1 y 45")
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw Exception("Usuario no autenticado")

            val sorteoSnapshot = sorteoRef.get().await()

            val estado = sorteoSnapshot.getString("estado")

            if (estado != "abierto") {
                throw Exception(
                    "El sorteo ya inició: no se puede modificar la jugada"
                )
            }

            val datos = hashMapOf(
                "uid" to uid,
                "sorteoId" to SORTEO_ID,
                "numeros" to numeros
            )

            sorteoRef
                .collection("jugadas")
                .document(uid)
                .set(datos)
                .await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}