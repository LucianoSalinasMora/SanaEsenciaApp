package com.sanaesencia.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ==========================================
// Entidad Room: Información Institucional "Nosotros"
// ==========================================
@Entity(tableName = "about_info")
data class AboutInfoEntity(
    // Identificador único (fila única por defecto id = 1)
    @PrimaryKey val id: Int = 1,
    // Título y descripción general del centro
    val centerTitle: String = "Centro Integrativo Sanaesencia",
    val description: String = "Salud mental infanto-juvenil, familia y bienestar integrativo. Atendemos de forma presencial en nuestros 8 boxes de Los Vilos y online para todo Chile y el extranjero.",
    // Datos de contacto y ubicación
    val address: String = "Caupolicán 220, Los Vilos",
    val phone: String = "+56 9 2366 5073",
    val email: String = "contacto@centrosanaesencia.cl"
)

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
    val patientNotes: String = "",
    // Asociación con TurnoEntity
    val turnoId: Long? = null
)

// ==========================================
// Propiedad de Extensión: Verificación de Cancelación
// ==========================================
val AppointmentEntity.isCancelled: Boolean
    get() = state.equals("CANCELADA", ignoreCase = true) ||
            status.equals("Cancelada", ignoreCase = true) ||
            status.equals("CANCELADA", ignoreCase = true)

// ==========================================
// Entidad Room: Usuarios y Credenciales
// ==========================================
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val run: String, // Usaremos el RUN como ID principal
    val name: String,
    val email: String = "",
    val phone: String = "",
    val passwordHash: String,
    val role: UserRole = UserRole.PACIENTE,
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val specialty: String = ""
)

// ==========================================
// Entidad Room: Productos y Catálogo Natural
// ==========================================
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val stock: Int = 10
)

// ==========================================
// Entidad Room: Reseñas y Testimonios de Pacientes
// ==========================================
@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val content: String,
    val rating: Int, // 1 a 5 estrellas
    val date: String
)

// ==========================================
// Entidad Room: Boxes de Atención Presencial
// ==========================================
@Entity(tableName = "boxes")
data class BoxEntity(
    @PrimaryKey val boxNumber: Int, // Boxes 1 a 8 en Los Vilos
    val name: String = "Box $boxNumber",
    val status: String = "disponible", // "disponible" o "reservado"
    val description: String = "Box de atención presencial $boxNumber",
    val currentAppointmentId: Long? = null // ID de la cita actualmente asignada
)

// ==========================================
// Entidad Room: Turnos / Módulos de Atención
// ==========================================
@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String = "", // Formato yyyy-MM-dd
    val timeSlot: String = "", // ej. "09:00 AM", "10:00 AM"
    val professionalRun: String = "", // Asociación con UserEntity por RUN
    val professionalName: String = "",
    val serviceType: String = "",
    val boxNumber: Int? = null, // Asociación con BoxEntity (1..8) si es presencial
    val status: String = "disponible", // "disponible" o "reservado"
    val isOnline: Boolean = false,
    val appointmentId: Long? = null // Asociación con AppointmentEntity si está reservado
)

val BoxEntity.isReserved: Boolean
    get() = status.equals("reservado", ignoreCase = true)

val TurnoEntity.isReserved: Boolean
    get() = status.equals("reservado", ignoreCase = true)
