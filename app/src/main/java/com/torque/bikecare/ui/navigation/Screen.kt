package com.torque.bikecare.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object CustomerHome : Screen("customer_home")
    object Services : Screen("services")
    object BookService : Screen("book_service")
    object MyBookings : Screen("my_bookings")
    object BookingDetails : Screen("booking_details/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_details/$bookingId"
    }
    object CustomerProfile : Screen("customer_profile")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminBookings : Screen("admin_bookings")
    object AdminServices : Screen("admin_services")
    object AdminCustomers : Screen("admin_customers")
    object AdminConfig : Screen("admin_config")
}
