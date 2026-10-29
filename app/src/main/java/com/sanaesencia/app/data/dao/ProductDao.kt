package com.sanaesencia.app.data.dao

import com.sanaesencia.app.data.model.ProductEntity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ==========================================
// Interfaz DAO para Productos y Catálogo Natural
// ==========================================
@Dao
interface ProductDao {

    // ------------------------------------------
    // Operaciones de Inserción (Individual y en Lote)
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // ------------------------------------------
    // Operaciones de Actualización y Eliminación
    // ------------------------------------------
    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // ------------------------------------------
    // Consultas Reactivas de Productos
    // ------------------------------------------
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsById(): Flow<List<ProductEntity>>

    // ------------------------------------------
    // Conteo Total de Productos
    // ------------------------------------------
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}

