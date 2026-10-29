package com.sanaesencia.app.data.dao

import com.sanaesencia.app.data.model.AppointmentEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ==========================================
// Interfaz DAO para Gestión de Citas Médicas
// ==========================================
@Dao
interface AppointmentDao {

    // ------------------------------------------
    // Inserción de Citas
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    // ------------------------------------------
    // Consultas Generales de Citas
    // ------------------------------------------
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    // ------------------------------------------
    // Consultas Filtradas por Rol (Profesional y Paciente)
    // ------------------------------------------
    // Obtener citas específicas de un profesional
    @Query("SELECT * FROM appointments WHERE professionalRun = :professionalRun ORDER BY date DESC")
    fun getAppointmentsByProfessional(professionalRun: String): Flow<List<AppointmentEntity>>

    // Obtener citas específicas de un paciente
    @Query("SELECT * FROM appointments WHERE patientRun = :patientRun ORDER BY date DESC")
    fun getAppointmentsByPatient(patientRun: String): Flow<List<AppointmentEntity>>

    // ------------------------------------------
    // Actualización de Estados de Cita
    // ------------------------------------------
    // Cambiar el estado de una cita
    @Query("UPDATE appointments SET state = :newState, status = :newStatus WHERE id = :appointmentId")
    suspend fun updateAppointmentState(appointmentId: Long, newState: String, newStatus: String = "Cancelada")

    // ------------------------------------------
    // Validación de Disponibilidad y Conflictos de Horario
    // ------------------------------------------
    // Verificar si ya existe una cita activa para el profesional en esa fecha y horario
    @Query("SELECT COUNT(*) FROM appointments WHERE date = :date AND professionalName = :professionalName AND timeSlot = :timeSlot AND state != 'CANCELADA' AND status != 'CANCELADA' AND status != 'Cancelada'")
    suspend fun countConflictAppointments(date: String, professionalName: String, timeSlot: String): Int

    // ------------------------------------------
    // Control de Ocupación de Boxes Presenciales
    // ------------------------------------------
    // Obtener qué boxes presenciales ya están ocupados en esa fecha y horario
    @Query("SELECT boxNumber FROM appointments WHERE date = :date AND timeSlot = :timeSlot AND isOnline = 0 AND state != 'CANCELADA' AND status != 'CANCELADA' AND status != 'Cancelada' AND boxNumber IS NOT NULL")
    suspend fun getOccupiedBoxes(date: String, timeSlot: String): List<Int>

    // ------------------------------------------
    // Eliminación de Registros de Cita
    // ------------------------------------------
    // Eliminar una cita por su ID
    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun deleteAppointment(appointmentId: Long)
}
