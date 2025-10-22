package com.example.proyecto1.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ParkingList : Screen("parking_list")
    object ParkingMap : Screen("parking_map")
    object Reservation : Screen("reservation/{parkingId}/{basementNumber}") {
        fun createRoute(parkingId: String, basementNumber: Int) =
            "reservation/$parkingId/$basementNumber"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object NoConnection : Screen("no_connection")
    object FullParking : Screen("full_parking/{basementNumber}") {
        fun createRoute(basementNumber: Int) = "full_parking/$basementNumber"
    }
}