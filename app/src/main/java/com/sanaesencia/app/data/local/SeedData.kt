package com.sanaesencia.app.data.local

import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.model.UserRole

// ==========================================
// Constantes de Contacto Institucional para Datos Semilla
// ==========================================
// Profesionales publicados en centrosanaesencia.cl (solo nombre y especialidad).
// Los dos "de Ejemplo" son ficticios, para cubrir áreas del caso que no aparecen en la web.
// Los RUN y las contraseñas son de prueba; el correo y teléfono son los de contacto del centro.
private const val CONTACT_EMAIL = "contacto@centrosanaesencia.cl"
private const val CONTACT_PHONE = "+56923665073"

// ==========================================
// Función Auxiliar para Creación de Profesionales
// ==========================================
private fun pro(run: String, name: String, specialty: String) = UserEntity(
    run = run,
    name = name,
    email = CONTACT_EMAIL,
    phone = CONTACT_PHONE,
    passwordHash = "1234",
    role = UserRole.PROFESIONAL,
    securityQuestion = "¿Nombre de tu primera mascota?",
    securityAnswer = "prueba",
    specialty = specialty
)

// ==========================================
// Lista Inicial de Usuarios y Profesionales del Sistema
// ==========================================
val seedUsers: List<UserEntity> = listOf(
    pro("20000001-3", "Dra. Keilen Sánchez Ríos", "Medicina General y Salud Mental"),
    pro("20000002-1", "Dra. Camila Tabilo", "Medicina General y Salud Mental"),
    pro("20000004-8", "Samantha Bastías", "Psicología Infanto-Juvenil"),
    pro("20000005-6", "Sebastián Olivares", "Psicología Infanto-Juvenil"),
    pro("20000006-4", "Yanira Ferreira", "Psicología Adolescentes y Adultos"),
    pro("20000007-2", "María José Mendicoa", "Psicología Adolescentes y Adultos"),
    pro("20000008-0", "Camila Soto", "Psicología Adolescentes y Adultos"),
    pro("20000009-9", "Francisca Villarroel", "Psicología Adolescentes y Adultos"),
    pro("20000010-2", "Janis Herrera", "Psicología Adolescentes y Adultos"),
    pro("20000011-0", "Jeaseth Alam", "Terapia Ocupacional"),
    pro("20000012-9", "Pamela Vergara", "Terapia Ocupacional"),
    pro("20000013-7", "Sebastián Acuña", "Terapia Ocupacional"),
    pro("20000014-5", "Yanari Ramírez", "Terapia Ocupacional"),
    pro("20000015-3", "Natalia Rojas", "Fonoaudiología"),
    pro("20000016-1", "Macarena González", "Fonoaudiología"),
    pro("20000018-8", "Natalia Ortiz", "Fonoaudiología"),
    pro("20000019-6", "Catalina Cobs", "Nutrición Integrativa"),
    pro("20000021-8", "Betzabé Codoceo", "Nutrición Integrativa"),
    pro("20000022-6", "Carolina Vega B.", "Nutrición Integrativa"),
    pro("20000023-4", "Maria Ignacia", "Terapia Floral y Aromaterapia"),
    pro("20000024-2", "Byron Sandoval", "Acupuntura"),
    pro("20000025-0", "Daniel Díaz", "Quiropraxia y Masajes"),
    pro("20000026-9", "Carmen Paz Astudillo", "Matronería y Salud Femenina"),
    pro("20000027-7", "Profesional de Ejemplo (Psicopedagogía)", "Psicopedagogía"),
    pro("20000028-5", "Profesional de Ejemplo (Neurodesarrollo)", "Evaluaciones del Neurodesarrollo"),
    // Paciente de prueba para ingresar con el rol Paciente sin registrarse
    UserEntity(
        run = "20000100-1",
        name = "Paciente de Prueba",
        email = "paciente@correo.com",
        phone = "987654321",
        passwordHash = "1234",
        role = UserRole.PACIENTE,
        securityQuestion = "¿Nombre de tu primera mascota?",
        securityAnswer = "prueba"
    )
)
