
package com.sanaesencia.app.view.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.view.components.AppointmentCard
import com.sanaesencia.app.viewmodel.BookingViewModel

// ==========================================
// Pantalla de Inicio / Dashboard de Resumen (Citas y Novedades)
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: BookingViewModel,
    role: String = "PACIENTE"
) {
    val context = LocalContext.current
    val appointments by viewModel.appointments.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Inicio y Novedades", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("¡Bienvenido a Centro Sanaesencia!", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Explora nuestras especialidades, agenda horas médicas presenciales en Los Vilos u online, e infórmate de nuestras novedades.")
                }
            }
        }

        item {
            Text("Tus Próximas Citas Agendadas", style = MaterialTheme.typography.titleLarge)
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No tienes citas agendadas aún. Presiona el botón '+' para reservar una hora.",
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

