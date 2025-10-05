package com.example.outsy.ui.auth

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
            onRegisterClick = {_, _, _, _-> },
            onNavigateToLogin = {}
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, android.graphics.Bitmap?) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // State za prikazivanje grešaka
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
        Text("Registracija", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null // Resetuj grešku kad korisnik kuca
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
            label = { Text("Lozinka") },
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
            label = { Text("Broj telefona") },
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
            Text(if (profileBitmap != null) "Slika izabrana ✓" else "Izaberi profilnu sliku")
        }

        Button(
            onClick = {
                // Trimuj sve vrednosti
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()
                val trimmedPhone = phone.trim()

                // Resetuj sve greške
                emailError = null
                passwordError = null
                phoneError = null

                var hasError = false

                // Validacija email-a
                if (trimmedEmail.isEmpty()) {
                    emailError = "Unesite email"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    emailError = "Email nije validan"
                    hasError = true
                }

                // Validacija lozinke
                if (trimmedPassword.isEmpty()) {
                    passwordError = "Unesite lozinku"
                    hasError = true
                } else if (trimmedPassword.length < 6) {
                    passwordError = "Lozinka mora imati minimum 6 karaktera"
                    hasError = true
                }

                // Validacija telefona
                if (trimmedPhone.isEmpty()) {
                    phoneError = "Unesite broj telefona"
                    hasError = true
                }

                // Ako nema grešaka, pozovi registraciju
                if (!hasError) {
                    onRegisterClick(trimmedEmail, trimmedPassword, trimmedPhone, profileBitmap)
                } else {
                    Toast.makeText(context, "Molimo popunite sva polja ispravno", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Registruj se")
        }

        TextButton(
            onClick = { onNavigateToLogin() },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Već imate nalog? Ulogujte se")
        }
    }
}