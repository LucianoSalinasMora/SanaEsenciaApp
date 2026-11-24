package com.sanaesencia.app

import com.sanaesencia.app.data.*
import com.sanaesencia.app.viewmodel.BookingFormState
import com.sanaesencia.app.viewmodel.LoginFormState
import com.sanaesencia.app.viewmodel.SignUpFormState
import org.junit.Assert.*
import org.junit.Test

// ==========================================
// 1. PRUEBAS UNITARIAS DE VALIDACIONES DE ENTRADA
// ==========================================
class ValidatorsUnitTest {

    @Test
    fun testValidChileanRUN() {
        // RUNs válidos reales (algoritmo Módulo 11)
        assertTrue(Validators.isValidRUN("20000001-3"))
        assertTrue(Validators.isValidRUN("20.000.001-3"))
        assertTrue(Validators.isValidRUN("20000002-1"))
        assertTrue(Validators.isValidRUN("20000004-8"))
        // RUN admin reservado para soporte
        assertTrue(Validators.isValidRUN("admin"))
        assertTrue(Validators.isValidRUN("ADMIN"))
    }

    @Test
    fun testInvalidChileanRUN() {
        // RUNs con dígito verificador erróneo o longitud insuficiente
        assertFalse(Validators.isValidRUN("20000001-9"))
        assertFalse(Validators.isValidRUN("12345"))
        assertFalse(Validators.isValidRUN("invalido"))
        assertFalse(Validators.isValidRUN(""))
    }

    @Test
    fun testValidEmail() {
        assertTrue(Validators.isValidEmail("contacto@centrosanaesencia.cl"))
        assertTrue(Validators.isValidEmail("paciente.test+1@gmail.com"))
        assertTrue(Validators.isValidEmail("dr.mendoza@clinica.org"))
    }

    @Test
    fun testInvalidEmail() {
        assertFalse(Validators.isValidEmail("pacientecorreo.cl"))
        assertFalse(Validators.isValidEmail("@centrosanaesencia.cl"))
        assertFalse(Validators.isValidEmail("paciente@"))
        assertFalse(Validators.isValidEmail(""))
    }

    @Test
    fun testValidPhone() {
        assertTrue(Validators.isValidPhone("+56923665073"))
        assertTrue(Validators.isValidPhone("987654321"))
        assertTrue(Validators.isValidPhone("56900000000"))
    }

    @Test
    fun testInvalidPhone() {
        assertFalse(Validators.isValidPhone("123"))
        assertFalse(Validators.isValidPhone("telefono"))
        assertFalse(Validators.isValidPhone(""))
    }

    @Test
    fun testValidName() {
        assertTrue(Validators.isValidName("Dr. Carlos Mendoza"))
        assertTrue(Validators.isValidName("Ana"))
        assertFalse(Validators.isValidName("A"))
        assertFalse(Validators.isValidName("  "))
    }

    @Test
    fun testValidPassword() {
        assertTrue(Validators.isValidPassword("admin"))
        assertTrue(Validators.isValidPassword("1234"))
        assertFalse(Validators.isValidPassword("123"))
        assertFalse(Validators.isValidPassword(""))
    }

    @Test
    fun testValidSpecialty() {
        assertTrue(Validators.isValidSpecialty("Psicología Infanto-Juvenil"))
        assertTrue(Validators.isValidSpecialty("Fonoaudiología"))
        assertFalse(Validators.isValidSpecialty("TO"))
        assertFalse(Validators.isValidSpecialty(""))
    }
}

// ==========================================
// 2. PRUEBAS UNITARIAS DE FORMATO DE FECHA (DD/MM/AAAA)
// ==========================================
class DateFormatUnitTest {

    @Test
    fun testFormatDisplayDateFromIso() {
        assertEquals("25/11/2026", formatDisplayDate("2026-11-25"))
        assertEquals("05/01/2027", formatDisplayDate("2027-01-05"))
        assertEquals("14/09/2026", formatDisplayDate("2026-9-14"))
    }

    @Test
    fun testFormatDisplayDateAlreadyFormatted() {
        assertEquals("25/11/2026", formatDisplayDate("25/11/2026"))
        assertEquals("01/12/2026", formatDisplayDate("01/12/2026"))
    }

    @Test
    fun testFormatDisplayDateEmptyOrInvalid() {
        assertEquals("", formatDisplayDate(""))
        assertEquals("   ", formatDisplayDate("   ").trim())
        assertEquals("texto-plano", formatDisplayDate("texto-plano"))
    }
}

// ==========================================
// 3. PRUEBAS UNITARIAS DE LÓGICA DE ENTIDADES
// ==========================================
class EntitiesLogicUnitTest {

    @Test
    fun testAppointmentIsCancelledExtension() {
        val activeAppointment = AppointmentEntity(
            status = "Agendada",
            state = "PENDIENTE"
        )
        assertFalse(activeAppointment.isCancelled)

        val cancelledByState = AppointmentEntity(
            status = "Agendada",
            state = "CANCELADA"
        )
        assertTrue(cancelledByState.isCancelled)

        val cancelledByStatus = AppointmentEntity(
            status = "Cancelada",
            state = "PENDIENTE"
        )
        assertTrue(cancelledByStatus.isCancelled)
    }

