package com.ll.taskflowv3.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ll.taskflowv3.presentation.create_task.CreateTaskScreen
import com.ll.taskflowv3.presentation.home.HomeScreen
import com.ll.taskflowv3.presentation.login.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                // Le damos instrucciones a la pantalla Home de qué hacer al picar el FAB
                onNavigateToCreateTask = {
                    navController.navigate("create_task")
                }
            )
        }

        // NUEVA RUTA
        composable("create_task") {
            CreateTaskScreen(
                onNavigateBack = {
                    navController.popBackStack() // Nos regresa a la pantalla anterior (Home)
                }
            )
        }
    }
}