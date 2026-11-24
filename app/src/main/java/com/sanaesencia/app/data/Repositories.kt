package com.sanaesencia.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// ==========================================
// Repositorio de Información Institucional "Nosotros"
// ==========================================
class AboutRepository(private val aboutDao: AboutDao) {

    // ------------------------------------------
    // Flujo Reactivo de Datos Institucionales
    // ------------------------------------------
    val aboutInfo: Flow<AboutInfoEntity?> = aboutDao.getAboutInfo()

    // ------------------------------------------
    // Operación de Guardado / Actualización
    // ------------------------------------------
    suspend fun saveAboutInfo(info: AboutInfoEntity) {
        aboutDao.insertAboutInfo(info)
    }
}

// ==========================================
// Repositorio para la Gestión de Citas
// ==========================================
class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val boxDao: BoxDao? = null,
    private val turnoDao: TurnoDao? = null
) {
    private val resolvedBoxDao: BoxDao?
        get() = boxDao ?: AppDatabase.getInstance()?.boxDao()

    private val resolvedTurnoDao: TurnoDao?
        get() = turnoDao ?: AppDatabase.getInstance()?.turnoDao()

    // ------------------------------------------
    // Flujo Reactivo de Todas las Citas
    // ------------------------------------------
    val allAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()
    val allBoxes: Flow<List<BoxEntity>>
        get() = resolvedBoxDao?.getAllBoxes() ?: flowOf(emptyList())
    val allTurnos: Flow<List<TurnoEntity>>
        get() = resolvedTurnoDao?.getAllTurnos() ?: flowOf(emptyList())

    // ------------------------------------------
    // Inicialización y Sembrado de Boxes y Turnos
    // ------------------------------------------
    suspend fun seedInitialBoxesAndTurnos() {
        try {
            resolvedBoxDao?.let { bDao ->
                if (bDao.getBoxCount() == 0) {
                    bDao.insertBoxes(seedBoxes)
                }
            }
            resolvedTurnoDao?.let { tDao ->
                if (tDao.getTurnoCount() == 0) {
                    tDao.insertTurnos(generateSeedTurnos())
                }
            }
        } catch (_: Exception) {}
    }

    // ------------------------------------------
    // Guardar / Registrar Nueva Cita
    // ------------------------------------------
    suspend fun saveAppointment(appointment: AppointmentEntity): Boolean {
        return try {
            val insertedId = appointmentDao.insertAppointment(appointment)

            // Si es presencial y tiene box asignado, actualizar estado del Box a 'reservado'
            if (!appointment.isOnline && appointment.boxNumber != null) {
                resolvedBoxDao?.updateBoxStatus(appointment.boxNumber, "reservado", insertedId)
            }

            // Actualizar o crear turno asociado en estado 'reservado'
            val existingTurno = resolvedTurnoDao?.findTurno(appointment.date, appointment.professionalName, appointment.timeSlot)
            if (existingTurno != null) {
                resolvedTurnoDao?.updateTurnoStatus(existingTurno.id, "reservado", insertedId)
            } else {
                resolvedTurnoDao?.insertTurno(
                    TurnoEntity(
                        date = appointment.date,
                        timeSlot = appointment.timeSlot,
                        professionalRun = appointment.professionalRun,
                        professionalName = appointment.professionalName,
                        serviceType = appointment.serviceType,
                        boxNumber = appointment.boxNumber,
                        status = "reservado",
                        isOnline = appointment.isOnline,
                        appointmentId = insertedId
                    )
                )
            }

            // Sincronización con backend microservicio Spring Boot (Offline-First)
            try {
                RetrofitClient.backendApi.createAppointment(appointment.copy(id = insertedId).toDto())
            } catch (_: Exception) {
                // Operación local en Room preservada exitosamente si no hay conexión externa
            }

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
    // Consumo de API Externa: Clima en tiempo real Los Vilos
    // ------------------------------------------
    suspend fun getLosVilosWeather(): CurrentWeatherDto? {
        return try {
            val response = RetrofitClient.weatherApi.getWeather()
            if (response.isSuccessful) response.body()?.current_weather else null
        } catch (_: Exception) {
            null
        }
    }

    // ------------------------------------------
    // Cancelación y Eliminación de Citas
    // ------------------------------------------
    suspend fun cancelAppointment(id: Long) {
        appointmentDao.updateAppointmentState(id, "CANCELADA", "Cancelada")
        // Liberar box asociado a la cita
        resolvedBoxDao?.releaseBoxByAppointmentId(id)
        // Liberar turno asociado a la cita
        resolvedTurnoDao?.releaseTurnoByAppointmentId(id)

        // Notificar al microservicio Spring Boot
        try {
            RetrofitClient.backendApi.cancelAppointment(id)
        } catch (_: Exception) {}
    }

    suspend fun deleteAppointment(id: Long) {
        resolvedBoxDao?.releaseBoxByAppointmentId(id)
        resolvedTurnoDao?.releaseTurnoByAppointmentId(id)
        appointmentDao.deleteAppointment(id)

        // Notificar al microservicio Spring Boot
        try {
            RetrofitClient.backendApi.deleteAppointment(id)
        } catch (_: Exception) {}
    }
}

// ==========================================
// Repositorio para la Gestión de Productos
// ==========================================
class ProductRepository(private val productDao: ProductDao) {

    // ------------------------------------------
    // Flujos Reactivos de Productos
    // ------------------------------------------
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val availableProducts: Flow<List<ProductEntity>> = productDao.getAvailableProducts()

    // ------------------------------------------
    // Operaciones CRUD de Productos
    // ------------------------------------------
    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    // ------------------------------------------
    // Inicialización y Sembrado de Productos por Defecto
    // ------------------------------------------
    suspend fun seedInitialProducts() {
        if (productDao.getCount() == 0) {
            val defaultProducts = listOf(
                ProductEntity(
                    name = "Kit de Bienestar",
                    description = "Set de aromaterapia con roll-on y bruma.",
                    price = 24990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Roll-on Calma",
                    description = "Aceites esenciales para momentos de estrés.",
                    price = 7990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Bruma de Almohada",
                    description = "Bruma relajante para el descanso nocturno.",
                    price = 9990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Roll-on Foco",
                    description = "Aroma para concentración y estudio.",
                    price = 7990.0,
                    isAvailable = false
                )
            )
            for (p in defaultProducts) {
                productDao.insertProduct(p)
            }
        }
    }
}

// ==========================================
// Repositorio de Usuarios y Autenticación
// ==========================================
class UserRepository(private val userDao: UserDao) {

    @Volatile
    private var isSeeded = false

    // ------------------------------------------
    // Usuario Administrador Inicial por Defecto
    // ------------------------------------------
    private val defaultAdmin = UserEntity(
        run = "admin",
        name = "Administrador Centro Sanaesencia",
        email = "admin@sanaesencia.cl",
        phone = "+56900000000",
        passwordHash = "admin",
        role = UserRole.ADMIN,
        securityQuestion = "Pregunta Admin",
        securityAnswer = "admin"
    )

    // ------------------------------------------
    // Inicialización y Sembrado de Datos de Usuario
    // ------------------------------------------
    suspend fun seedInitialData() {
        if (isSeeded) return
        try {
            val allUsers = listOf(defaultAdmin) + seedUsers
            userDao.insertUsers(allUsers)
            isSeeded = true
        } catch (e: Throwable) {
            // Ignore if exists or error
        }
    }

    // ------------------------------------------
    // Operaciones CRUD de Usuarios
    // ------------------------------------------
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(run: String) {
        userDao.deleteUser(run)
    }

    // ------------------------------------------
    // Lógica de Inicio de Sesión y Autenticación
    // ------------------------------------------
    suspend fun login(run: String, password: String): UserEntity? {
        val cleanRun = run.trim()
        val formattedRun = cleanRun.replace(".", "").replace("-", "").uppercase()

        if (cleanRun.equals("admin", ignoreCase = true) && password == "admin") {
            seedInitialData()
            return defaultAdmin
        }

        seedInitialData()

        return userDao.login(cleanRun, password)
            ?: userDao.login(formattedRun, password)
    }

    // ------------------------------------------
    // Búsqueda y Consulta de Usuarios por RUN o Rol
    // ------------------------------------------
    suspend fun getUserByRun(run: String): UserEntity? {
        val cleanRun = run.trim()
        val formattedRun = cleanRun.replace(".", "").replace("-", "").uppercase()

        if (cleanRun.equals("admin", ignoreCase = true)) {
            return defaultAdmin
        }

        seedInitialData()

        return userDao.getUserByRun(cleanRun)
            ?: userDao.getUserByRun(formattedRun)
    }

    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>> {
        return userDao.getUsersByRole(role.name)
    }

    // ------------------------------------------
    // Actualización de Contraseña
    // ------------------------------------------
    suspend fun updatePassword(run: String, newPassword: String) {
        val cleanRun = run.trim()
        val formattedRun = cleanRun.replace(".", "").replace("-", "").uppercase()
        userDao.updatePassword(cleanRun, newPassword)
        userDao.updatePassword(formattedRun, newPassword)
    }
}

// ==========================================
// Repositorio para la Gestión de Boxes de Atención
// ==========================================
class BoxRepository(private val boxDao: BoxDao) {

    val allBoxes: Flow<List<BoxEntity>> = boxDao.getAllBoxes()
    val availableBoxes: Flow<List<BoxEntity>> = boxDao.getAvailableBoxes()

    suspend fun seedInitialBoxes() {
        if (boxDao.getBoxCount() == 0) {
            boxDao.insertBoxes(seedBoxes)
        }
    }

    suspend fun getBoxByNumber(boxNumber: Int): BoxEntity? {
        return boxDao.getBoxByNumber(boxNumber)
    }

    suspend fun reserveBox(boxNumber: Int, appointmentId: Long? = null) {
        boxDao.updateBoxStatus(boxNumber, "reservado", appointmentId)
    }

    suspend fun releaseBox(boxNumber: Int) {
        boxDao.updateBoxStatus(boxNumber, "disponible", null)
    }

    suspend fun updateBoxStatus(boxNumber: Int, status: String) {
        boxDao.updateBoxStatus(boxNumber, status)
    }
}

// ==========================================
// Repositorio para la Gestión de Turnos de Atención
// ==========================================
class TurnoRepository(private val turnoDao: TurnoDao) {

    val allTurnos: Flow<List<TurnoEntity>> = turnoDao.getAllTurnos()

    fun getTurnosByDate(date: String): Flow<List<TurnoEntity>> {
        return turnoDao.getTurnosByDate(date)
    }

    fun getTurnosByProfessional(professionalRun: String): Flow<List<TurnoEntity>> {
        return turnoDao.getTurnosByProfessional(professionalRun)
    }

    fun getAvailableTurnos(date: String): Flow<List<TurnoEntity>> {
        return turnoDao.getAvailableTurnos(date)
    }

    suspend fun seedInitialTurnos() {
        if (turnoDao.getTurnoCount() == 0) {
            turnoDao.insertTurnos(generateSeedTurnos())
        }
    }

    suspend fun insertTurno(turno: TurnoEntity): Long {
        return turnoDao.insertTurno(turno)
    }

    suspend fun updateTurno(turno: TurnoEntity) {
        turnoDao.updateTurno(turno)
    }

    suspend fun reserveTurno(turnoId: Long, appointmentId: Long) {
        turnoDao.updateTurnoStatus(turnoId, "reservado", appointmentId)
    }

    suspend fun releaseTurno(turnoId: Long) {
        turnoDao.updateTurnoStatus(turnoId, "disponible", null)
    }

    suspend fun releaseTurnoByAppointment(appointmentId: Long) {
        turnoDao.releaseTurnoByAppointmentId(appointmentId)
    }
}
