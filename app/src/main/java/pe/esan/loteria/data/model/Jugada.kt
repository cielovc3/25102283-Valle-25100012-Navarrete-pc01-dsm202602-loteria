package pe.esan.loteria.data.model

data class Jugada(
    val uid: String,
    val sorteoId: String,
    val numeros: List<Long>
)
