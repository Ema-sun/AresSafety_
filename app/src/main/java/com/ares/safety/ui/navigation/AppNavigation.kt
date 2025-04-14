package com.ares.safety.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ares.safety.ui.screens.auth.LoginScreen
import com.ares.safety.ui.screens.auth.RegisterScreen
import com.ares.safety.ui.screens.contacts.AddEditContactScreen
import com.ares.safety.ui.screens.contacts.ContactsScreen
import com.ares.safety.ui.screens.home.HomeScreen
import com.ares.safety.ui.screens.onboarding.OnboardingScreen
import com.ares.safety.ui.screens.profile.ProfileScreen
import com.ares.safety.utils.PreferencesManager
import com.ares.safety.viewmodel.contact.ContactViewModel
import com.ares.safety.viewmodel.profile.ProfileViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object AddEditContact : Screen("add_edit_contact?contactId={contactId}") {
        fun createRoute(contactId: String? = null): String {
            return if (contactId != null) {
                "add_edit_contact?contactId=$contactId"
            } else {
                "add_edit_contact"
            }
        }
    }
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(startDestination: String = Screen.Login.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navigateToRegister = { navController.navigate(Screen.Register.route) },
                navigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navigateToLogin = { navController.navigate(Screen.Login.route) },
                navigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Contacts.route) {
            val contactViewModel: ContactViewModel = viewModel()
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddContact = {
                    navController.navigate(Screen.AddEditContact.createRoute())
                },
                onEditContact = { contactId ->
                    navController.navigate(Screen.AddEditContact.createRoute(contactId))
                },
                viewModel = contactViewModel
            )
        }

        composable(
            route = Screen.AddEditContact.route,
            arguments = listOf(
                navArgument("contactId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")
            val contactViewModel: ContactViewModel = viewModel()

            AddEditContactScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = contactViewModel
            )
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToContacts = {
                    navController.navigate(Screen.Contacts.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToPanic = {
                    // Para implementar más adelante
                },
                onNavigateToAlerts = {
                    // Para implementar más adelante
                }
            )
        }
    }
}