package com.sanaesencia.app.data.repository

import com.sanaesencia.app.data.dao.ProductDao
import com.sanaesencia.app.data.model.ProductEntity

import kotlinx.coroutines.flow.Flow

// ==========================================
// Repositorio para la Gestión de Productos
// ==========================================
class ProductRepository(private val productDao: ProductDao) {

    // ------------------------------------------
    // Flujos Reactivos de Productos
    // ------------------------------------------
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val availableProducts: Flow<List<ProductEntity>> = productDao.getAvailableProducts()

    // ------------------------------------------
    // Operaciones CRUD de Productos
    // ------------------------------------------
    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    // ------------------------------------------
    // Inicialización y Sembrado de Productos por Defecto
    // ------------------------------------------
    suspend fun seedInitialProducts() {
        if (productDao.getCount() == 0) {
            val defaultProducts = listOf(
                ProductEntity(
                    name = "Kit de Bienestar",
                    description = "Set de aromaterapia con roll-on y bruma.",
                    price = 24990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Roll-on Calma",
                    description = "Aceites esenciales para momentos de estrés.",
                    price = 7990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Bruma de Almohada",
                    description = "Bruma relajante para el descanso nocturno.",
                    price = 9990.0,
                    isAvailable = true
                ),
                ProductEntity(
                    name = "Roll-on Foco",
                    description = "Aroma para concentración y estudio.",
                    price = 7990.0,
                    isAvailable = false
                )
            )
            for (p in defaultProducts) {
                productDao.insertProduct(p)
            }
        }
    }
}
