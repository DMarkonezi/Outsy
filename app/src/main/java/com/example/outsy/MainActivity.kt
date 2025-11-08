package com.example.outsy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.outsy.ui.main.HomeScreen
import com.example.outsy.ui.auth.LoginScreen
import com.example.outsy.ui.auth.RegisterScreen
import com.example.outsy.viewmodel.AuthViewModel

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
    val authViewModel: AuthViewModel = viewModel()

    MaterialTheme {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginClick = {email, password ->
                        authViewModel.login(email, password)
                        navController.navigate("home")
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterClick = { firstName, lastName, email, password, phone, profileBitmap ->
                        authViewModel.register(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password,
                            phoneNumber = phone,
                            location = null,
                            profileBitmap = profileBitmap
                        ) { success, errorMessage ->
                            if (success) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Log.e("Register", "Registration failed: $errorMessage")
                            }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(navController)
            }
        }
    }


}
