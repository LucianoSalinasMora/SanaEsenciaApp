package com.sanaesencia.app.data.repository

import com.sanaesencia.app.data.dao.AboutDao
import com.sanaesencia.app.data.model.AboutInfoEntity

import kotlinx.coroutines.flow.Flow

// ==========================================
// Repositorio de Información Institucional "Nosotros"
// ==========================================
class AboutRepository(private val aboutDao: AboutDao) {

    // ------------------------------------------
    // Flujo Reactivo de Datos Institucionales
    // ------------------------------------------
    val aboutInfo: Flow<AboutInfoEntity?> = aboutDao.getAboutInfo()

    // ------------------------------------------
    // Operación de Guardado / Actualización
    // ------------------------------------------
    suspend fun saveAboutInfo(info: AboutInfoEntity) {
        aboutDao.insertAboutInfo(info)
    }
}
