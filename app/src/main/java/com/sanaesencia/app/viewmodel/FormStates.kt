package com.sanaesencia.app.viewmodel

// ==========================================
// Estado del Formulario de Agendamiento de Citas
// ==========================================
data class BookingFormState(
    // Datos del Paciente
    val patientName: String = "",
    val patientEmail: String = "",
    val patientPhone: String = "",
    val patientRun: String = "",
    // Selección de Servicio y Profesional
    val serviceType: String = "",
    val professionalName: String = "",
    // Fecha, Horario y Modalidad
    val date: String = "",
    val timeSlot: String = "",
    val notes: String = "",
    val isOnline: Boolean = false,
    // Estados de Validación y Envío del Formulario
    val isFormValid: Boolean = false,
    val isSubmittedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

// ==========================================
// Estado Reactivo del Formulario de Inicio de Sesión (Login)
// ==========================================
/**
 * Estado reactivo del formulario de inicio de sesión (Login).
 * Desacopla la lógica de validación de la interfaz de usuario (Compose).
 */
data class LoginFormState(
    val run: String = "",
    val isRunValid: Boolean = true,
    val password: String = "",
    val isPasswordValid: Boolean = true,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

// ==========================================
// Estado Reactivo del Formulario de Registro (Sign Up)
// ==========================================
/**
 * Estado reactivo del formulario de registro de nuevos pacientes (Sign Up).
 * Centraliza validaciones de RUT/RUN, correo, teléfono y contraseñas.
 */
data class SignUpFormState(
    val run: String = "",
    val isRunValid: Boolean = true,
    val name: String = "",
    val isNameValid: Boolean = true,
    val email: String = "",
    val isEmailValid: Boolean = true,
    val phone: String = "",
    val isPhoneValid: Boolean = true,
    val password: String = "",
    val isPasswordValid: Boolean = true,
    val securityQuestion: String = "¿Nombre de tu primera mascota?",
    val securityAnswer: String = "",
    val isSecurityAnswerValid: Boolean = true,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)
