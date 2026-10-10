package pe.esan.loteria.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.esan.loteria.data.remote.FirebaseAuthManager
import pe.esan.loteria.presentation.auth.LoginScreen
import pe.esan.loteria.presentation.auth.RegistroScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // Si ya hay sesión iniciada, la app abre directo en la jugada
    val inicio = if (FirebaseAuthManager.usuarioActual != null) Routes.JUGADA else Routes.LOGIN

    val cerrarSesion = {
        FirebaseAuthManager.cerrarSesion()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true } // borra todo el historial
        }
    }

    NavHost(navController = navController, startDestination = inicio) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Routes.JUGADA) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Routes.REGISTRO) }
            )
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = {
                    navController.navigate(Routes.JUGADA) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onIrALogin = { navController.popBackStack() }
            )
        }

        // TEMPORAL: se reemplaza por JugadaScreen de Italo
        composable(Routes.JUGADA) {
            PantallaTemporal("Jugada (de Italo)", "Ir al seguimiento") {
                navController.navigate(Routes.SEGUIMIENTO)
            }
        }

        // TEMPORAL: se reemplaza por SeguimientoScreen de Italo
        composable(Routes.SEGUIMIENTO) {
            PantallaTemporal("Seguimiento (de Italo)", "Cerrar sesión", cerrarSesion)
        }
    }
}

// TEMPORAL: se borra al unir la rama de Italo
@Composable
private fun PantallaTemporal(titulo: String, textoBoton: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo)
        Text("Sesión: ${FirebaseAuthManager.usuarioActual?.email}")
        Button(onClick = onClick) { Text(textoBoton) }
    }
}