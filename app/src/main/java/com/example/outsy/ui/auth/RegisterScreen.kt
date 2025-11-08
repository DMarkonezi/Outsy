package com.example.outsy.ui.auth

import android.graphics.Bitmap
import android.provider.MediaStore
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            onRegisterClick = {_, _, _, _, _, _-> },
            onNavigateToLogin = {}
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (firstName: String, lastName: String, email: String, password: String, phone: String, profileBitmap: Bitmap?) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // State za prikazivanje grešaka
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
                val trimmedFirstName = firstName.trim()
                val trimmedLastName = lastName.trim()
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()
                val trimmedPhone = phone.trim()

                emailError = null
                passwordError = null
                phoneError = null

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
                    onRegisterClick(trimmedFirstName, trimmedLastName, trimmedEmail, trimmedPassword, trimmedPhone, profileBitmap)
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