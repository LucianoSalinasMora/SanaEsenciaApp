package com.sanaesencia.app.data.local

import com.sanaesencia.app.data.model.UserRole

import androidx.room.TypeConverter

// ==========================================
// Convertidores de Tipos para Persistencia en Room
// ==========================================
class Converters {

    // ------------------------------------------
    // Conversión de Enum UserRole a String
    // ------------------------------------------
    @TypeConverter
    fun fromUserRole(role: UserRole?): String? {
        return role?.name
    }

    // ------------------------------------------
    // Conversión de String a Enum UserRole
    // ------------------------------------------
    @TypeConverter
    fun toUserRole(value: String?): UserRole? {
        return if (value != null) {
            try {
                UserRole.valueOf(value)
            } catch (e: Exception) {
                UserRole.PACIENTE
            }
        } else {
            UserRole.PACIENTE
        }
    }
}
