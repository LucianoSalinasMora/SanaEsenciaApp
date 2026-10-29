package com.sanaesencia.app.data.dao

import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.model.UserRole

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
