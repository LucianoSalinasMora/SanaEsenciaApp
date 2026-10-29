package com.sanaesencia.app.view.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.util.Validators
import com.sanaesencia.app.view.components.AppointmentCard
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Perfil de Usuario y Datos Personales
// ==========================================
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val appointments by bookingViewModel.appointments.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }

    val isNameValid = remember(name) { name.isBlank() || Validators.isValidName(name) }
    val isEmailValid = remember(email) { email.isBlank() || Validators.isValidEmail(email) }
    val isPhoneValid = remember(phone) { phone.isBlank() || Validators.isValidPhone(phone) }

    val isFormValid = name.isNotBlank() && isNameValid &&
            email.isNotBlank() && isEmailValid &&
            phone.isNotBlank() && isPhoneValid

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Perfil de Usuario", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(currentUser?.name ?: "Usuario", style = MaterialTheme.typography.titleMedium)
                                Text("RUN: ${currentUser?.run ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                Text("Rol: ${currentUser?.role?.name ?: "PACIENTE"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(if (isEditing) Icons.Default.Save else Icons.Default.Edit, contentDescription = "Editar Perfil")
                        }
                    }

                    HorizontalDivider()

                    if (!isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Correo Electrónico: ${currentUser?.email?.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium)
                            Text("Teléfono / WhatsApp: ${currentUser?.phone?.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium)
                            if (!currentUser?.specialty.isNullOrBlank()) {
                                Text("Especialidad: ${currentUser?.specialty}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    } else {
                        Text("Editar Datos Personales", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

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
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            isError = !isEmailValid,
                            supportingText = { if (!isEmailValid) Text("Formato correo inválido (ej. usuario@dominio.com)", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono de Contacto") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            isError = !isPhoneValid,
                            supportingText = { if (!isPhoneValid) Text("Número inválido (8 a 12 dígitos)", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                userViewModel.updateUserProfile(name.trim(), email.trim(), phone.trim()) {
                                    isEditing = false
                                    Toast.makeText(context, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isFormValid,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }

        item {
            Text("Historial de Citas Registradas", style = MaterialTheme.typography.titleLarge)
        }

        if (appointments.isEmpty()) {
            item {
                Text("No hay registros de citas guardadas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            items(appointments) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onCancel = {
                        bookingViewModel.cancelAppointment(appointment.id) {
                            Toast.makeText(context, "Cita cancelada exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

