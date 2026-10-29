package com.sanaesencia.app.view.patient

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sanaesencia.app.R
import com.sanaesencia.app.data.util.ContactHelper
import com.sanaesencia.app.data.util.LocationHelper
import com.sanaesencia.app.data.util.NosotrosInfo
import com.sanaesencia.app.data.util.NosotrosManager
import java.util.Locale

// ==========================================
// Pantalla Institucional: Nosotros y Ubicación de la Sede
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NosotrosScreen(role: String = "PACIENTE") {
    val context = LocalContext.current
    val nosotrosInfo by NosotrosManager.infoState.collectAsState()
    val mapsUrl = "https://maps.app.goo.gl/3hFvN2dRbjQkc5vq5"

    fun openGoogleMaps() {
        val intent = Intent(Intent.ACTION_VIEW, mapsUrl.toUri())
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir la aplicación de mapas", Toast.LENGTH_SHORT).show()
        }
    }

    // Integración de Recurso Nativo: GPS (LocationHelper)
    val locationHelper = remember { LocationHelper(context) }
    var isCheckingGps by remember { mutableStateOf(false) }
    var gpsDistanceKm by remember { mutableStateOf<Float?>(null) }
    var gpsIsNear by remember { mutableStateOf<Boolean?>(null) }
    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasLocationPermission by remember { mutableStateOf(locationHelper.hasLocationPermission()) }

    // Launcher para solicitar permisos de GPS en tiempo de ejecución
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            hasLocationPermission = fineGranted || coarseGranted
            if (hasLocationPermission) {
                isCheckingGps = true
                gpsErrorMessage = null
                locationHelper.checkProximityToCenter { isNear, dist, err ->
                    isCheckingGps = false
                    gpsIsNear = isNear
                    gpsDistanceKm = dist
                    gpsErrorMessage = err
                }
            } else {
                isCheckingGps = false
                gpsErrorMessage = "Para calcular la distancia y orientar la llegada a la sede presencial, concede los permisos de ubicación."
            }
        }
    )

    fun consultarGps() {
        if (locationHelper.hasLocationPermission()) {
            isCheckingGps = true
            gpsErrorMessage = null
            locationHelper.checkProximityToCenter { isNear, dist, err ->
                isCheckingGps = false
                gpsIsNear = isNear
                gpsDistanceKm = dist
                gpsErrorMessage = err
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

    LaunchedEffect(Unit) {
        NosotrosManager.init(context)
    }

    var mostrarDialogoEditar by remember { mutableStateOf(false) }

    // Reseñas de ejemplo (ficticias, solo en memoria). El admin puede eliminarlas.
    val resenas = remember {
        mutableStateListOf(
            "Paciente 1" to "Excelente atención y un espacio muy acogedor.",
            "Paciente 2" to "Pude agendar mi hora online sin complicaciones.",
            "Paciente 3" to "Los profesionales explican todo con mucha claridad."
        )
    }

    val especialidades = listOf(
        "Psicología Infanto-Juvenil",
        "Psicología Adolescentes y Adultos",
        "Terapia Ocupacional",
        "Fonoaudiología",
        "Psicopedagogía",
        "Nutrición Integrativa",
        "Matronería y Salud Femenina",
        "Quiropraxia y Masajes",
        "Evaluaciones del Neurodesarrollo",
        "Medicina General y Salud Mental",
        "Terapia Floral y Aromaterapia",
        "Acupuntura"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nosotros",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (role == "ADMIN") {
                    Button(onClick = { mostrarDialogoEditar = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Información")
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(nosotrosInfo.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        nosotrosInfo.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contacto y Horarios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Dirección: ${nosotrosInfo.address}", style = MaterialTheme.typography.bodyMedium)
                    Text("Teléfono / WhatsApp: ${nosotrosInfo.phone}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Correo: ${nosotrosInfo.email}", style = MaterialTheme.typography.bodyMedium)
                    Text("Horario de Atención: ${nosotrosInfo.schedule}", style = MaterialTheme.typography.bodyMedium)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ContactHelper.openWhatsApp(
                                    context = context,
                                    rawPhone = nosotrosInfo.phone,
                                    message = "Hola Centro Sanaesencia, me gustaría solicitar información."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chatear WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ContactHelper.addContact(
                                    context = context,
                                    name = nosotrosInfo.title,
                                    phone = nosotrosInfo.phone,
                                    email = nosotrosInfo.email,
                                    address = nosotrosInfo.address
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Contacto")
                        }
                    }
                }
            }
        }

        // ------------------------------------------
        // Tarjeta de Ubicación y Mapa Interactivo
        // ------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openGoogleMaps() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_map_preview),
                            contentDescription = "Mapa Ubicación Sanaesencia Caupolicán 220",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Abrir Google Maps",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Abrir Maps",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ubicación Presencial",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = nosotrosInfo.address,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Toca la imagen o presiona el botón para abrir la ruta en Google Maps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { openGoogleMaps() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Ubicación en Google Maps")
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nuestras Especialidades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    especialidades.forEach { esp ->
                        Text("• $esp", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Solo el administrador gestiona las reseñas
        if (role == "ADMIN") {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gestión de Reseñas", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Reseñas publicadas por pacientes (puedes administrarlas o eliminarlas).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (resenas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            "No hay reseñas para mostrar.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                itemsIndexed(resenas) { index, resena ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(resena.first, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(resena.second, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { resenas.removeAt(index) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar reseña",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Edición del apartado Nosotros para el ADMIN
    if (mostrarDialogoEditar && role == "ADMIN") {
        var titulo by remember { mutableStateOf(nosotrosInfo.title) }
        var descripcion by remember { mutableStateOf(nosotrosInfo.description) }
        var direccion by remember { mutableStateOf(nosotrosInfo.address) }
        var telefono by remember { mutableStateOf(nosotrosInfo.phone) }
        var correo by remember { mutableStateOf(nosotrosInfo.email) }
        var horario by remember { mutableStateOf(nosotrosInfo.schedule) }

        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            title = { Text("Editar Apartado Nosotros") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it.replace("\n", "") },
                        label = { Text("Nombre del Centro") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it.replace("\n", "") },
                        label = { Text("Descripción / Misión") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it.replace("\n", "") },
                        label = { Text("Dirección") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it.replace("\n", "") },
                        label = { Text("Teléfono / WhatsApp") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it.replace("\n", "") },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = horario,
                        onValueChange = { horario = it.replace("\n", "") },
                        label = { Text("Horario de Atención") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = NosotrosInfo(
                            title = titulo.trim(),
                            description = descripcion.trim(),
                            address = direccion.trim(),
                            phone = telefono.trim(),
                            email = correo.trim(),
                            schedule = horario.trim()
                        )
                        NosotrosManager.saveInfo(context, updated)
                        mostrarDialogoEditar = false
                    },
                    enabled = titulo.isNotBlank() && telefono.isNotBlank()
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditar = false }) { Text("Cancelar") }
            }
        )
    }
}
