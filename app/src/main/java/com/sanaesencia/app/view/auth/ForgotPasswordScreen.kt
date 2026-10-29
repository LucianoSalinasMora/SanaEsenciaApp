package com.sanaesencia.app.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Recuperación de Contraseña (3 Pasos)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit,
    onPasswordResetSuccess: () -> Unit
) {
    // Control de paso actual (1: RUN, 2: Pregunta Seguridad, 3: Nueva Clave)
    var step by remember { mutableIntStateOf(1) }

    var run by remember { mutableStateOf("") }
    var runError by remember { mutableStateOf(false) }

    var fetchedUser by remember { mutableStateOf<UserEntity?>(null) }
    var answer by remember { mutableStateOf("") }
    var answerError by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ------------------------------------------
            // Paso 1: Búsqueda de Usuario por RUN
            // ------------------------------------------
            // --- PASO 1: Ingresar RUN ---
            OutlinedTextField(
                value = run,
                onValueChange = { run = it.replace("\n", "").replace("\r", ""); runError = false },
                isError = runError,
                label = { Text("Ingresa tu RUN") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                maxLines = 1,
                supportingText = { if (runError) Text("Usuario no encontrado", color = MaterialTheme.colorScheme.error) },
                enabled = step == 1,
                modifier = Modifier.fillMaxWidth()
            )

            if (step == 1) {
                Button(
                    onClick = {
                        viewModel.getUserByRun(run.trim()) { user ->
                            if (user != null) {
                                fetchedUser = user
                                step = 2
                            } else {
                                runError = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Buscar Usuario") }
            }

            // ------------------------------------------
            // Paso 2: Validación de la Pregunta de Seguridad
            // ------------------------------------------
            // --- PASO 2: Pregunta de Seguridad ---
            if (step >= 2 && fetchedUser != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Text("Pregunta de seguridad:", color = MaterialTheme.colorScheme.primary)
                Text(fetchedUser!!.securityQuestion, style = MaterialTheme.typography.bodyLarge)

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it.replace("\n", "").replace("\r", ""); answerError = false },
                    label = { Text("Tu respuesta") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    maxLines = 1,
                    isError = answerError,
                    supportingText = { if (answerError) Text("Respuesta incorrecta", color = MaterialTheme.colorScheme.error) },
                    enabled = step == 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (step == 2) {
                    Button(
                        onClick = {
                            if (answer.trim().equals(fetchedUser!!.securityAnswer.trim(), ignoreCase = true)) {
                                step = 3
                            } else {
                                answerError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Verificar Respuesta") }
                }
            }

            // ------------------------------------------
            // Paso 3: Asignación y Guardado de Nueva Contraseña
            // ------------------------------------------
            // --- PASO 3: Nueva Contraseña ---
            if (step == 3) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it.replace("\n", "").replace("\r", "") },
                    label = { Text("Nueva Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    maxLines = 1,
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = {
                        if (newPassword.isNotBlank() && newPassword.length < 4) {
                            Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.updatePassword(run.trim(), newPassword.trim()) {
                            onPasswordResetSuccess()
                        }
                    },
                    enabled = newPassword.trim().length >= 4,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Guardar y Volver") }
            }
        }
    }
}