package com.sanaesencia.app.data.util

// ==========================================
// Módulo de Validación de Datos de Entrada
// ==========================================
object Validators {

    // ------------------------------------------
    // Validación de RUN / RUT Chileno (Algoritmo Módulo 11)
    // ------------------------------------------
    // Validador de RUN Chileno (Módulo 11) o "admin" para cuenta de administración
    fun isValidRUN(run: String): Boolean {
        val trimmed = run.trim()
        if (trimmed.equals("admin", ignoreCase = true)) return true

        val cleanRun = trimmed.replace(".", "").replace("-", "").uppercase()
        if (cleanRun.length < 8) return false

        val rut = cleanRun.substring(0, cleanRun.length - 1)
        val dv = cleanRun.substring(cleanRun.length - 1)

        var sum = 0
        var multiplier = 2

        try {
            for (i in rut.length - 1 downTo 0) {
                sum += rut[i].toString().toInt() * multiplier
                multiplier = if (multiplier == 7) 2 else multiplier + 1
            }
            val expectedDv = 11 - (sum % 11)
            val finalDv = when (expectedDv) {
                11 -> "0"
                10 -> "K"
                else -> expectedDv.toString()
            }
            return dv == finalDv
        } catch (e: Exception) {
            return false
        }
    }

    // ------------------------------------------
    // Validación de Formato de Correo Electrónico
    // ------------------------------------------
    // Validador de Correo Electrónico con expresión regular estándar
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\\.[A-Za-z]{2,}\$".toRegex()
        return trimmed.isNotBlank() && trimmed.matches(emailRegex)
    }

    // ------------------------------------------
    // Validación de Formato de Teléfono
    // ------------------------------------------
    // Validador de Teléfono (sólo dígitos, opcional signo +, longitud 8 a 12 dígitos)
    fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        return cleanPhone.matches(Regex("^[+]?[0-9]{8,12}\$"))
    }

    // ------------------------------------------
    // Validación de Longitud de Nombre
    // ------------------------------------------
    // Validador de Nombre completo (mínimo 3 caracteres no vacíos)
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 3
    }

    // ------------------------------------------
    // Validación de Longitud de Contraseña
    // ------------------------------------------
    // Validador de Contraseña (mínimo 4 caracteres)
    fun isValidPassword(password: String): Boolean {
        return password.trim().length >= 4
    }

    // ------------------------------------------
    // Validación de Especialidad Médica / Profesional
    // ------------------------------------------
    // Validador de Especialidad (mínimo 3 caracteres)
    fun isValidSpecialty(specialty: String): Boolean {
        return specialty.trim().length >= 3
    }
}
