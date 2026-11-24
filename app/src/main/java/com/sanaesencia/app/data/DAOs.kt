package com.sanaesencia.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ==========================================
// Interfaz DAO para Información "Nosotros / Acerca de"
// ==========================================
@Dao
interface AboutDao {

    // ------------------------------------------
    // Consulta reactiva de información general
    // ------------------------------------------
    @Query("SELECT * FROM about_info WHERE id = 1")
    fun getAboutInfo(): Flow<AboutInfoEntity?>

    // ------------------------------------------
    // Inserción / Actualización de información
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAboutInfo(info: AboutInfoEntity)
}

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

// ==========================================
// Interfaz DAO para Productos y Catálogo Natural
// ==========================================
@Dao
interface ProductDao {

    // ------------------------------------------
    // Operaciones de Inserción (Individual y en Lote)
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // ------------------------------------------
    // Operaciones de Actualización y Eliminación
    // ------------------------------------------
    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // ------------------------------------------
    // Consultas Reactivas de Productos
    // ------------------------------------------
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsById(): Flow<List<ProductEntity>>

    // ------------------------------------------
    // Conteo Total de Productos
    // ------------------------------------------
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}

// ==========================================
// Interfaz DAO para Reseñas y Testimonios
// ==========================================
@Dao
interface ReviewDao {

    // ------------------------------------------
    // Inserción de Nueva Reseña
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // ------------------------------------------
    // Consulta Reactiva de Reseñas (Orden Cronológico Inverso)
    // ------------------------------------------
    // Muestra las reseñas más recientes primero
    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>
}

// ==========================================
// Interfaz DAO para Usuarios y Autenticación
// ==========================================
@Dao
interface UserDao {

    // ------------------------------------------
    // Inserción y Creación de Usuarios
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // ------------------------------------------
    // Actualización y Eliminación
    // ------------------------------------------
    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE run = :run")
    suspend fun deleteUser(run: String)

    // ------------------------------------------
    // Autenticación y Búsqueda de Usuarios
    // ------------------------------------------
    @Query("SELECT * FROM users WHERE run = :run AND passwordHash = :password LIMIT 1")
    suspend fun login(run: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE run = :run LIMIT 1")
    suspend fun getUserByRun(run: String): UserEntity?

    // ------------------------------------------
    // Consultas Reactivas por Rol
    // ------------------------------------------
    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    // ------------------------------------------
    // Recuperación y Actualización de Contraseña
    // ------------------------------------------
    @Query("UPDATE users SET passwordHash = :newPassword WHERE run = :run")
    suspend fun updatePassword(run: String, newPassword: String)
}

// ==========================================
// Interfaz DAO para Boxes de Atención Presencial
// ==========================================
@Dao
interface BoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBox(box: BoxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxes(boxes: List<BoxEntity>)

    @Update
    suspend fun updateBox(box: BoxEntity)

    @Query("SELECT * FROM boxes ORDER BY boxNumber ASC")
    fun getAllBoxes(): Flow<List<BoxEntity>>

    @Query("SELECT * FROM boxes WHERE status = 'disponible' ORDER BY boxNumber ASC")
    fun getAvailableBoxes(): Flow<List<BoxEntity>>

    @Query("SELECT * FROM boxes WHERE boxNumber = :boxNumber LIMIT 1")
    suspend fun getBoxByNumber(boxNumber: Int): BoxEntity?

    @Query("UPDATE boxes SET status = :status, currentAppointmentId = :appointmentId WHERE boxNumber = :boxNumber")
    suspend fun updateBoxStatus(boxNumber: Int, status: String, appointmentId: Long? = null)

    @Query("UPDATE boxes SET status = 'disponible', currentAppointmentId = NULL WHERE currentAppointmentId = :appointmentId")
    suspend fun releaseBoxByAppointmentId(appointmentId: Long)

    @Query("SELECT COUNT(*) FROM boxes")
    suspend fun getBoxCount(): Int
}

// ==========================================
// Interfaz DAO para Turnos / Módulos de Atención
// ==========================================
@Dao
interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurnos(turnos: List<TurnoEntity>)

    @Update
    suspend fun updateTurno(turno: TurnoEntity)

    @Query("DELETE FROM turnos WHERE id = :id")
    suspend fun deleteTurno(id: Long)

    @Query("SELECT * FROM turnos ORDER BY date ASC, timeSlot ASC")
    fun getAllTurnos(): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE date = :date ORDER BY timeSlot ASC")
    fun getTurnosByDate(date: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE professionalRun = :professionalRun ORDER BY date ASC, timeSlot ASC")
    fun getTurnosByProfessional(professionalRun: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE date = :date AND status = 'disponible' ORDER BY timeSlot ASC")
    fun getAvailableTurnos(date: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE date = :date AND professionalName = :professionalName AND timeSlot = :timeSlot LIMIT 1")
    suspend fun findTurno(date: String, professionalName: String, timeSlot: String): TurnoEntity?

    @Query("SELECT * FROM turnos WHERE id = :id LIMIT 1")
    suspend fun getTurnoById(id: Long): TurnoEntity?

    @Query("UPDATE turnos SET status = :status, appointmentId = :appointmentId WHERE id = :id")
    suspend fun updateTurnoStatus(id: Long, status: String, appointmentId: Long? = null)

    @Query("UPDATE turnos SET status = 'disponible', appointmentId = NULL WHERE appointmentId = :appointmentId")
    suspend fun releaseTurnoByAppointmentId(appointmentId: Long)

    @Query("SELECT COUNT(*) FROM turnos")
    suspend fun getTurnoCount(): Int
}
