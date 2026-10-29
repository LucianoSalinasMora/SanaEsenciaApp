package com.sanaesencia.app.data.repository

import com.sanaesencia.app.data.dao.AppointmentDao
import com.sanaesencia.app.data.model.AppointmentEntity

import kotlinx.coroutines.flow.Flow

// ==========================================
// Repositorio para la Gestión de Citas
// ==========================================
class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    // ------------------------------------------
    // Flujo Reactivo de Todas las Citas
    // ------------------------------------------
    val allAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()

    // ------------------------------------------
    // Guardar / Registrar Nueva Cita
    // ------------------------------------------
    suspend fun saveAppointment(appointment: AppointmentEntity): Boolean {
        return try {
            appointmentDao.insertAppointment(appointment)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ------------------------------------------
    // Comprobación de Conflictos de Horario
    // ------------------------------------------
    suspend fun isSlotTaken(date: String, professionalName: String, timeSlot: String): Boolean {
        return appointmentDao.countConflictAppointments(date, professionalName, timeSlot) > 0
    }

    // ------------------------------------------
    // Consulta de Boxes Ocupados
    // ------------------------------------------
    suspend fun getOccupiedBoxes(date: String, timeSlot: String): List<Int> {
        return appointmentDao.getOccupiedBoxes(date, timeSlot)
    }

    // ------------------------------------------
    // Cancelación y Eliminación de Citas
    // ------------------------------------------
    suspend fun cancelAppointment(id: Long) {
        appointmentDao.updateAppointmentState(id, "CANCELADA", "Cancelada")
    }

    suspend fun deleteAppointment(id: Long) {
        appointmentDao.deleteAppointment(id)
    }
}
