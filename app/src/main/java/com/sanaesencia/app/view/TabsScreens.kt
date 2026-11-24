package com.sanaesencia.app.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.sanaesencia.app.R
import com.sanaesencia.app.data.AppointmentEntity
import com.sanaesencia.app.data.ContactHelper
import com.sanaesencia.app.data.NosotrosManager
import com.sanaesencia.app.data.NotificationHelper
import com.sanaesencia.app.data.UserEntity
import com.sanaesencia.app.data.UserRole
import com.sanaesencia.app.data.Validators
import com.sanaesencia.app.data.formatDisplayDate
import com.sanaesencia.app.data.isCancelled
import com.sanaesencia.app.data.mockProfessionals
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Inicio / Dashboard de Resumen (Citas y Novedades)
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: BookingViewModel,
    userViewModel: UserViewModel? = null,
    role: String = "PACIENTE"
) {
    val context = LocalContext.current
    val currentUser by userViewModel?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
    val allAppointments by viewModel.appointments.collectAsState()

    val appointments = remember(allAppointments, currentUser, role) {
        val user = currentUser
        if (user == null || role.equals("ADMIN", ignoreCase = true)) {
            allAppointments
        } else if (role.equals("PROFESIONAL", ignoreCase = true)) {
            allAppointments.filter { cita ->
                val cleanUserRun = user.run.replace(".", "").replace("-", "").uppercase()
                val cleanProRun = cita.professionalRun.replace(".", "").replace("-", "").uppercase()
                cleanProRun == cleanUserRun || cita.professionalName.equals(user.name, ignoreCase = true)
            }
        } else {
            // PACIENTE: solo ver SUS citas
            allAppointments.filter { cita ->
                val cleanUserRun = user.run.replace(".", "").replace("-", "").uppercase()
                val cleanPatientRun = cita.patientRun.replace(".", "").replace("-", "").uppercase()
                val matchRun = cleanPatientRun.isNotBlank() && cleanPatientRun == cleanUserRun
                val matchName = cita.patientName.isNotBlank() && cita.patientName.equals(user.name, ignoreCase = true)
                matchRun || matchName
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val headerTitle = when (role.uppercase()) {
            "ADMIN" -> "Panel de Administración"
            "PROFESIONAL" -> "Panel del Profesional"
            else -> "Inicio y Novedades"
        }

        val welcomeCardTitle = when (role.uppercase()) {
            "ADMIN" -> "¡Bienvenido, Administrador!"
            "PROFESIONAL" -> "¡Bienvenido, ${currentUser?.name ?: "Profesional"}!"
            else -> "¡Bienvenido a Centro Sanaesencia!"
        }

        val welcomeCardDesc = when (role.uppercase()) {
            "ADMIN" -> "Supervisa la ocupación de los boxes de atención, administra las citas globales y gestiona al equipo del centro."
            "PROFESIONAL" -> "Consulta tus citas asignadas, revisa el contexto clínico de tus pacientes y administra tus turnos."
            else -> "Explora nuestras especialidades, agenda horas médicas presenciales en Los Vilos u online, e infórmate de nuestras novedades."
        }

        val appointmentsTitle = when (role.uppercase()) {
            "ADMIN" -> "Todas las Citas del Centro"
            "PROFESIONAL" -> "Tus Citas Asignadas"
            else -> "Tus Próximas Citas Agendadas"
        }

        val emptyText = when (role.uppercase()) {
            "ADMIN" -> "No hay citas registradas en el sistema actualmente."
            "PROFESIONAL" -> "No tienes citas agendadas con pacientes en este momento."
            else -> "No tienes citas agendadas aún. Presiona el botón '+' para reservar una hora."
        }

        item {
            Text(headerTitle, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(welcomeCardTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(welcomeCardDesc)
                }
            }
        }

        item {
            Text(appointmentsTitle, style = MaterialTheme.typography.titleLarge)
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = emptyText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(appointments) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onCancel = {
                        viewModel.cancelAppointment(appointment.id) {
                            Toast.makeText(context, "Cita cancelada exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

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

    val patientAppointments = remember(appointments, currentUser) {
        val user = currentUser
        if (user == null) emptyList()
        else {
            appointments.filter { cita ->
                val cleanUserRun = user.run.replace(".", "").replace("-", "").uppercase()
                val cleanPatientRun = cita.patientRun.replace(".", "").replace("-", "").uppercase()
                val matchRun = cleanPatientRun.isNotBlank() && cleanPatientRun == cleanUserRun
                val matchName = cita.patientName.isNotBlank() && cita.patientName.equals(user.name, ignoreCase = true)
                matchRun || matchName
            }
        }
    }

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

        if (patientAppointments.isEmpty()) {
            item {
                Text("No hay registros de citas guardadas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            items(patientAppointments) { appointment ->
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

// ==========================================
// Componente de Tarjeta de Cita Médica (Con Acciones y Detalle Expandible)
// ==========================================
@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    onCancel: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var mostrarConfirmacionCancelar by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    val isCancelled = appointment.isCancelled

    val animatedBg by animateColorAsState(
        targetValue = when {
            isCancelled -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            isExpanded -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 280),
        label = "cardBgColor"
    )

    if (mostrarConfirmacionCancelar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCancelar = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Cancelación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cancelar la cita de ${appointment.serviceType} con ${appointment.professionalName} agendada para el ${formatDisplayDate(appointment.date)} a las ${appointment.timeSlot}?\n\nAl cancelarla, desaparecerá de tus citas agendadas y el horario volverá a quedar disponible para otros pacientes."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionCancelar = false
                        onCancel?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, cancelar cita")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionCancelar = false }) {
                    Text("No, mantener cita")
                }
            }
        )
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Eliminar Registro",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar definitivamente este registro de cita de la base de datos?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionEliminar = false
                        onDelete?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 280))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.elevatedCardColors(containerColor = animatedBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceType,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isCancelled) {
                    AssistChip(
                        onClick = { isExpanded = !isExpanded },
                        label = { Text("Cancelada", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                } else {
                    AssistChip(
                        onClick = { isExpanded = !isExpanded },
                        label = { Text(if (appointment.status.isBlank() || appointment.status.equals("Cancelada", ignoreCase = true)) "Agendada" else appointment.status) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (isCancelled) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cita Cancelada - El box y horario fueron liberados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paciente: ${appointment.patientName} • Atiende: ${appointment.professionalName}", style = MaterialTheme.typography.bodyMedium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fecha: ${formatDisplayDate(appointment.date)}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hora: ${appointment.timeSlot}", style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                text = if (appointment.isOnline) "Modalidad: Online (MEDIlink)" else "Modalidad: Presencial (Caupolicán 220, Los Vilos ${appointment.boxNumber?.let { "- Box $it" } ?: ""})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (appointment.patientNotes.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nota/Motivo: ${appointment.patientNotes}", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (!isCancelled && onCancel != null) {
                    Button(
                        onClick = { mostrarConfirmacionCancelar = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancelar Cita", fontWeight = FontWeight.Bold)
                    }
                }
                if (isCancelled && onDelete != null) {
                    OutlinedButton(
                        onClick = { mostrarConfirmacionEliminar = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar Registro de la Lista", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ==========================================
// Pestaña de Catálogo (Placeholder / Vista General)
// ==========================================
@Composable
fun CatalogTabScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Catálogo de Servicios", style = MaterialTheme.typography.headlineMedium)
    }
}

// ==========================================
// Pestaña de Profesionales (Lista General)
// ==========================================
@Composable
fun ProfessionalsScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(mockProfessionals) { pro ->
            Text(pro.name)
        }
    }
}

// ==========================================
// Pantalla Institucional "Nosotros" y Ubicación en Mapa
// ==========================================
@Composable
fun AboutScreen(role: String = "PACIENTE") {
    val context = LocalContext.current
    val nosotrosInfo by NosotrosManager.infoState.collectAsState()

    LaunchedEffect(Unit) {
        NosotrosManager.init(context)
    }

    val mapsUrl = "https://maps.app.goo.gl/3hFvN2dRbjQkc5vq5"

    fun openGoogleMaps() {
        val intent = Intent(Intent.ACTION_VIEW, mapsUrl.toUri())
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir la aplicación de mapas", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Nosotros",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Centro Sanaesencia",
                        modifier = Modifier.height(42.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = nosotrosInfo.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = nosotrosInfo.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // --- TARJETA DE UBICACIÓN Y MAPA INTERACTIVO ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openGoogleMaps() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_map_preview),
                            contentDescription = "Mapa Ubicación Sanaesencia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Abrir Google Maps",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Abrir Maps",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ubicación Presencial",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = nosotrosInfo.address,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Toca la imagen o presiona el botón para abrir la ruta en Google Maps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { openGoogleMaps() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Ubicación en Google Maps")
                        }
                    }
                }
            }
        }

        // --- CONTACTO Y HORARIOS ---
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Información de Atención",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contacto / WhatsApp: ${nosotrosInfo.phone}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Correo: ${nosotrosInfo.email}", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Horario: ${nosotrosInfo.schedule}", style = MaterialTheme.typography.bodyMedium)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ContactHelper.openWhatsApp(
                                    context = context,
                                    rawPhone = nosotrosInfo.phone,
                                    message = "Hola Centro Sanaesencia, me gustaría solicitar información."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chatear por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ContactHelper.addContact(
                                    context = context,
                                    name = nosotrosInfo.title,
                                    phone = nosotrosInfo.phone,
                                    email = nosotrosInfo.email,
                                    address = nosotrosInfo.address
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Contacto")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Función Auxiliar: Filtrado de Citas según Rol del Usuario
// ==========================================
// Cada usuario ve solo las citas que le corresponden (el admin ve todas), excluyendo las canceladas
private fun filtrarCitasPorUsuario(
    citas: List<AppointmentEntity>,
    user: UserEntity?
): List<AppointmentEntity> {
    if (user == null) return emptyList()
    val activas = citas.filter { !it.isCancelled }
    return when (user.role) {
        UserRole.PACIENTE -> activas.filter { 
            (user.run.isNotBlank() && it.patientRun.equals(user.run, ignoreCase = true)) || 
            (user.email.isNotBlank() && it.patientEmail.equals(user.email, ignoreCase = true)) ||
            (user.name.isNotBlank() && it.patientName.equals(user.name, ignoreCase = true))
        }
        UserRole.PROFESIONAL -> activas.filter { 
            it.professionalName.equals(user.name, ignoreCase = true) ||
            (user.run.isNotBlank() && it.professionalRun.equals(user.run, ignoreCase = true))
        }
        UserRole.ADMIN -> activas
    }
}
