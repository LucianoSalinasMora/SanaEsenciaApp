package com.sanaesencia.app.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// ==========================================
// 1. DTOs PARA API EXTERNA (Pronóstico / Clima Los Vilos)
// ==========================================
data class OpenMeteoWeatherResponse(
    val current_weather: CurrentWeatherDto? = null
)

data class CurrentWeatherDto(
    val temperature: Double = 0.0,
    val windspeed: Double = 0.0,
    val weathercode: Int = 0,
    val time: String = ""
)

// ==========================================
// 2. DTOs PARA MICROSERVICIOS SPRING BOOT
// ==========================================
data class LoginRequestDto(
    val run: String,
    val password: String
)

data class LoginResponseDto(
    val success: Boolean,
    val token: String? = null,
    val user: UserDto? = null,
    val message: String? = null
)

data class UserDto(
    val run: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val role: String = "PACIENTE",
    val specialty: String = ""
)

data class AppointmentDto(
    val id: Long = 0,
    val patientName: String = "",
    val patientEmail: String = "",
    val patientPhone: String = "",
    val patientRun: String = "",
    val professionalRun: String = "",
    val serviceType: String = "",
    val professionalName: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val isOnline: Boolean = false,
    val boxNumber: Int? = null,
    val status: String = "Agendada",
    val state: String = "PENDIENTE",
    val patientNotes: String = "",
    val turnoId: Long? = null
)

data class BoxDto(
    val boxNumber: Int,
    val name: String = "Box $boxNumber",
    val status: String = "disponible",
    val description: String = "",
    val currentAppointmentId: Long? = null
)

data class TurnoDto(
    val id: Long = 0,
    val date: String = "",
    val timeSlot: String = "",
    val professionalRun: String = "",
    val professionalName: String = "",
    val serviceType: String = "",
    val boxNumber: Int? = null,
    val status: String = "disponible",
    val isOnline: Boolean = false,
    val appointmentId: Long? = null
)

data class ProductDto(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val stock: Int = 10
)

// ==========================================
// 3. INTERFACES RETROFIT (API EXTERNA Y MICROSERVICIOS)
// ==========================================
/**
 * Interfaz para consumo de API Externa pública (Open-Meteo: Clima en tiempo real para Los Vilos)
 */
interface ExternalWeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double = -31.9123,
        @Query("longitude") longitude: Double = -71.5164,
        @Query("current_weather") currentWeather: Boolean = true
    ): Response<OpenMeteoWeatherResponse>
}

/**
 * Interfaz de comunicación con microservicios desarrollados en Spring Boot
 */
interface SpringBootApiService {

    // --- Endpoints de Usuarios / Auth ---
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/users/register")
    suspend fun registerUser(@Body user: UserDto): Response<UserDto>

    @GET("api/users/{run}")
    suspend fun getUserByRun(@Path("run") run: String): Response<UserDto>

    @GET("api/users/role/{role}")
    suspend fun getUsersByRole(@Path("role") role: String): Response<List<UserDto>>

    // --- Endpoints de Citas Médicas ---
    @GET("api/appointments")
    suspend fun getAppointments(): Response<List<AppointmentDto>>

    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: AppointmentDto): Response<AppointmentDto>

    @PUT("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: Long): Response<Unit>

    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    // --- Endpoints de Boxes de Atención ---
    @GET("api/boxes")
    suspend fun getBoxes(): Response<List<BoxDto>>

    @GET("api/boxes/available")
    suspend fun getAvailableBoxes(): Response<List<BoxDto>>

    @PUT("api/boxes/{boxNumber}/status")
    suspend fun updateBoxStatus(
        @Path("boxNumber") boxNumber: Int,
        @Query("status") status: String
    ): Response<BoxDto>

    // --- Endpoints de Turnos ---
    @GET("api/turnos")
    suspend fun getTurnos(): Response<List<TurnoDto>>

    @POST("api/turnos")
    suspend fun createTurno(@Body turno: TurnoDto): Response<TurnoDto>

    @PUT("api/turnos/{id}/status")
    suspend fun updateTurnoStatus(
        @Path("id") id: Long,
        @Query("status") status: String
    ): Response<TurnoDto>

    // --- Endpoints de Productos ---
    @GET("api/products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductDto): Response<ProductDto>
}

// ==========================================
// 4. CLIENTE RETROFIT SINGLETON
// ==========================================
object RetrofitClient {

    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    
    // IP 10.0.2.2 redirige al localhost de la máquina host en el emulador Android
    @Volatile
    var backendBaseUrl: String = "http://10.0.2.2:8080/"
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            rebuildBackendApi()
        }

    // Instancia Retrofit para API Externa
    val weatherApi: ExternalWeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExternalWeatherApiService::class.java)
    }

    // Instancia Retrofit dinámica para Microservicios Spring Boot
    @Volatile
    private var _backendApi: SpringBootApiService? = null

    val backendApi: SpringBootApiService
        get() = _backendApi ?: synchronized(this) {
            _backendApi ?: rebuildBackendApi()
        }

    private fun rebuildBackendApi(): SpringBootApiService {
        val instance = Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpringBootApiService::class.java)
        _backendApi = instance
        return instance
    }
}

// ==========================================
// 5. EXTENSIONES MAPPER (ENTITY <-> DTO)
// ==========================================
fun AppointmentEntity.toDto(): AppointmentDto = AppointmentDto(
    id = id,
    patientName = patientName,
    patientEmail = patientEmail,
    patientPhone = patientPhone,
    patientRun = patientRun,
    professionalRun = professionalRun,
    serviceType = serviceType,
    professionalName = professionalName,
    date = date,
    timeSlot = timeSlot,
    isOnline = isOnline,
    boxNumber = boxNumber,
    status = status,
    state = state,
    patientNotes = patientNotes,
    turnoId = turnoId
)

fun AppointmentDto.toEntity(): AppointmentEntity = AppointmentEntity(
    id = id,
    patientName = patientName,
    patientEmail = patientEmail,
    patientPhone = patientPhone,
    patientRun = patientRun,
    professionalRun = professionalRun,
    serviceType = serviceType,
    professionalName = professionalName,
    date = date,
    timeSlot = timeSlot,
    isOnline = isOnline,
    boxNumber = boxNumber,
    status = status,
    state = state,
    patientNotes = patientNotes,
    turnoId = turnoId
)

fun BoxEntity.toDto(): BoxDto = BoxDto(
    boxNumber = boxNumber,
    name = name,
    status = status,
    description = description,
    currentAppointmentId = currentAppointmentId
)

fun BoxDto.toEntity(): BoxEntity = BoxEntity(
    boxNumber = boxNumber,
    name = name,
    status = status,
    description = description,
    currentAppointmentId = currentAppointmentId
)

fun TurnoEntity.toDto(): TurnoDto = TurnoDto(
    id = id,
    date = date,
    timeSlot = timeSlot,
    professionalRun = professionalRun,
    professionalName = professionalName,
    serviceType = serviceType,
    boxNumber = boxNumber,
    status = status,
    isOnline = isOnline,
    appointmentId = appointmentId
)

fun TurnoDto.toEntity(): TurnoEntity = TurnoEntity(
    id = id,
    date = date,
    timeSlot = timeSlot,
    professionalRun = professionalRun,
    professionalName = professionalName,
    serviceType = serviceType,
    boxNumber = boxNumber,
    status = status,
    isOnline = isOnline,
    appointmentId = appointmentId
)
