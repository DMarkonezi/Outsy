package com.example.outsy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.outsy.ui.main.UserMainScreen
import com.example.outsy.ui.auth.LoginScreen
import com.example.outsy.ui.auth.RegisterScreen
import com.example.outsy.viewmodel.AuthViewModel
import com.example.outsy.ui.owner.OwnerMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    // View Models
    val authViewModel: AuthViewModel = viewModel()

    // Auth States
    val authStateValue by authViewModel.authState.collectAsStateWithLifecycle()
    val userTypeValue by authViewModel.userType.collectAsStateWithLifecycle()

    MaterialTheme {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginScreen(
                    onLoginClick = {email, password ->
                        authViewModel.login(email, password)
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegistrationSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            composable("userHome") {
                UserMainScreen(navController)
            }

            composable("ownerMainScreen") {
                OwnerMainScreen()
            }

        }
    }

    LaunchedEffect(authStateValue, userTypeValue) {
        val (success, error) = authStateValue
        if (success && error == null) {
            when (userTypeValue) {
                "placeowner" -> {
                    navController.navigate("ownerMainScreen") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                "user" -> {
                    navController.navigate("userHome") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    }

}
