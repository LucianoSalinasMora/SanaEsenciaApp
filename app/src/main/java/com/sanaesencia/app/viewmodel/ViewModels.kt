package com.sanaesencia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanaesencia.app.data.AboutInfoEntity
import com.sanaesencia.app.data.AboutRepository
import com.sanaesencia.app.data.AppointmentEntity
import com.sanaesencia.app.data.AppointmentRepository
import com.sanaesencia.app.data.ProductEntity
import com.sanaesencia.app.data.ProductRepository
import com.sanaesencia.app.data.UserEntity
import com.sanaesencia.app.data.UserRepository
import com.sanaesencia.app.data.UserRole
import com.sanaesencia.app.data.Validators
import com.sanaesencia.app.data.mockProfessionals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// ViewModel para la Vista y Gestión de "Nosotros"
// ==========================================
class AboutViewModel(private val repository: AboutRepository) : ViewModel() {

    // ------------------------------------------
    // Flujo de Estado Reactivo de Información Institucional
    // ------------------------------------------
    val aboutInfo: StateFlow<AboutInfoEntity?> = repository.aboutInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AboutInfoEntity()
        )

    // ------------------------------------------
    // Inicialización y Sembrado Automático
    // ------------------------------------------
    init {
        viewModelScope.launch {
            repository.saveAboutInfo(AboutInfoEntity())
        }
    }

    // ------------------------------------------
    // Actualización de Información Institucional
    // ------------------------------------------
    fun updateAboutInfo(info: AboutInfoEntity) {
        viewModelScope.launch {
            repository.saveAboutInfo(info)
        }
    }
}

// ==========================================
// Factoría para Instanciación de AboutViewModel
// ==========================================
class AboutViewModelFactory(private val repository: AboutRepository) : ViewModelProvider.Factory {

    // ------------------------------------------
    // Creación y Casteo de la Instancia de ViewModel
    // ------------------------------------------
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AboutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AboutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

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
            ""
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

    fun resetBookingForm() {
        _uiState.update { current ->
            current.copy(
                serviceType = "",
                professionalName = "",
                date = "",
                timeSlot = "",
                notes = "",
                isFormValid = false,
                isSubmittedSuccessfully = false,
                errorMessage = null
            )
        }
    }

    // ------------------------------------------
    // Validación de Integridad del Formulario
    // ------------------------------------------
    private fun validateForm(dbProfessionals: List<UserEntity> = emptyList()) {
        val state = _uiState.value
        val availablePros = if (state.serviceType.isBlank()) emptyList() else getAvailableProfessionals(dbProfessionals, state.serviceType)
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

            val entity = AppointmentEntity(
                patientName = state.patientName.ifBlank { "Paciente Registrado" },
                patientEmail = state.patientEmail,
                patientPhone = state.patientPhone,
                patientRun = state.patientRun,
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
                _uiState.update { current ->
                    current.copy(
                        isSubmittedSuccessfully = true,
                        errorMessage = null,
                        notes = "",
                        date = "",
                        timeSlot = "",
                        serviceType = "",
                        professionalName = "",
                        isFormValid = false
                    )
                }
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

// ==========================================
// ViewModel para la Gestión de Productos y Catálogo
// ==========================================
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // ------------------------------------------
    // Flujos Reactivos de Productos (Totales y Disponibles)
    // ------------------------------------------
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableProducts: StateFlow<List<ProductEntity>> = repository.availableProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val products: StateFlow<List<ProductEntity>> get() = allProducts

    // ------------------------------------------
    // Inicialización y Sembrado de Productos
    // ------------------------------------------
    init {
        viewModelScope.launch {
            repository.seedInitialProducts()
        }
    }

    // ------------------------------------------
    // Operaciones de Creación, Actualización y Eliminación
    // ------------------------------------------
    fun addProduct(name: String, description: String, price: Double, isAvailable: Boolean = true, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertProduct(
                ProductEntity(
                    name = name,
                    description = description,
                    price = price,
                    isAvailable = isAvailable
                )
            )
            onSuccess()
        }
    }

    fun toggleAvailability(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(isAvailable = !product.isAvailable))
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onSuccess()
        }
    }

    fun deleteProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            onSuccess()
        }
    }
}

