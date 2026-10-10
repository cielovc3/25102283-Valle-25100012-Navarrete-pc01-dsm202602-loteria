package pe.esan.loteria.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.esan.loteria.data.remote.FirebaseAuthManager
import pe.esan.loteria.presentation.auth.LoginScreen
import pe.esan.loteria.presentation.auth.RegistroScreen
import pe.esan.loteria.presentation.jugada.JugadaScreen
import pe.esan.loteria.presentation.seguimiento.SeguimientoScreen

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

        composable(Routes.JUGADA) {
            JugadaScreen(
                onIrASeguimiento = { navController.navigate(Routes.SEGUIMIENTO) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Routes.SEGUIMIENTO) {
            SeguimientoScreen(
                onVolver = { navController.popBackStack() }
            )
        }
    }
}