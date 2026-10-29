package com.sanaesencia.app.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.data.util.Validators
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Administración y Gestión de Profesionales
// ==========================================
@Composable
fun AdminProfessionalsScreen(viewModel: UserViewModel) {
    val dbProfessionals by viewModel.professionals.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProfessional by remember { mutableStateOf<UserEntity?>(null) }

    // ------------------------------------------
    // Encabezado y Botón de Nuevo Profesional
    // ------------------------------------------
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Panel de Gestión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                Text("Profesionales del Centro", style = MaterialTheme.typography.headlineSmall)
            }
            Button(
                onClick = {
                    editingProfessional = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------------------------------
        // Lista de Tarjetas de Profesionales o Estado Vacío
        // ------------------------------------------
        if (dbProfessionals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No hay profesionales registrados aún. Presione el botón 'Nuevo' para registrar credenciales de un profesional.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dbProfessionals) { pro ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(pro.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Especialidad: ${pro.specialty.ifBlank { "General" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text("RUN: ${pro.run} • ${pro.email}", style = MaterialTheme.typography.bodySmall)
                                    Text("Tel: ${pro.phone}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    editingProfessional = pro
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteProfessional(pro.run) {} }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------
    // Diálogo Modal para Crear o Editar Profesional
    // ------------------------------------------
    if (showDialog) {
        AddOrEditProfessionalDialog(
            professionalToEdit = editingProfessional,
            onDismiss = { showDialog = false },
            onSave = { pro ->
                viewModel.registerProfessional(pro) {
                    showDialog = false
                }
            }
        )
    }
}

// ==========================================
// Diálogo de Formulario: Agregar o Editar Profesional
// ==========================================
@Composable
fun AddOrEditProfessionalDialog(
    professionalToEdit: UserEntity?,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var run by remember { mutableStateOf(professionalToEdit?.run ?: "") }
    var name by remember { mutableStateOf(professionalToEdit?.name ?: "") }
    var specialty by remember { mutableStateOf(professionalToEdit?.specialty ?: "") }
    var email by remember { mutableStateOf(professionalToEdit?.email ?: "") }
    var phone by remember { mutableStateOf(professionalToEdit?.phone ?: "") }
    var password by remember { mutableStateOf(professionalToEdit?.passwordHash ?: "") }

    val isRunValid = remember(run) { run.isBlank() || Validators.isValidRUN(run) }
    val isNameValid = remember(name) { name.isBlank() || Validators.isValidName(name) }
    val isSpecialtyValid = remember(specialty) { specialty.isBlank() || Validators.isValidSpecialty(specialty) }
    val isEmailValid = remember(email) { email.isBlank() || Validators.isValidEmail(email) }
    val isPhoneValid = remember(phone) { phone.isBlank() || Validators.isValidPhone(phone) }
    val isPasswordValid = remember(password) { password.isBlank() || Validators.isValidPassword(password) }

    val isFormValid = run.isNotBlank() && isRunValid &&
            name.isNotBlank() && isNameValid &&
            specialty.isNotBlank() && isSpecialtyValid &&
            email.isNotBlank() && isEmailValid &&
            phone.isNotBlank() && isPhoneValid &&
            password.isNotBlank() && isPasswordValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (professionalToEdit == null) "Registrar Nuevo Profesional" else "Editar Profesional") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = run,
                    onValueChange = { if (professionalToEdit == null) run = it },
                    enabled = professionalToEdit == null,
                    label = { Text("RUN del Profesional") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = !isRunValid,
                    supportingText = { if (!isRunValid) Text("RUN inválido", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = !isNameValid,
                    supportingText = { if (!isNameValid) Text("Mínimo 3 caracteres", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad médica") },
                    leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = !isSpecialtyValid,
                    supportingText = { if (!isSpecialtyValid) Text("Campo obligatorio", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = !isEmailValid,
                    supportingText = { if (!isEmailValid) Text("Formato correo inválido", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Contacto") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = !isPhoneValid,
                    supportingText = { if (!isPhoneValid) Text("Número de teléfono inválido", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = !isPasswordValid,
                    supportingText = { if (!isPasswordValid) Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        UserEntity(
                            run = run.trim(),
                            name = name.trim(),
                            specialty = specialty.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            passwordHash = password.trim(),
                            role = UserRole.PROFESIONAL,
                            securityQuestion = professionalToEdit?.securityQuestion ?: "Especialidad",
                            securityAnswer = professionalToEdit?.securityAnswer ?: specialty.trim()
                        )
                    )
                },
                enabled = isFormValid
            ) {
                Text(if (professionalToEdit == null) "Guardar Profesional" else "Actualizar Profesional")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
