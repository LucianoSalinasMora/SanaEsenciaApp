package com.sanaesencia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanaesencia.app.data.model.AboutInfoEntity
import com.sanaesencia.app.data.repository.AboutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
            // Seed initial about info if not present
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
