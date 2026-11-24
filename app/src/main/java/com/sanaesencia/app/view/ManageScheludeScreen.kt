package com.sanaesencia.app.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.AppointmentEntity
import com.sanaesencia.app.data.isCancelled
import com.sanaesencia.app.viewmodel.BookingViewModel
import java.time.LocalDate
import java.time.LocalTime

// ==========================================
// Pantalla de Gestión de Citas y Ocupación de Boxes (Administrador)
// ==========================================
@Composable
fun ManageScheduleScreen(bookingViewModel: BookingViewModel) {
    // ------------------------------------------
    // Observación y Filtrado de Citas (Activas vs Canceladas)
    // ------------------------------------------
    val citas by bookingViewModel.appointments.collectAsState()
    val boxes by bookingViewModel.boxes.collectAsState()

    var filtroSeleccionado by remember { mutableStateOf("TODAS") }

    val activas = citas.filter { !it.isCancelled }
    val canceladas = citas.filter { it.isCancelled }
    val presencialesActivas = activas.count { !it.isOnline }
    val onlineActivas = activas.count { it.isOnline }

    val citasFiltradas = when (filtroSeleccionado) {
        "ACTIVAS" -> activas
        "CANCELADAS" -> canceladas
        else -> citas
    }.sortedWith(compareBy({ it.date }, { it.timeSlot }))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Gestión de Citas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Programación general y ocupación de los 8 boxes de atención.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Resumen general con citas activas y canceladas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ResumenCard("Activas", activas.size.toString(), Modifier.weight(1f))
                ResumenCard("Presenciales", presencialesActivas.toString(), Modifier.weight(1f))
                ResumenCard("Online", onlineActivas.toString(), Modifier.weight(1f))
                ResumenCard("Canceladas", canceladas.size.toString(), Modifier.weight(1f), isDestructive = canceladas.isNotEmpty())
            }
        }

        // Ocupación de los 8 boxes (solo citas presenciales ACTIVAS tienen box)
        item {
            Text("Ocupación por box", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..4).forEach { box ->
                        val activeCount = activas.count { it.boxNumber == box }
                        val isOccupiedNow = isBoxOccupiedNow(box, activas)
                        val status = if (isOccupiedNow) "reservado" else "disponible"
                        BoxCard(box, activeCount, status, Modifier.weight(1f))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (5..8).forEach { box ->
                        val activeCount = activas.count { it.boxNumber == box }
                        val isOccupiedNow = isBoxOccupiedNow(box, activas)
                        val status = if (isOccupiedNow) "reservado" else "disponible"
                        BoxCard(box, activeCount, status, Modifier.weight(1f))
                    }
                }
            }
        }

        // Filtro y lista de citas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Listado de Citas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroSeleccionado == "TODAS",
                        onClick = { filtroSeleccionado = "TODAS" },
                        label = { Text("Todas (${citas.size})") }
                    )
                    FilterChip(
                        selected = filtroSeleccionado == "ACTIVAS",
                        onClick = { filtroSeleccionado = "ACTIVAS" },
                        label = { Text("Activas (${activas.size})") }
                    )
                    FilterChip(
                        selected = filtroSeleccionado == "CANCELADAS",
                        onClick = { filtroSeleccionado = "CANCELADAS" },
                        label = { Text("Canceladas (${canceladas.size})") }
                    )
                }
            }
        }

        if (citasFiltradas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = when (filtroSeleccionado) {
                            "ACTIVAS" -> "No hay citas activas agendadas en este momento."
                            "CANCELADAS" -> "No hay citas canceladas registradas."
                            else -> "Aún no hay citas registradas en el centro."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(citasFiltradas, key = { it.id }) { cita ->
                AppointmentCard(
                    appointment = cita,
                    onCancel = {
                        bookingViewModel.cancelAppointment(cita.id)
                    },
                    onDelete = {
                        bookingViewModel.deleteAppointment(cita.id)
                    }
                )
            }
        }
    }
}

// ==========================================
// Componente de Tarjeta de Resumen Estadístico
// ==========================================
@Composable
private fun ResumenCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    ElevatedCard(
        modifier = modifier,
        colors = if (isDestructive) CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ) else CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// Componente de Tarjeta de Ocupación de Box
// ==========================================
@Composable
private fun BoxCard(
    box: Int,
    cantidad: Int,
    status: String = "disponible",
    modifier: Modifier = Modifier
) {
    val isReserved = status.equals("reservado", ignoreCase = true)
    ElevatedCard(
        modifier = modifier,
        colors = if (isReserved) CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ) else CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Box $box", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = if (isReserved) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFE8F5E9),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (isReserved) "Reservado" else "Disponible",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isReserved) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (cantidad == 1) "1 cita" else "$cantidad citas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------------------------------
// Verificación de Ocupación en Tiempo Real (Momento Actual)
// ------------------------------------------
private fun isBoxOccupiedNow(box: Int, activeAppointments: List<AppointmentEntity>): Boolean {
    val today = LocalDate.now().toString() // yyyy-MM-dd
    val now = LocalTime.now()

    return activeAppointments.any { cita ->
        if (cita.boxNumber != box || cita.isOnline || cita.isCancelled) return@any false

        val cleanDate = cita.date.trim()
        val isToday = if (cleanDate.contains("/")) {
            val parts = cleanDate.split("/")
            if (parts.size == 3) {
                val isoDate = if (parts[0].length == 4) "${parts[0]}-${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}"
                else "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                isoDate == today
            } else false
        } else {
            cleanDate == today
        }
        if (!isToday) return@any false

        val start = parseSlotToTime(cita.timeSlot) ?: return@any false
        val end = start.plusHours(1)
        !now.isBefore(start) && now.isBefore(end)
    }
}

private fun parseSlotToTime(slot: String): LocalTime? {
    return try {
        val clean = slot.trim()
        val parts = clean.split(" ")
        val timeParts = parts.getOrNull(0)?.split(":") ?: return null
        var hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        val isPm = parts.getOrNull(1)?.equals("PM", ignoreCase = true) == true
        if (isPm && hour in 1..11) hour += 12
        else if (!isPm && hour == 12) hour = 0
        LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    } catch (_: Exception) {
        null
    }
}
