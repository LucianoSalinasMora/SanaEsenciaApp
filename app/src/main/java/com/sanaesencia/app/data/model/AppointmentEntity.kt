package com.sanaesencia.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ==========================================
// Entidad Room: Registro de Citas Médicas
// ==========================================
@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Datos del Paciente
    val patientName: String = "",
    val patientEmail: String = "",
    val patientPhone: String = "",
    val patientRun: String = "",
    // Datos del Profesional y Servicio
    val professionalRun: String = "",
    val serviceType: String = "",
    val professionalName: String = "Centro Sanaesencia",
    // Fecha, Horario y Modalidad
    val date: String = "",
    val timeSlot: String = "10:00 AM",
    val isOnline: Boolean = false,
    val boxNumber: Int? = null,
    // Estado y Notas Clínicas / de Atención
    val status: String = "Agendada",
    val state: String = "PENDIENTE",
    val patientNotes: String = ""
)

// ==========================================
// Propiedad de Extensión: Verificación de Cancelación
// ==========================================
val AppointmentEntity.isCancelled: Boolean
    get() = state.equals("CANCELADA", ignoreCase = true) ||
            status.equals("Cancelada", ignoreCase = true) ||
            status.equals("CANCELADA", ignoreCase = true)
