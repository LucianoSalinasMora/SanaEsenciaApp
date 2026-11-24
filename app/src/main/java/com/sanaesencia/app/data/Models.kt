package com.sanaesencia.app.data

// ==========================================
// Enumeraciones de Roles y Estados del Sistema
// ==========================================
enum class UserRole { PACIENTE, PROFESIONAL, ADMIN }
enum class AppointmentState { PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA, NO_ASISTE }

// ==========================================
// Modelo de Datos y Mock de Profesionales
// ==========================================
data class Professional(
    val id: String = "",
    val name: String,
    val specialty: String
)

val mockProfessionals = listOf(
    Professional("1", "Dr. Carlos Mendoza", "Psicología Infanto-Juvenil"),
    Professional("2", "Dra. María Paz Silva", "Terapia Ocupacional"),
    Professional("3", "Lic. Andrea Rojas", "Fonoaudiología"),
    Professional("4", "Dr. Juan Pablo Torres", "Nutrición Integrativa")
)
