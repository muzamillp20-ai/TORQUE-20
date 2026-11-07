package com.torque.bikecare.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.torque.bikecare.R
import com.torque.bikecare.data.repository.AuthRepository
import com.torque.bikecare.data.repository.BookingRepository
import com.torque.bikecare.data.repository.ServiceRepository
import com.torque.bikecare.data.repository.WorkshopRepository
import com.torque.bikecare.ui.screens.admin.AdminBookingsScreen
import com.torque.bikecare.ui.screens.admin.AdminConfigScreen
import com.torque.bikecare.ui.screens.admin.AdminCustomersScreen
import com.torque.bikecare.ui.screens.admin.AdminDashboardScreen
import com.torque.bikecare.ui.screens.admin.AdminServicesScreen
import com.torque.bikecare.ui.screens.auth.AuthScreen
import com.torque.bikecare.ui.screens.customer.BookServiceScreen
import com.torque.bikecare.ui.screens.customer.BookingDetailsScreen
import com.torque.bikecare.ui.screens.customer.CustomerHomeScreen
import com.torque.bikecare.ui.screens.customer.CustomerProfileScreen
import com.torque.bikecare.ui.screens.customer.MyBookingsScreen
import com.torque.bikecare.ui.screens.customer.ServicesScreen
import com.torque.bikecare.ui.screens.splash.SplashScreen
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.AuthViewModel
import com.torque.bikecare.viewmodel.CustomerViewModel

@Composable
fun TorqueNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val databaseId = context.getString(R.string.firestore_database_id)
    val db = FirebaseFirestore.getInstance(databaseId)

    val authRepo = AuthRepository(db)
    val serviceRepo = ServiceRepository(db)
    val bookingRepo = BookingRepository(db)
    val workshopRepo = WorkshopRepository(db)

    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(authRepo)
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = authViewModel.isCurrentUserAdmin()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                currentUser = currentUser,
                isAdmin = isAdmin,
                onNavigateNext = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    val dest = if (authViewModel.isCurrentUserAdmin()) Screen.AdminDashboard.route else Screen.CustomerHome.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Customer Routes ---
        composable(Screen.CustomerHome.route) {
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            CustomerHomeScreen(
                customerViewModel = customerViewModel,
                userName = user?.displayName ?: "Rider",
                onBookServiceClick = { service ->
                    customerViewModel.selectService(service)
                    navController.navigate(Screen.BookService.route)
                },
                onViewServicesClick = {
                    navController.navigate(Screen.Services.route)
                },
                onViewBookingsClick = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onBookingDetailsClick = { bookingId ->
                    navController.navigate(Screen.BookingDetails.createRoute(bookingId))
                },
                onProfileClick = {
                    navController.navigate(Screen.CustomerProfile.route)
                }
            )
        }

        composable(Screen.Services.route) {
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            BackHandler { navController.popBackStack() }

            ServicesScreen(
                customerViewModel = customerViewModel,
                onBackClick = { navController.popBackStack() },
                onBookServiceClick = { service ->
                    customerViewModel.selectService(service)
                    navController.navigate(Screen.BookService.route)
                }
            )
        }

        composable(Screen.BookService.route) {
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            val selectedService by customerViewModel.selectedService.collectAsState()

            BackHandler { navController.popBackStack() }

            BookServiceScreen(
                customerViewModel = customerViewModel,
                initialService = selectedService,
                defaultCustomerName = user?.displayName ?: "",
                defaultCustomerEmail = user?.email ?: "",
                onBackClick = { navController.popBackStack() },
                onBookingSuccess = { bookingId ->
                    navController.navigate(Screen.MyBookings.route) {
                        popUpTo(Screen.CustomerHome.route)
                    }
                }
            )
        }

        composable(Screen.MyBookings.route) {
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            BackHandler { navController.popBackStack() }

            MyBookingsScreen(
                customerViewModel = customerViewModel,
                onBackClick = { navController.popBackStack() },
                onBookingClick = { bookingId ->
                    navController.navigate(Screen.BookingDetails.createRoute(bookingId))
                },
                onNewBookingClick = {
                    navController.navigate(Screen.BookService.route)
                }
            )
        }

        composable(
            route = Screen.BookingDetails.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            BackHandler { navController.popBackStack() }

            BookingDetailsScreen(
                bookingId = bookingId,
                customerViewModel = customerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.CustomerProfile.route) {
            val user = currentUser
            val currentUid = user?.uid ?: ""
            val customerViewModel: CustomerViewModel = viewModel(key = currentUid) {
                CustomerViewModel(serviceRepo, bookingRepo, workshopRepo, currentUid)
            }

            BackHandler { navController.popBackStack() }

            CustomerProfileScreen(
                authViewModel = authViewModel,
                customerViewModel = customerViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route)
                },
                onSignOutComplete = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- Admin Routes ---
        composable(Screen.AdminDashboard.route) {
            val adminViewModel: AdminViewModel = viewModel {
                AdminViewModel(bookingRepo, serviceRepo, workshopRepo)
            }

            AdminDashboardScreen(
                adminViewModel = adminViewModel,
                onNavigateToBookings = { navController.navigate(Screen.AdminBookings.route) },
                onNavigateToServices = { navController.navigate(Screen.AdminServices.route) },
                onNavigateToCustomers = { navController.navigate(Screen.AdminCustomers.route) },
                onNavigateToSettings = { navController.navigate(Screen.AdminConfig.route) },
                onSwitchToCustomerView = { navController.navigate(Screen.CustomerHome.route) }
            )
        }

        composable(Screen.AdminBookings.route) {
            val adminViewModel: AdminViewModel = viewModel {
                AdminViewModel(bookingRepo, serviceRepo, workshopRepo)
            }

            BackHandler { navController.popBackStack() }

            AdminBookingsScreen(
                adminViewModel = adminViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminServices.route) {
            val adminViewModel: AdminViewModel = viewModel {
                AdminViewModel(bookingRepo, serviceRepo, workshopRepo)
            }

            BackHandler { navController.popBackStack() }

            AdminServicesScreen(
                adminViewModel = adminViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminCustomers.route) {
            val adminViewModel: AdminViewModel = viewModel {
                AdminViewModel(bookingRepo, serviceRepo, workshopRepo)
            }

            BackHandler { navController.popBackStack() }

            AdminCustomersScreen(
                adminViewModel = adminViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminConfig.route) {
            val adminViewModel: AdminViewModel = viewModel {
                AdminViewModel(bookingRepo, serviceRepo, workshopRepo)
            }

            BackHandler { navController.popBackStack() }

            AdminConfigScreen(
                adminViewModel = adminViewModel,
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onSignOutComplete = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
