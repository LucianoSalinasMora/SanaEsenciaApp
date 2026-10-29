package com.sanaesencia.app.data.dao

import com.sanaesencia.app.data.model.ReviewEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ==========================================
// Interfaz DAO para Reseñas y Testimonios
// ==========================================
@Dao
interface ReviewDao {

    // ------------------------------------------
    // Inserción de Nueva Reseña
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // ------------------------------------------
    // Consulta Reactiva de Reseñas (Orden Cronológico Inverso)
    // ------------------------------------------
    // Muestra las reseñas más recientes primero
    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>
}