    @Test
    fun testBoxEntityReservationStatus() {
        val availableBox = BoxEntity(
            boxNumber = 1,
            name = "Box 1",
            status = "disponible"
        )
        assertFalse(availableBox.isReserved)

        val reservedBox = BoxEntity(
            boxNumber = 2,
            name = "Box 2",
            status = "reservado",
            currentAppointmentId = 101L
        )
        assertTrue(reservedBox.isReserved)
        assertEquals(101L, reservedBox.currentAppointmentId)
    }

    @Test
    fun testTurnoEntityReservationStatus() {
        val availableTurno = TurnoEntity(
            id = 1L,
            date = "2026-11-25",
            timeSlot = "10:00 AM",
            professionalRun = "20000001-3",
            boxNumber = 1,
            status = "disponible"
        )
        assertFalse(availableTurno.isReserved)

        val reservedTurno = TurnoEntity(
            id = 2L,
            date = "2026-11-25",
            timeSlot = "11:00 AM",
            professionalRun = "20000001-3",
            boxNumber = 1,
            status = "reservado",
            appointmentId = 55L
        )
        assertTrue(reservedTurno.isReserved)
        assertEquals(55L, reservedTurno.appointmentId)
    }
}

// ==========================================
// 4. PRUEBAS UNITARIAS DE ESTADOS DE FORMULARIO
// ==========================================
class FormStatesUnitTest {

    @Test
    fun testBookingFormStateDefaultsToPlaceholders() {
        val defaultBookingState = BookingFormState()
        // Debe iniciar vacío para que la UI muestre placeholders según requerimientos
        assertEquals("", defaultBookingState.serviceType)
        assertEquals("", defaultBookingState.professionalName)
        assertEquals("", defaultBookingState.notes)
        assertEquals("", defaultBookingState.date)
        assertEquals("", defaultBookingState.timeSlot)
        assertFalse(defaultBookingState.isFormValid)
    }

    @Test
    fun testLoginFormStateDefaults() {
        val loginState = LoginFormState()
        assertEquals("", loginState.run)
        assertEquals("", loginState.password)
        assertTrue(loginState.isRunValid)
        assertTrue(loginState.isPasswordValid)
        assertFalse(loginState.isFormValid)
    }

    @Test
    fun testSignUpFormStateDefaults() {
        val signUpState = SignUpFormState()
        assertEquals("", signUpState.run)
        assertEquals("", signUpState.name)
        assertEquals("", signUpState.email)
        assertEquals("", signUpState.phone)
        assertEquals("", signUpState.password)
        assertFalse(signUpState.isFormValid)
    }
}

// ==========================================
// 5. PRUEBAS UNITARIAS DE MAPEO DTO <-> ENTIDAD
// ==========================================
class NetworkDtoMappingUnitTest {

    @Test
    fun testAppointmentEntityToDtoAndBack() {
        val entity = AppointmentEntity(
            id = 15L,
            patientName = "Juan Pérez",
            patientEmail = "juan@correo.com",
            patientPhone = "+56912345678",
            patientRun = "20000100-1",
            professionalRun = "20000001-3",
            serviceType = "Psicología",
            professionalName = "Dra. Keilen Sánchez Ríos",
            date = "2026-11-25",
            timeSlot = "10:00 AM",
            isOnline = false,
            boxNumber = 3,
            status = "Agendada",
            state = "PENDIENTE",
            patientNotes = "Consulta de control",
            turnoId = 42L
        )

        val dto = entity.toDto()
        assertEquals(entity.id, dto.id)
        assertEquals(entity.patientName, dto.patientName)
        assertEquals(entity.boxNumber, dto.boxNumber)
        assertEquals(entity.turnoId, dto.turnoId)

        val mappedBack = dto.toEntity()
        assertEquals(entity, mappedBack)
    }

    @Test
    fun testBoxEntityToDtoAndBack() {
        val box = BoxEntity(
            boxNumber = 4,
            name = "Box 4",
            status = "reservado",
            description = "Atención presencial Box 4",
            currentAppointmentId = 99L
        )

        val dto = box.toDto()
        assertEquals(4, dto.boxNumber)
        assertEquals("reservado", dto.status)
        assertEquals(99L, dto.currentAppointmentId)

        val mappedBack = dto.toEntity()
        assertEquals(box, mappedBack)
    }

    @Test
    fun testTurnoEntityToDtoAndBack() {
        val turno = TurnoEntity(
            id = 7L,
            date = "2026-11-25",
            timeSlot = "15:00 PM",
            professionalRun = "20000005-6",
            professionalName = "Sebastián Olivares",
            serviceType = "Psicología Infanto-Juvenil",
            boxNumber = 2,
            status = "disponible",
            isOnline = false
        )

        val dto = turno.toDto()
        assertEquals(7L, dto.id)
        assertEquals("15:00 PM", dto.timeSlot)
        assertEquals(2, dto.boxNumber)

        val mappedBack = dto.toEntity()
        assertEquals(turno, mappedBack)
    }
}

