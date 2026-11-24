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
