package com.sanaesencia.app.data.repository

import com.sanaesencia.app.data.dao.UserDao
import com.sanaesencia.app.data.local.seedUsers
import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.model.UserRole

import kotlinx.coroutines.flow.Flow

// ==========================================
// Repositorio de Usuarios y Autenticación
// ==========================================
class UserRepository(private val userDao: UserDao) {

    @Volatile
    private var isSeeded = false

    // ------------------------------------------
    // Usuario Administrador Inicial por Defecto
    // ------------------------------------------
    // Usuario administrador por defecto
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
