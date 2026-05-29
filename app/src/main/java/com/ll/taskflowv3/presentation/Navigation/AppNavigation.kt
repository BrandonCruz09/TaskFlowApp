package com.ll.taskflowv3.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Importaciones de tus pantallas
import com.ll.taskflowv3.presentation.login.LoginScreen
import com.ll.taskflowv3.presentation.home.HomeScreen
import com.ll.taskflowv3.presentation.create_task.CreateTaskScreen
import com.ll.taskflowv3.presentation.admin.AdminDashboardScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // INICIO DEL MAPA DE NAVEGACIÓN
    NavHost(navController = navController, startDestination = "login") {

        // PANTALLA 0: LOGIN
        composable("login") {
            LoginScreen(
                onNavigateToHome = {  // <-- ¡ESTE ES EL VERDADERO NOMBRE!
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA 1: HOME
        composable("home") {
            HomeScreen(
                onNavigateToCreateTask = { navController.navigate("create_task") },
                onNavigateToAdmin = { navController.navigate("admin_dashboard") }
            )
        }

        // PANTALLA 2: CREAR TAREA
        composable("create_task") {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // PANTALLA 3: DASHBOARD ADMINISTRATIVO
        composable("admin_dashboard") {
            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

    } // LLAVE DE CIERRE DEL NAVHOST
}