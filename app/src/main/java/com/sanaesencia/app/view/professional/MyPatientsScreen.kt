package com.sanaesencia.app.view.professional

import com.sanaesencia.app.view.components.AppointmentCard

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

// ==========================================
// Pantalla de Pacientes Agendados (Vista Profesional)
// ==========================================
@Composable
fun MyPatientsScreen(bookingViewModel: BookingViewModel) {
    // ------------------------------------------
    // Observación de Citas de Pacientes
    // ------------------------------------------
    val appointments by bookingViewModel.appointments.collectAsState()

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

        if (appointments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No hay pacientes agendados actualmente.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onCancel = { bookingViewModel.cancelAppointment(appointment.id) }
                    )
                }
            }
        }
    }
}
