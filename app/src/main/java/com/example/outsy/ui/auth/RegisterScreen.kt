package com.example.outsy.ui.auth

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.outsy.ui.main.HomeScreen
import com.example.outsy.viewmodel.AuthViewModel

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun RegisterScreenPreview() {
//    MaterialTheme {
//        RegisterScreen(
//            onRegisterClick = {_, _, _, _, _, _-> },
//            onNavigateToLogin = {}
//        )
//    }
//}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("User", "Place Owner")

    Column(modifier = Modifier.fillMaxSize()) {
        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> UserRegisterForm(
                onRegisterClick = { username, first, last, email, pass, phone, bitmap ->
                    viewModel.registerUser(
                        username, first, last, email, pass, phone, null, bitmap
                    ) { success, message ->
                        if (success) {
                            Log.d("Register", "User registered successfully")
                            onRegistrationSuccess()
                        } else {
                            Log.e("Register", "Registration failed: $message")
                        }
                    }
                },
                onNavigateToLogin = { onRegistrationSuccess() }
            )
            1 -> PlaceOwnerRegisterForm(
                onRegisterClick = { businessName, email, pass, phone ->
                    viewModel.registerPlaceOwner(
                        businessName, email, pass, phone
                    ) { success, message ->
                        if (success) {
                            Log.d("Register", "Place owner registered successfully")
                            onRegistrationSuccess()
                        } else {
                            Log.e("Register", "Registration failed: $message")
                        }
                    }
                },
                onNavigateToLogin = { onRegistrationSuccess() }
            )
        }
    }
}

@Composable
fun UserRegisterForm(
    onRegisterClick: (username: String, firstName: String, lastName: String, email: String, password: String, phone: String, profileBitmap: Bitmap?) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Error messages
    var usernameError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            profileBitmap = bitmap
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registration", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = null
            },
            label = { Text("Name") },
            isError = firstNameError != null,
            supportingText = firstNameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = null
            },
            label = { Text("Surname") },
            isError = lastNameError != null,
            supportingText = lastNameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = null
            },
            label = { Text("Username") },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                phoneError = null
            },
            label = { Text("Phone number") },
            isError = phoneError != null,
            supportingText = phoneError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(if (profileBitmap != null) "Image selected ✓" else "Choose profile image")
        }

        Button(
            onClick = {
                val trimmedUsername     = username.trim()
                val trimmedFirstName    = firstName.trim()
                val trimmedLastName     = lastName.trim()
                val trimmedEmail        = email.trim()
                val trimmedPassword     = password.trim()
                val trimmedPhone        = phone.trim()

                emailError      = null
                passwordError   = null
                phoneError      = null

                var hasError = false

                if (trimmedFirstName.isEmpty()) {
                    emailError = "Enter name"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    emailError = "Email format is not valid"
                    hasError = true
                }

                if (trimmedLastName.isEmpty()) {
                    emailError = "Enter surname"
                    hasError = true
                }

                if (trimmedEmail.isEmpty()) {
                    emailError = "Enter email"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    emailError = "Email format is not valid"
                    hasError = true
                }

                if (trimmedPassword.isEmpty()) {
                    passwordError = "Enter password"
                    hasError = true
                } else if (trimmedPassword.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    hasError = true
                }

                if (trimmedPhone.isEmpty()) {
                    phoneError = "Enter phone number"
                    hasError = true
                }

                if (!hasError) {
                    onRegisterClick(trimmedUsername, trimmedFirstName, trimmedLastName, trimmedEmail, trimmedPassword, trimmedPhone, profileBitmap)
                } else {
                    Toast.makeText(context, "Please enter all valid input", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Register")
        }

        TextButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Already have an account? Login!")
        }
    }
}

@Composable
fun PlaceOwnerRegisterForm(
    onRegisterClick: (businessName: String, email: String, password: String, phone: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false
) {
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var businessNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Place Owner Registration", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = businessName,
            onValueChange = {
                businessName = it
                businessNameError = null
            },
            label = { Text("Business Name") },
            isError = businessNameError != null,
            supportingText = businessNameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                phoneError = null
            },
            label = { Text("Phone Number") },
            isError = phoneError != null,
            supportingText = phoneError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        )

        Button(
            onClick = {
                val trimmedBusinessName = businessName.trim()
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()
                val trimmedPhone = phone.trim()

                businessNameError = null
                emailError = null
                passwordError = null
                phoneError = null

                var hasError = false

                if (trimmedBusinessName.isEmpty()) {
                    businessNameError = "Enter business name"
                    hasError = true
                }

                if (trimmedEmail.isEmpty()) {
                    emailError = "Enter email"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    emailError = "Email format is not valid"
                    hasError = true
                }

                if (trimmedPassword.isEmpty()) {
                    passwordError = "Enter password"
                    hasError = true
                } else if (trimmedPassword.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    hasError = true
                }

                if (trimmedPhone.isEmpty()) {
                    phoneError = "Enter phone number"
                    hasError = true
                }

                if (!hasError) {
                    onRegisterClick(trimmedBusinessName, trimmedEmail, trimmedPassword, trimmedPhone)
                } else {
                    Toast.makeText(context, "Please enter all valid input", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Register Place Owner")
        }

        TextButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Already have an account? Login!")
        }
    }
}
