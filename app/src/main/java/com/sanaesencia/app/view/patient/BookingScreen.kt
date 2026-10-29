package com.sanaesencia.app.view.patient

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.AppointmentEntity
import com.sanaesencia.app.data.util.LocationHelper
import com.sanaesencia.app.data.model.isCancelled
import com.sanaesencia.app.data.model.mockProfessionals
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

// ==========================================
// Pantalla de Agendamiento de Citas Médicas y Terapéuticas
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel,
    onBookingSuccess: (AppointmentEntity) -> Unit,
    onBack: () -> Unit
) {
    // ------------------------------------------
    // Observación de Estados Reactivos y Contexto
    // ------------------------------------------
    val currentUser by userViewModel.currentUser.collectAsState()
    val dbProfessionals by userViewModel.professionals.collectAsState()
    val allAppointments by bookingViewModel.appointments.collectAsState()
    val state by bookingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Integración de GPS para orientar la elección de modalidad (Presencial vs Online)
    val locationHelper = remember { LocationHelper(context) }
    var isCheckingGps by remember { mutableStateOf(false) }
    var gpsGpsSuggestion by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                isCheckingGps = true
                locationHelper.checkProximityToCenter { isNear, dist, err ->
                    isCheckingGps = false
                    if (err != null) {
                        gpsGpsSuggestion = err
                    } else {
                        val distFormatted = String.format(Locale.getDefault(), "%.1f", dist)
                        if (isNear) {
                            bookingViewModel.onModeChanged(false)
                            gpsGpsSuggestion = "GPS: Estás a $distFormatted km de la sede (<20 km). Sugerimos atención presencial."
                        } else {
                            bookingViewModel.onModeChanged(true)
                            gpsGpsSuggestion = "GPS: Estás a $distFormatted km de Los Vilos (>20 km). Sugerimos atención Online vía MEDIlink."
                        }
                    }
                }
            } else {
                gpsGpsSuggestion = "Permiso de ubicación denegado. Puedes seleccionar tu modalidad manualmente."
            }
        }
    )

    fun orientarModalidadGps() {
        if (locationHelper.hasLocationPermission()) {
            isCheckingGps = true
            locationHelper.checkProximityToCenter { isNear, dist, err ->
                isCheckingGps = false
                if (err != null) {
                    gpsGpsSuggestion = err
                } else {
                    val distFormatted = String.format(Locale.getDefault(), "%.1f", dist)
                    if (isNear) {
                        bookingViewModel.onModeChanged(false)
                        gpsGpsSuggestion = "GPS: Estás a $distFormatted km de la sede (<20 km). Sugerimos atención presencial."
                    } else {
                        bookingViewModel.onModeChanged(true)
                        gpsGpsSuggestion = "GPS: Estás a $distFormatted km de Los Vilos (>20 km). Sugerimos atención Online vía MEDIlink."
                    }
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Cargar automáticamente los datos del paciente logueado
    LaunchedEffect(currentUser) {
        bookingViewModel.loadPatientData(currentUser)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    // Lista de Áreas / Especialidades del Centro
    val areas = listOf(
        "Psicología Infanto-Juvenil",
        "Psicología Adolescentes y Adultos",
        "Terapia Ocupacional",
        "Fonoaudiología",
        "Psicopedagogía",
        "Nutrición Integrativa",
        "Matronería y Salud Femenina",
        "Quiropraxia y Masajes",
        "Terapia Floral y Aromaterapia",
        "Evaluaciones del Neurodesarrollo",
        "Medicina General y Salud Mental",
        "Acupuntura"
    )

    // Función para obtener profesionales de un área específica
    fun getProfessionalsForArea(area: String): List<String> {
        val filteredFromDb = dbProfessionals.filter { it.specialty.equals(area, ignoreCase = true) }.map { it.name }
        return if (filteredFromDb.isNotEmpty()) {
            filteredFromDb
        } else {
            val filteredFromMock = mockProfessionals.filter { it.specialty.equals(area, ignoreCase = true) }.map { it.name }
            if (filteredFromMock.isNotEmpty()) filteredFromMock else listOf("Centro Sanaesencia")
        }
    }

    // Filtrar profesionales disponibles según la especialidad seleccionada
    val availableProfessionals = remember(state.serviceType, dbProfessionals) {
        getProfessionalsForArea(state.serviceType)
    }

    // Preseleccionar automáticamente el primer profesional del área si el actual no pertenece a la misma
    LaunchedEffect(state.serviceType, availableProfessionals) {
        if (availableProfessionals.isNotEmpty() && state.professionalName !in availableProfessionals) {
            bookingViewModel.onProfessionalChanged(availableProfessionals.first(), dbProfessionals)
        }
    }

    // Lista de todos los posibles bloques de horario
    val allTimeSlots = listOf(
        "09:00 AM",
        "10:00 AM",
        "11:00 AM",
        "12:00 PM",
        "15:00 PM",
        "16:00 PM",
        "17:00 PM"
    )

    // Filtrar horarios disponibles: no pasados si es hoy, y no ocupados con el profesional
    val availableTimeSlots = remember(state.date, state.professionalName, allAppointments) {
        val todayStr = LocalDate.now().toString()
        val now = LocalTime.now()

        allTimeSlots.filter { slot ->
            // 1. Si la fecha es hoy, descartar si la hora ya pasó
            val isPassedToday = if (state.date == todayStr) {
                now.isAfter(parseSlotToLocalTime(slot))
            } else {
                false
            }

            // 2. Descartar si el profesional ya tiene una cita activa agendada en esa fecha y horario
            val isTakenByProfessional = if (state.date.isNotBlank() && state.professionalName.isNotBlank()) {
                allAppointments.any { cita ->
                    cita.date == state.date &&
                    cita.professionalName.equals(state.professionalName, ignoreCase = true) &&
                    cita.timeSlot == slot &&
                    !cita.isCancelled
                }
            } else {
                false
            }

            !isPassedToday && !isTakenByProfessional
        }
    }

    // Si el horario previamente seleccionado ya no está disponible, limpiarlo
    LaunchedEffect(availableTimeSlots) {
        if (state.timeSlot.isNotBlank() && state.timeSlot !in availableTimeSlots) {
            bookingViewModel.onTimeSlotChanged("", dbProfessionals)
        }
    }

    var areaExpanded by remember { mutableStateOf(false) }
    var proExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendamiento de Citas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Alert Dialog / Card si ocurre un conflicto de solapamiento o validación
            state.errorMessage?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aviso de Validación", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        IconButton(onClick = { bookingViewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // TARJETA DE DATOS DE PACIENTE (Cargados automáticamente de la sesión)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Datos del Paciente (Cargados automáticamente)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Nombre: ${state.patientName.ifBlank { currentUser?.name ?: "Paciente" }}", style = MaterialTheme.typography.bodyMedium)
                    Text("RUN: ${state.patientRun.ifBlank { currentUser?.run ?: "N/A" }} • Tel: ${state.patientPhone.ifBlank { currentUser?.phone ?: "Sin registro" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Correo: ${state.patientEmail.ifBlank { currentUser?.email ?: "Sin registro" }}", style = MaterialTheme.typography.bodySmall)
                }
            }

            // 1. SELECCIÓN DE ÁREA / ESPECIALIDAD
            Text("1. Área o Especialidad Requerida", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ExposedDropdownMenuBox(
                expanded = areaExpanded,
                onExpandedChange = { areaExpanded = !areaExpanded }
            ) {
                OutlinedTextField(
                    value = state.serviceType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Área de Atención") },
                    leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = areaExpanded,
                    onDismissRequest = { areaExpanded = false }
                ) {
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area) },
                            onClick = {
                                val pros = getProfessionalsForArea(area)
                                val firstPro = pros.firstOrNull() ?: ""
                                bookingViewModel.onAreaChanged(area, firstPro, dbProfessionals)
                                areaExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. SELECCIÓN DE PROFESIONAL DEL ÁREA (Filtrado dinámicamente)
            Text("2. Profesional del Área", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ExposedDropdownMenuBox(
                expanded = proExpanded,
                onExpandedChange = { proExpanded = !proExpanded }
            ) {
                OutlinedTextField(
                    value = state.professionalName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profesional Atendiente") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = proExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = proExpanded,
                    onDismissRequest = { proExpanded = false }
                ) {
                    availableProfessionals.forEach { proName ->
                        DropdownMenuItem(
                            text = { Text(proName) },
                            onClick = {
                                bookingViewModel.onProfessionalChanged(proName, dbProfessionals)
                                proExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. SELECCIÓN DE FECHA
            Text("3. Fecha de Atención", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = state.date,
                onValueChange = { },
                readOnly = true,
                label = { Text("Seleccionar Fecha en Calendario") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // 4. SELECCIÓN DE HORARIO (Bloquea/Deshabilita bloques ya reservados)
            Text("4. Horario / Módulo Disponible", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (state.date.isBlank()) {
                Text(
                    text = "Seleccione una fecha de atención en el paso anterior para ver los horarios disponibles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else if (availableTimeSlots.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No quedan horarios disponibles para esta fecha con el profesional seleccionado. Por favor seleccione otra fecha u otro profesional.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                availableTimeSlots.chunked(4).forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            FilterChip(
                                selected = state.timeSlot == slot,
                                onClick = { bookingViewModel.onTimeSlotChanged(slot, dbProfessionals) },
                                label = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            // 5. MODALIDAD
            Text("5. Modalidad de Atención", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = !state.isOnline,
                    onClick = { bookingViewModel.onModeChanged(false) },
                    label = { Text("Presencial (Los Vilos)") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = state.isOnline,
                    onClick = { bookingViewModel.onModeChanged(true) },
                    label = { Text("Online (MEDIlink)") },
                    leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null) }
                )
            }

            // Asistente GPS para elección de modalidad (orientación presencial vs online)
            AssistChip(
                onClick = { orientarModalidadGps() },
                label = {
                    Text(
                        if (isCheckingGps) "Consultando GPS..." else "Comprobar cercanía por GPS",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    if (isCheckingGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            AnimatedVisibility(
                visible = gpsGpsSuggestion != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                gpsGpsSuggestion?.let { suggestion ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 6. INFORMACIÓN RELEVANTE / SÍNTOMAS PREVIOS
            Text("6. Información relevante o antecedentes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = state.notes,
                onValueChange = { bookingViewModel.onNotesChanged(it) },
                label = { Text("Describa motivo o síntomas previos") },
                placeholder = { Text("Escriba un breve párrafo con antecedentes importantes para el profesional...") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = state.isFormValid,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Área, Profesional, Fecha y Horario seleccionados correctamente", color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    bookingViewModel.submitBooking(
                        onSuccess = { appointment ->
                            onBookingSuccess(appointment)
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = state.isFormValid && state.date.isNotBlank() && availableTimeSlots.contains(state.timeSlot),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Confirmar y Guardar Cita")
            }
        }
    }

    // ------------------------------------------
    // Selector de Fecha (DatePicker Dialog con Validación de Días Hábiles)
    // ------------------------------------------
    // Componente Calendario
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = DateValidators.weekdayOnlyValidator
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = formatter.format(Date(millis))
                        bookingViewModel.onDateChanged(formattedDate, dbProfessionals)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ==========================================
// Validador de Fechas Disponibles (Solo Días Hábiles y No Pasados)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
object DateValidators {
    val weekdayOnlyValidator = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val date = Instant.ofEpochMilli(utcTimeMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val isPast = date.isBefore(LocalDate.now())
            val isWeekend = date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7

            return !isPast && !isWeekend
        }
    }
}

// ==========================================
// Función Auxiliar: Parseo de Cadena de Horario a LocalTime
// ==========================================
private fun parseSlotToLocalTime(slot: String): LocalTime {
    val clean = slot.trim()
    val parts = clean.split(" ")
    val timeParts = parts.getOrNull(0)?.split(":") ?: emptyList()
    var hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val isPm = parts.getOrNull(1)?.equals("PM", ignoreCase = true) == true

    if (isPm && hour in 1..11) {
        hour += 12
    } else if (!isPm && hour == 12) {
        hour = 0
    }
    return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}