// ==========================================
// Factoría para Instanciación de ProductViewModel
// ==========================================
class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {

    // ------------------------------------------
    // Creación y Casteo de la Instancia de ViewModel
    // ------------------------------------------
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ==========================================
// ViewModel para Autenticación, Usuarios y Profesionales
// ==========================================
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // ------------------------------------------
    // Estado del Usuario Actualmente Autenticado
    // ------------------------------------------
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // ------------------------------------------
    // Estado y Validación Reactiva del Formulario de Login
    // ------------------------------------------
    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState.asStateFlow()

    // ------------------------------------------
    // Estado y Validación Reactiva del Formulario de Registro
    // ------------------------------------------
    private val _signUpFormState = MutableStateFlow(SignUpFormState())
    val signUpFormState: StateFlow<SignUpFormState> = _signUpFormState.asStateFlow()

    // ------------------------------------------
    // Flujo Reactivo de la Lista de Profesionales Activos
    // ------------------------------------------
    val professionals: StateFlow<List<UserEntity>> = repository.getUsersByRole(UserRole.PROFESIONAL)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ------------------------------------------
    // Inicialización y Sembrado de Usuarios
    // ------------------------------------------
    init {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    // ==========================================
    // MÉTODOS DE FORMULARIO DE LOGIN (DESACOPLADO)
    // ==========================================
    fun onLoginRunChanged(newRun: String) {
        val cleanRun = newRun.replace("\n", "").replace("\r", "")
        val isValid = cleanRun.isBlank() || Validators.isValidRUN(cleanRun)
        _loginFormState.update { current ->
            val isFormValid = cleanRun.isNotBlank() && isValid && current.password.isNotBlank() && current.isPasswordValid
            current.copy(
                run = cleanRun,
                isRunValid = isValid,
                isFormValid = isFormValid,
                errorMessage = null
            )
        }
    }

    fun onLoginPasswordChanged(newPassword: String) {
        val cleanPassword = newPassword.replace("\n", "").replace("\r", "")
        val isValid = cleanPassword.isBlank() || Validators.isValidPassword(cleanPassword)
        _loginFormState.update { current ->
            val isFormValid = current.run.isNotBlank() && current.isRunValid && cleanPassword.isNotBlank() && isValid
            current.copy(
                password = cleanPassword,
                isPasswordValid = isValid,
                isFormValid = isFormValid,
                errorMessage = null
            )
        }
    }

    fun submitLogin(onSuccess: (UserRole) -> Unit, onError: ((String) -> Unit)? = null) {
        val state = _loginFormState.value
        if (!state.isFormValid) {
            val err = "Complete los campos con formatos válidos."
            _loginFormState.update { it.copy(errorMessage = err) }
            onError?.invoke(err)
            return
        }

        viewModelScope.launch {
            _loginFormState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = repository.login(state.run.trim(), state.password.trim())
            _loginFormState.update { it.copy(isLoading = false) }
            if (user != null) {
                _currentUser.value = user
                onSuccess(user.role)
            } else {
                val errMsg = "Credenciales incorrectas. Verifique su RUN/Usuario y contraseña."
                _loginFormState.update { it.copy(errorMessage = errMsg) }
                onError?.invoke(errMsg)
            }
        }
    }

    fun resetLoginForm() {
        _loginFormState.value = LoginFormState()
    }

    // ==========================================
    // MÉTODOS DE FORMULARIO DE SIGN UP (DESACOPLADO)
    // ==========================================
    fun onSignUpRunChanged(newRun: String) {
        val clean = newRun.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || Validators.isValidRUN(clean)
        _signUpFormState.update { current ->
            val updated = current.copy(run = clean, isRunValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpNameChanged(newName: String) {
        val clean = newName.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || Validators.isValidName(clean)
        _signUpFormState.update { current ->
            val updated = current.copy(name = clean, isNameValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpEmailChanged(newEmail: String) {
        val clean = newEmail.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || Validators.isValidEmail(clean)
        _signUpFormState.update { current ->
            val updated = current.copy(email = clean, isEmailValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpPhoneChanged(newPhone: String) {
        val clean = newPhone.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || Validators.isValidPhone(clean)
        _signUpFormState.update { current ->
            val updated = current.copy(phone = clean, isPhoneValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpPasswordChanged(newPassword: String) {
        val clean = newPassword.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || Validators.isValidPassword(clean)
        _signUpFormState.update { current ->
            val updated = current.copy(password = clean, isPasswordValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpSecurityQuestionChanged(newQuestion: String) {
        val clean = newQuestion.replace("\n", "").replace("\r", "")
        _signUpFormState.update { current ->
            val updated = current.copy(securityQuestion = clean)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    fun onSignUpSecurityAnswerChanged(newAnswer: String) {
        val clean = newAnswer.replace("\n", "").replace("\r", "")
        val isValid = clean.isBlank() || clean.trim().length >= 2
        _signUpFormState.update { current ->
            val updated = current.copy(securityAnswer = clean, isSecurityAnswerValid = isValid, errorMessage = null)
            updated.copy(isFormValid = validateSignUpForm(updated))
        }
    }

    private fun validateSignUpForm(state: SignUpFormState): Boolean {
        return state.run.isNotBlank() && state.isRunValid &&
                state.name.isNotBlank() && state.isNameValid &&
                state.email.isNotBlank() && state.isEmailValid &&
                state.phone.isNotBlank() && state.isPhoneValid &&
                state.password.isNotBlank() && state.isPasswordValid &&
                state.securityQuestion.isNotBlank() &&
                state.securityAnswer.isNotBlank() && state.isSecurityAnswerValid
    }

    fun submitSignUp(onSuccess: () -> Unit, onError: ((String) -> Unit)? = null) {
        val state = _signUpFormState.value
        if (!state.isFormValid) {
            val err = "Complete todos los campos del formulario de registro."
            _signUpFormState.update { it.copy(errorMessage = err) }
            onError?.invoke(err)
            return
        }

        viewModelScope.launch {
            _signUpFormState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val existing = repository.getUserByRun(state.run.trim())
            if (existing != null) {
                _signUpFormState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Ya existe un usuario registrado con el RUN ${state.run.trim()}"
                    )
                }
                onError?.invoke("Ya existe un usuario registrado con este RUN.")
                return@launch
            }

            val newUser = UserEntity(
                run = state.run.trim(),
                name = state.name.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                passwordHash = state.password.trim(),
                role = UserRole.PACIENTE,
                securityQuestion = state.securityQuestion.trim(),
                securityAnswer = state.securityAnswer.trim()
            )
            repository.insertUser(newUser)
            _signUpFormState.update { it.copy(isSubmitting = false) }
            onSuccess()
        }
    }

    fun resetSignUpForm() {
        _signUpFormState.value = SignUpFormState()
    }

    // ==========================================
    // Operaciones Generales del Repositorio y Perfil
    // ------------------------------------------
    // Registro y Consulta de Usuarios
    // ------------------------------------------
    fun registerUser(user: UserEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertUser(user)
            onSuccess()
        }
    }

    fun login(run: String, password: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val user = repository.login(run, password)
            if (user != null) {
                _currentUser.value = user
            }
            onResult(user)
        }
    }

    // ------------------------------------------
    // Actualización de Perfil de Usuario
    // ------------------------------------------
    fun updateUserProfile(name: String, email: String, phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val updated = current.copy(name = name, email = email, phone = phone)
            repository.updateUser(updated)
            _currentUser.value = updated
            onSuccess()
        }
    }

    // ------------------------------------------
    // Gestión de Profesionales (Admin)
    // ------------------------------------------
    fun registerProfessional(user: UserEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertUser(user.copy(role = UserRole.PROFESIONAL))
            onSuccess()
        }
    }

    fun deleteProfessional(run: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(run)
            onSuccess()
        }
    }

    // ------------------------------------------
    // Recuperación de Contraseña y Cierre de Sesión
    // ------------------------------------------
    fun getUserByRun(run: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByRun(run)
            onResult(user)
        }
    }

    fun updatePassword(run: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updatePassword(run, newPassword)
            onSuccess()
        }
    }

    fun logout() {
        _currentUser.value = null
        resetLoginForm()
        resetSignUpForm()
    }
}

// ==========================================
// Factoría para Instanciación de UserViewModel
// ==========================================
class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {

    // ------------------------------------------
    // Creación y Casteo de la Instancia de ViewModel
    // ------------------------------------------
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
