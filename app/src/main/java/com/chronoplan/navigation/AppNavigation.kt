package com.chronoplan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chronoplan.ui.main.MainScreen
import com.chronoplan.ui.user.signin.SignInScreen
import com.chronoplan.ui.user.signup.SignUpScreen

object AppDestinations {
    const val SIGN_IN = "signin"
    const val SIGN_UP = "signup"
    const val MAIN_ROUTE = "main"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navigateToMainSafe: () -> Unit = {
        navController.navigate(AppDestinations.MAIN_ROUTE) {
            popUpTo(AppDestinations.SIGN_IN) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = AppDestinations.SIGN_IN) {
        composable(AppDestinations.SIGN_IN) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(AppDestinations.SIGN_UP) },
                onNavigateToHome = navigateToMainSafe
            )
        }

        composable(AppDestinations.SIGN_UP) {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
                onNavigateToHome = navigateToMainSafe
            )
        }

        composable(AppDestinations.MAIN_ROUTE) {
            MainScreen(
                onLogoutSuccess = {
                    navController.navigate(AppDestinations.SIGN_IN) {
                        popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
}