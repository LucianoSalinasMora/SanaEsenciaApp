package com.sanaesencia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sanaesencia.app.data.model.ProductEntity
import com.sanaesencia.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
