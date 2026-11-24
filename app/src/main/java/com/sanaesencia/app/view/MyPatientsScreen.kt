package com.sanaesencia.app.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Pacientes Agendados (Vista Profesional)
// ==========================================
@Composable
fun MyPatientsScreen(
    bookingViewModel: BookingViewModel,
    userViewModel: UserViewModel? = null
) {
    // ------------------------------------------
    // Observación de Citas de Pacientes asignadas al Profesional
    // ------------------------------------------
    val appointments by bookingViewModel.appointments.collectAsState()
    val currentUser by userViewModel?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }

    val myAppointments = remember(appointments, currentUser) {
        val user = currentUser
        if (user == null) {
            appointments
        } else {
            appointments.filter { cita ->
                val cleanUserRun = user.run.replace(".", "").replace("-", "").uppercase()
                val cleanProRun = cita.professionalRun.replace(".", "").replace("-", "").uppercase()
                cleanProRun == cleanUserRun || cita.professionalName.equals(user.name, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Panel de Pacientes Agendados",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Revise el contexto y notas previas ingresadas por sus pacientes antes de la consulta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (myAppointments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No tiene pacientes agendados actualmente.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myAppointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCancel = { bookingViewModel.cancelAppointment(appointment.id) }
                    )
                }
            }
        }
    }
}
