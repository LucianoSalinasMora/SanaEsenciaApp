package com.sanaesencia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanaesencia.app.data.model.UserEntity
import com.sanaesencia.app.data.repository.UserRepository
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.data.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
