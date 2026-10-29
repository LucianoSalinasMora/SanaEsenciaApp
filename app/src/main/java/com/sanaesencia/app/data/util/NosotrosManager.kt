package com.sanaesencia.app.data.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ==========================================
// Modelo de Información Institucional "Nosotros"
// ==========================================
data class NosotrosInfo(
    val title: String = "Centro Integrativo Sanaesencia",
    val description: String = "Salud mental infanto-juvenil, familia y bienestar integrativo. Atendemos de forma presencial en nuestros 8 boxes de Los Vilos y online para todo Chile y el extranjero.",
    val address: String = "Caupolicán 220, Los Vilos, Región de Coquimbo, Chile",
    val phone: String = "+56 9 2366 5073",
    val email: String = "contacto@centrosanaesencia.cl",
    val schedule: String = "Lunes a Viernes 09:00 - 19:00 hrs"
)

// ==========================================
// Administrador de Persistencia de "Nosotros" con SharedPreferences
// ==========================================
object NosotrosManager {
    // ------------------------------------------
    // Constantes de Claves de SharedPreferences
    // ------------------------------------------
    private const val PREFS_NAME = "nosotros_prefs"
    private const val KEY_TITLE = "title"
    private const val KEY_DESC = "description"
    private const val KEY_ADDRESS = "address"
    private const val KEY_PHONE = "phone"
    private const val KEY_EMAIL = "email"
    private const val KEY_SCHEDULE = "schedule"

    // ------------------------------------------
    // Estado Reactivo en Memoria
    // ------------------------------------------
    private val _infoState = MutableStateFlow(NosotrosInfo())
    val infoState: StateFlow<NosotrosInfo> = _infoState.asStateFlow()

    private var isInitialized = false

    // ------------------------------------------
    // Inicialización y Carga de Preferencias
    // ------------------------------------------
    fun init(context: Context) {
        if (!isInitialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _infoState.value = NosotrosInfo(
                title = prefs.getString(KEY_TITLE, "Centro Integrativo Sanaesencia") ?: "Centro Integrativo Sanaesencia",
                description = prefs.getString(
                    KEY_DESC,
                    "Salud mental infanto-juvenil, familia y bienestar integrativo. Atendemos de forma presencial en nuestros 8 boxes de Los Vilos y online para todo Chile y el extranjero."
                ) ?: "",
                address = prefs.getString(KEY_ADDRESS, "Caupolicán 220, Los Vilos, Región de Coquimbo, Chile") ?: "",
                phone = prefs.getString(KEY_PHONE, "+56 9 2366 5073") ?: "+56 9 2366 5073",
                email = prefs.getString(KEY_EMAIL, "contacto@centrosanaesencia.cl") ?: "contacto@centrosanaesencia.cl",
                schedule = prefs.getString(KEY_SCHEDULE, "Lunes a Viernes 09:00 - 19:00 hrs") ?: "Lunes a Viernes 09:00 - 19:00 hrs"
            )
            isInitialized = true
        }
    }

    // ------------------------------------------
    // Guardado y Actualización de Información
    // ------------------------------------------
    fun saveInfo(context: Context, newInfo: NosotrosInfo) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TITLE, newInfo.title)
            .putString(KEY_DESC, newInfo.description)
            .putString(KEY_ADDRESS, newInfo.address)
            .putString(KEY_PHONE, newInfo.phone)
            .putString(KEY_EMAIL, newInfo.email)
            .putString(KEY_SCHEDULE, newInfo.schedule)
            .apply()

        _infoState.value = newInfo
    }
}
