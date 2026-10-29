package com.sanaesencia.app.view.professional

import com.sanaesencia.app.view.components.AppointmentCard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel

import com.sanaesencia.app.data.model.isCancelled
import java.time.LocalDate

// ==========================================
// Pantalla de Pacientes Agendados (Vista Profesional)
// ==========================================
@Composable
fun MyPatientsScreen(
    bookingViewModel: BookingViewModel,
    userViewModel: UserViewModel? = null
) {
    // ------------------------------------------
    // Observación de Citas de Pacientes
    // ------------------------------------------
    val appointments by bookingViewModel.appointments.collectAsState()
    val currentUser by userViewModel?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }

    val today = LocalDate.now()
    val myAppointments = remember(appointments, currentUser) {
        val user = currentUser
        val base = if (user == null) {
            appointments
        } else {
            appointments.filter { appt ->
                appt.professionalName.equals(user.name, ignoreCase = true) ||
                (user.run.isNotBlank() && appt.professionalRun.equals(user.run, ignoreCase = true))
            }
        }

        base.filter { appt ->
            if (!appt.isCancelled) {
                true
            } else {
                try {
                    val apptDate = LocalDate.parse(appt.date)
                    !apptDate.plusDays(1).isBefore(today)
                } catch (e: Exception) {
                    false
                }
            }
        }.sortedWith(compareBy({ it.date }, { it.timeSlot }))
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
                    text = "No hay pacientes agendados actualmente para sus consultas.",
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
