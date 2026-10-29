package com.sanaesencia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanaesencia.app.data.model.AppointmentEntity
import com.sanaesencia.app.data.repository.AppointmentRepository
import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.model.mockProfessionals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// ViewModel para el Agendamiento y Gestión de Citas
// ==========================================
class BookingViewModel(private val repository: AppointmentRepository) : ViewModel() {

    // ------------------------------------------
    // Estados Reactivos del Formulario y Lista de Citas
    // ------------------------------------------
    private val _uiState = MutableStateFlow(BookingFormState())
    val uiState: StateFlow<BookingFormState> = _uiState.asStateFlow()

    val appointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ------------------------------------------
    // Carga de Datos del Paciente Autenticado
    // ------------------------------------------
    fun loadPatientData(patient: UserEntity?) {
        if (patient != null) {
            _uiState.update {
                it.copy(
                    patientName = patient.name,
                    patientEmail = patient.email,
                    patientPhone = patient.phone,
                    patientRun = patient.run
                )
            }
        }
    }

    // ------------------------------------------
    // Filtrado de Profesionales por Especialidad
    // ------------------------------------------
    fun getAvailableProfessionals(dbProfessionals: List<UserEntity> = emptyList(), area: String): List<String> {
        val filteredFromDb = dbProfessionals.filter { it.specialty.equals(area, ignoreCase = true) }.map { it.name }
        return if (filteredFromDb.isNotEmpty()) {
            filteredFromDb
        } else {
            val filteredFromMock = mockProfessionals.filter { it.specialty.equals(area, ignoreCase = true) }.map { it.name }
            if (filteredFromMock.isNotEmpty()) filteredFromMock else listOf("Centro Sanaesencia")
        }
    }

    // ------------------------------------------
    // Manejadores de Eventos del Formulario de Agendamiento
    // ------------------------------------------
    fun onAreaChanged(area: String, defaultProfessional: String = "", dbProfessionals: List<UserEntity> = emptyList()) {
        val pro = if (defaultProfessional.isNotBlank()) {
            defaultProfessional
        } else {
            val pros = getAvailableProfessionals(dbProfessionals, area)
            pros.firstOrNull() ?: ""
        }
        _uiState.update { current ->
            current.copy(
                serviceType = area,
                professionalName = pro,
                errorMessage = null
            )
        }
        validateForm(dbProfessionals)
    }

    fun onSpecialtySelected(service: String) {
        onAreaChanged(service)
    }

    fun onProfessionalChanged(professional: String, dbProfessionals: List<UserEntity> = emptyList()) {
        _uiState.update { it.copy(professionalName = professional, errorMessage = null) }
        validateForm(dbProfessionals)
    }

    fun onDateChanged(newValue: String, dbProfessionals: List<UserEntity> = emptyList()) {
        _uiState.update { it.copy(date = newValue, errorMessage = null) }
        validateForm(dbProfessionals)
    }

    fun onTimeSlotChanged(newValue: String, dbProfessionals: List<UserEntity> = emptyList()) {
        _uiState.update { it.copy(timeSlot = newValue, errorMessage = null) }
        validateForm(dbProfessionals)
    }

    fun onNotesChanged(newValue: String) {
        _uiState.update { it.copy(notes = newValue) }
    }

    fun onModeChanged(isOnline: Boolean) {
        _uiState.update { it.copy(isOnline = isOnline) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ------------------------------------------
    // Validación de Integridad del Formulario
    // ------------------------------------------
    private fun validateForm(dbProfessionals: List<UserEntity> = emptyList()) {
        val state = _uiState.value
        val availablePros = getAvailableProfessionals(dbProfessionals, state.serviceType)
        val isProValid = state.professionalName.isNotBlank() && (availablePros.isEmpty() || availablePros.contains(state.professionalName))

        val isValid = state.serviceType.isNotBlank() &&
                state.professionalName.isNotBlank() &&
                isProValid &&
                state.date.isNotBlank() &&
                state.timeSlot.isNotBlank()

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    // ------------------------------------------
    // Cancelación y Eliminación de Citas
    // ------------------------------------------
    fun cancelAppointment(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.cancelAppointment(id)
            onSuccess()
        }
    }

    fun deleteAppointment(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAppointment(id)
            onSuccess()
        }
    }

    // ------------------------------------------
    // Confirmación y Envío de la Cita Médica
    // ------------------------------------------
    fun submitBooking(
        patient: UserEntity? = null,
        dbProfessionals: List<UserEntity> = emptyList(),
        onSuccess: (AppointmentEntity) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (!_uiState.value.isFormValid) {
            onError?.invoke("Por favor complete todos los campos requeridos")
            return
        }

        viewModelScope.launch {
            val state = _uiState.value

            // 1. Validar directamente en SQLite Room que el horario no esté tomado
            val alreadyTaken = repository.isSlotTaken(
                date = state.date.trim(),
                professionalName = state.professionalName.trim(),
                timeSlot = state.timeSlot.trim()
            )
            if (alreadyTaken) {
                onError?.invoke("Este horario ya ha sido reservado con el profesional.")
                return@launch
            }

            // 2. Para modalidad presencial, buscar un box libre (1..8) para evitar colisiones de sala
            val occupiedBoxes = repository.getOccupiedBoxes(state.date.trim(), state.timeSlot.trim())
            val freeBoxes = (1..8).filter { it !in occupiedBoxes }
            val assignedBox = if (state.isOnline) null else (freeBoxes.firstOrNull() ?: (1..8).random())

            val resolvedPatientRun = state.patientRun.ifBlank { patient?.run.orEmpty() }
            val resolvedPatientName = state.patientName.ifBlank { patient?.name ?: "Paciente Registrado" }
            val resolvedPatientEmail = state.patientEmail.ifBlank { patient?.email.orEmpty() }
            val resolvedPatientPhone = state.patientPhone.ifBlank { patient?.phone.orEmpty() }
            val resolvedProRun = dbProfessionals.find { it.name.equals(state.professionalName, ignoreCase = true) }?.run.orEmpty()

            val entity = AppointmentEntity(
                patientName = resolvedPatientName,
                patientEmail = resolvedPatientEmail,
                patientPhone = resolvedPatientPhone,
                patientRun = resolvedPatientRun,
                professionalRun = resolvedProRun,
                serviceType = state.serviceType,
                professionalName = state.professionalName,
                date = state.date,
                timeSlot = state.timeSlot,
                isOnline = state.isOnline,
                boxNumber = assignedBox,
                patientNotes = state.notes
            )

            val success = repository.saveAppointment(entity)
            if (success) {
                _uiState.update { it.copy(isSubmittedSuccessfully = true, errorMessage = null) }
                onSuccess(entity)
            } else {
                onError?.invoke("Error al guardar la cita en el sistema.")
            }
        }
    }
}

// ==========================================
// Factoría para Instanciación de BookingViewModel
// ==========================================
class BookingViewModelFactory(
    private val repository: AppointmentRepository
) : ViewModelProvider.Factory {

    // ------------------------------------------
    // Creación y Casteo de la Instancia de ViewModel
    // ------------------------------------------
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            return BookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
