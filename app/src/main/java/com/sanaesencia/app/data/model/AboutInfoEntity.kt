package com.sanaesencia.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ==========================================
// Entidad Room: Información Institucional "Nosotros"
// ==========================================
@Entity(tableName = "about_info")
data class AboutInfoEntity(
    // Identificador único (fila única por defecto id = 1)
    @PrimaryKey val id: Int = 1,
    // Título y descripción general del centro
    val centerTitle: String = "Centro Integrativo Sanaesencia",
    val description: String = "Salud mental infanto-juvenil, familia y bienestar integrativo. Atendemos de forma presencial en nuestros 8 boxes de Los Vilos y online para todo Chile y el extranjero.",
    // Datos de contacto y ubicación
    val address: String = "Caupolicán 220, Los Vilos",
    val phone: String = "+56 9 2366 5073",
    val email: String = "contacto@centrosanaesencia.cl"
)
