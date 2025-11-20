package com.example.proyecto1.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyecto1.presentation.forgotpassword.ForgotPasswordScreen
import com.example.proyecto1.presentation.login.LoginScreen
import com.example.proyecto1.presentation.parkinglist.ParkingListScreen
import com.example.proyecto1.presentation.profile.ProfileScreen
import com.example.proyecto1.presentation.register.RegisterScreen
import com.example.proyecto1.presentation.reservation.ReservationScreen
import com.example.proyecto1.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.ParkingList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.ParkingList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password Screen
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Parking List Screen (Main)
        composable(route = Screen.ParkingList.route) {
            ParkingListScreen(
                onNavigateToReservation = { parkingId, basementNumber ->
                    navController.navigate(
                        Screen.Reservation.createRoute(parkingId, basementNumber)
                    )
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Reservation Screen with arguments
        composable(
            route = Screen.Reservation.route,
            arguments = listOf(
                navArgument("parkingId") { type = NavType.StringType },
                navArgument("basementNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""
            val basementNumber = backStackEntry.arguments?.getInt("basementNumber") ?: 0

            ReservationScreen(
                parkingId = parkingId,
                basementNumber = basementNumber,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile Screen
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // No Connection Screen
        composable(route = Screen.NoConnection.route) {
            // TODO: Implement NoConnectionScreen
        }

        // Full Parking Screen with argument
        composable(
            route = Screen.FullParking.route,
            arguments = listOf(
                navArgument("basementNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val basementNumber = backStackEntry.arguments?.getInt("basementNumber") ?: 0
            // TODO: Implement FullParkingScreen
        }
    }
}