package com.sanaesencia.app.data.dao

import com.sanaesencia.app.data.model.AboutInfoEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ==========================================
// Interfaz DAO para Información "Nosotros / Acerca de"
// ==========================================
@Dao
interface AboutDao {

    // ------------------------------------------
    // Consulta reactiva de información general
    // ------------------------------------------
    @Query("SELECT * FROM about_info WHERE id = 1")
    fun getAboutInfo(): Flow<AboutInfoEntity?>

    // ------------------------------------------
    // Inserción / Actualización de información
    // ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAboutInfo(info: AboutInfoEntity)
}
