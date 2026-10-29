package com.sanaesencia.app.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.R
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.data.util.Validators
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Inicio de Sesión (Login)
// ==========================================
@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    // ------------------------------------------
    // Estado y Validación del Formulario de Inicio de Sesión
    // ------------------------------------------
    val state by viewModel.loginFormState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ------------------------------------------
        // Identidad Visual y Bienvenida
        // ------------------------------------------
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Sanaesencia",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = "Bienvenido a Sanaesencia",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ------------------------------------------
        // Campos de Entrada: RUN / Usuario y Contraseña
        // ------------------------------------------
        OutlinedTextField(
            value = state.run,
            onValueChange = { viewModel.onLoginRunChanged(it) },
            label = { Text("RUN / Usuario") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isError = !state.isRunValid,
            supportingText = {
                if (!state.isRunValid) {
                    Text("RUN o Usuario inválido (ej. 12345678-9 o admin)", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Campo Contraseña con leadingIcon, toggle de visibilidad y validación centralizada
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onLoginPasswordChanged(it) },
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.isFormValid && !state.isLoading) {
                        viewModel.submitLogin(onSuccess = onLoginSuccess)
                    }
                }
            ),
            isError = !state.isPasswordValid,
            supportingText = {
                if (!state.isPasswordValid) {
                    Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ------------------------------------------
        // Botón Principal de Envío / Inicio de Sesión
        // ------------------------------------------
        Button(
            onClick = { viewModel.submitLogin(onSuccess = onLoginSuccess) },
            enabled = state.isFormValid && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validando credenciales...")
            } else {
                Text("Ingresar")
            }
        }

        // ------------------------------------------
        // Enlaces de Navegación a Recuperación y Registro
        // ------------------------------------------
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToForgotPassword) {
            Text("¿Olvidaste tu contraseña?")
        }
        TextButton(onClick = onNavigateToSignUp) {
            Text("¿No tienes cuenta? Regístrate como Paciente")
        }
    }
}
