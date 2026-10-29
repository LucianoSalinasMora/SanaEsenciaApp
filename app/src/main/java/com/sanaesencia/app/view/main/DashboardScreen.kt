
package com.sanaesencia.app.view.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.data.model.isCancelled
import com.sanaesencia.app.view.components.AppointmentCard
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel
import java.time.LocalDate

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
    val appointments by viewModel.appointments.collectAsState()
    val currentUser by userViewModel?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }

    val visibleAppointments = remember(appointments, currentUser, role) {
        val user = currentUser
        val roleFiltered = when (role) {
            UserRole.PACIENTE.name -> appointments.filter { appt ->
                if (user == null) false
                else {
                    (user.run.isNotBlank() && appt.patientRun.equals(user.run, ignoreCase = true)) ||
                    (user.email.isNotBlank() && appt.patientEmail.equals(user.email, ignoreCase = true)) ||
                    (user.name.isNotBlank() && appt.patientName.equals(user.name, ignoreCase = true))
                }
            }
            UserRole.PROFESIONAL.name -> appointments.filter { appt ->
                if (user == null) false
                else {
                    appt.professionalName.equals(user.name, ignoreCase = true) ||
                    (user.run.isNotBlank() && appt.professionalRun.equals(user.run, ignoreCase = true))
                }
            }
            else -> appointments // ADMIN ve todas
        }

        val today = LocalDate.now()
        roleFiltered.filter { appt ->
            if (!appt.isCancelled) {
                true
            } else {
                try {
                    val apptDate = LocalDate.parse(appt.date)
                    // Desaparece después de 1 día de la fecha agendada
                    !apptDate.plusDays(1).isBefore(today)
                } catch (e: Exception) {
                    false
                }
            }
        }.sortedWith(compareBy({ it.date }, { it.timeSlot }))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = when (role) {
                    UserRole.PROFESIONAL.name -> "Panel Profesional"
                    UserRole.ADMIN.name -> "Panel de Administración"
                    else -> "Inicio y Novedades"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (role == UserRole.ADMIN.name || role == UserRole.PROFESIONAL.name) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (role == UserRole.ADMIN.name)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (role == UserRole.ADMIN.name) Icons.Default.ManageAccounts else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (role == UserRole.ADMIN.name) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (role == UserRole.ADMIN.name) "Panel Administrador" else "Cuenta Profesional",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (role == UserRole.ADMIN.name) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    val titleText = when (role) {
                        UserRole.PROFESIONAL.name -> if (!currentUser?.name.isNullOrBlank()) "¡Hola, ${currentUser?.name}!" else "Portal de Atención Profesional"
                        UserRole.ADMIN.name -> "Gestión Centralizada del Centro"
                        else -> "¡Bienvenido a Centro Sanaesencia!"
                    }

                    val descriptionText = when (role) {
                        UserRole.PROFESIONAL.name -> "Accede rápidamente a tus turnos asignados, revisa las notas de atención de tus pacientes y mantén tu agenda coordinada."
                        UserRole.ADMIN.name -> "Supervisa la ocupación de boxes en tiempo real, controla las citas y administra el equipo de especialistas y catálogo."
                        else -> "Explora nuestras especialidades, agenda horas médicas presenciales en Los Vilos u online, e infórmate de nuestras novedades."
                    }

                    Text(titleText, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(descriptionText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text(
                text = if (role == UserRole.PROFESIONAL.name) "Tus Citas Asignadas" else "Tus Próximas Citas Agendadas",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (visibleAppointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = when (role) {
                            UserRole.PROFESIONAL.name -> "No tienes citas agendadas con pacientes aún."
                            UserRole.ADMIN.name -> "No hay citas registradas en el sistema aún."
                            else -> "No tienes citas agendadas aún. Presiona el botón '+' para reservar una hora."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(visibleAppointments) { appointment ->
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

