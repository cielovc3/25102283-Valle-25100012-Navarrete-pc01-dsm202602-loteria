package pe.esan.loteria.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    // Usuario con sesión iniciada (null si no hay nadie)
    val usuarioActual get() = auth.currentUser

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(correo, contrasena).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrar(correo: String, contrasena: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(correo, contrasena).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun mensajeDeError(error: Throwable): String = when (error) {
        is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "Sin conexión a internet."
        else -> error.localizedMessage ?: "Ocurrió un error."
    }
}