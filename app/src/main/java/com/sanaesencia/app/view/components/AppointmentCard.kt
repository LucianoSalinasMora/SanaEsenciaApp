package com.sanaesencia.app.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.AppointmentEntity
import com.sanaesencia.app.data.model.isCancelled

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
                    text = "¿Estás seguro de que deseas cancelar la cita de ${appointment.serviceType} con ${appointment.professionalName} agendada para el ${appointment.date} a las ${appointment.timeSlot}?\n\nAl cancelarla, desaparecerá de tus citas agendadas y el horario volverá a quedar disponible para otros pacientes."
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
                Text("Fecha: ${appointment.date}", style = MaterialTheme.typography.bodyMedium)
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

