package com.sanaesencia.app.view.professional

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel
import java.time.LocalDate

// ==========================================
// Pantalla de Turnos y Citas del Profesional Conectado
// ==========================================
@Composable
fun MyScheduleScreen(
    bookingViewModel: BookingViewModel,
    userViewModel: UserViewModel
) {
    // ------------------------------------------
    // Observación y Filtrado de Citas Asignadas al Profesional
    // ------------------------------------------
    val allAppointments by bookingViewModel.appointments.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()

    val hoy = LocalDate.now().toString() // formato yyyy-MM-dd, igual que las citas
    val proximas = remember(allAppointments, currentUser, hoy) {
        val user = currentUser
        if (user == null) emptyList()
        else {
            allAppointments
                .filter {
                    (it.professionalName.equals(user.name, ignoreCase = true) ||
                     (user.run.isNotBlank() && it.professionalRun.equals(user.run, ignoreCase = true))) &&
                    it.date >= hoy
                }
                .sortedWith(compareBy({ it.date }, { it.timeSlot }))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Turnos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Sus próximas citas asignadas, ordenadas por fecha y hora.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (proximas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No tiene citas próximas asignadas.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(proximas) { cita ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = cita.patientName.ifBlank { "Paciente Registrado" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cita.date, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cita.timeSlot, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Servicio: ${cita.serviceType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (cita.isOnline) "Modalidad: Online (MEDIlink)"
                                else "Modalidad: Presencial${cita.boxNumber?.let { " - Box $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}