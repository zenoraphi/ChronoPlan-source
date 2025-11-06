package com.chronoplan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        navController.navigate("main") {
            popUpTo("signin") { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = "signin") {
        composable("signin") {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToHome = navigateToMainSafe
            )
        }
        composable("main") {
            MainScreen(onLogoutSuccess = {
                navController.navigate("signin") { popUpTo("main") { inclusive = true } }
            })
        }
        // signup...
    }
}
