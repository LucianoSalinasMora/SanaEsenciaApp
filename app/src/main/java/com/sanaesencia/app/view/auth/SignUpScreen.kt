package com.sanaesencia.app.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Registro de Nuevos Pacientes
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: UserViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // ------------------------------------------
    // Estados y Validaciones del Formulario de Registro
    // ------------------------------------------
    val state by viewModel.signUpFormState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Paciente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ingrese sus datos personales para crear su cuenta de Paciente:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mensaje de error general si ya existe el usuario
            state.errorMessage?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Campo RUN con leadingIcon
            OutlinedTextField(
                value = state.run,
                onValueChange = { viewModel.onSignUpRunChanged(it) },
                label = { Text("RUN (Ej: 12345678-9)") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                isError = !state.isRunValid,
                supportingText = {
                    if (!state.isRunValid) Text("RUN chileno inválido", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Nombre Completo con leadingIcon
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onSignUpNameChanged(it) },
                label = { Text("Nombre Completo") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                isError = !state.isNameValid,
                supportingText = {
                    if (!state.isNameValid) Text("Mínimo 3 caracteres", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Correo Electrónico con leadingIcon
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onSignUpEmailChanged(it) },
                label = { Text("Correo Electrónico (ej. usuario@dominio.com)") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                isError = !state.isEmailValid,
                supportingText = {
                    if (!state.isEmailValid) Text("Formato de correo inválido", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Teléfono con leadingIcon
            OutlinedTextField(
                value = state.phone,
                onValueChange = { viewModel.onSignUpPhoneChanged(it) },
                label = { Text("Teléfono (+56912345678)") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                isError = !state.isPhoneValid,
                supportingText = {
                    if (!state.isPhoneValid) Text("Número de teléfono inválido (8 a 12 dígitos)", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Contraseña con leadingIcon y toggle de visibilidad
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onSignUpPasswordChanged(it) },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                isError = !state.isPasswordValid,
                supportingText = {
                    if (!state.isPasswordValid) Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Recuperación de cuenta", style = MaterialTheme.typography.titleSmall)

            // Campo Pregunta de Seguridad con leadingIcon
            OutlinedTextField(
                value = state.securityQuestion,
                onValueChange = { viewModel.onSignUpSecurityQuestionChanged(it) },
                label = { Text("Pregunta de Seguridad") },
                leadingIcon = {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Respuesta de Seguridad con leadingIcon
            OutlinedTextField(
                value = state.securityAnswer,
                onValueChange = { viewModel.onSignUpSecurityAnswerChanged(it) },
                label = { Text("Respuesta de Seguridad") },
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                isError = !state.isSecurityAnswerValid,
                supportingText = {
                    if (!state.isSecurityAnswerValid) Text("Ingrese una respuesta válida (mínimo 2 caracteres)", color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.submitSignUp(onSuccess = onRegisterSuccess)
                },
                enabled = state.isFormValid && !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Registrarse como Paciente")
                }
            }
        }
    }
}
