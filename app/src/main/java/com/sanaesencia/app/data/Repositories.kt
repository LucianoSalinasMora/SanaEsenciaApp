package com.sanaesencia.app.data

import kotlinx.coroutines.flow.Flow

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
