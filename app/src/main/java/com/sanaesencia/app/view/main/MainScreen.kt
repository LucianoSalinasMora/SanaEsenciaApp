package com.sanaesencia.app.view.main

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sanaesencia.app.R
import com.sanaesencia.app.Screen
import com.sanaesencia.app.data.util.AppNotification
import com.sanaesencia.app.data.util.NotificationCategory
import com.sanaesencia.app.data.util.NotificationHelper
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.view.admin.AdminProfessionalsScreen
import com.sanaesencia.app.view.admin.ManageProductsScreen
import com.sanaesencia.app.view.admin.ManageScheduleScreen
import com.sanaesencia.app.view.patient.NosotrosScreen
import com.sanaesencia.app.view.patient.PatientProductsScreen
import com.sanaesencia.app.view.patient.ServiciosCatalogoScreen
import com.sanaesencia.app.view.professional.MyPatientsScreen
import com.sanaesencia.app.view.professional.MyScheduleScreen
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.ProductViewModel
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla Principal Contenedora (Dashboards, Pestañas y Roles)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    parentNavController: NavHostController,
    userRole: String, // Recibe "PACIENTE", "PROFESIONAL" o "ADMIN"
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel,
    productViewModel: ProductViewModel,
    notificationHelper: NotificationHelper,
    onLogout: () -> Unit // Acción para cerrar sesión
) {
    // ------------------------------------------
    // Controladores de Navegación y Estados de Diálogo
    // ------------------------------------------
    val context = LocalContext.current
    val bottomNavController = rememberNavController()

    // Solo el rol PACIENTE maneja e interactúa con notificaciones push
    val isPaciente = userRole == UserRole.PACIENTE.name

    // Estado del modal de notificaciones
    var showNotificationSheet by remember { mutableStateOf(false) }

    // Alerta de confirmación para cerrar sesión
    var mostrarDialogoLogout by remember { mutableStateOf(false) }

    // Notificaciones de la app (StateFlow)
    val notifications by NotificationHelper.notificationsState.collectAsState()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    // Launcher para pedir permiso POST_NOTIFICATIONS en Android 13+ (Solo si el usuario presiona permitir)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No recibirás notificaciones en la barra de estado", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Configuración de TABS según el Rol
    val navItems = when (userRole) {
        UserRole.ADMIN.name -> listOf(
            Screen.Inicio,             // Dashboard y Proyecciones
            Screen.ManageProducts,     // Gestión de Productos y Novedades
            Screen.ManageSchedule,     // Gestión de Turnos Global
            Screen.AdminProfessionals, // Editar Profesionales
            Screen.About               // Gestión de Reseñas
        )
        UserRole.PROFESIONAL.name -> listOf(
            Screen.Inicio,             // Turnos del día
            Screen.MyPatients,         // Seguimiento de Pacientes
            Screen.MySchedule,         // Turnos asignados
            Screen.Profile             // Datos del profesional
        )
        else -> listOf( // PACIENTE por defecto
            Screen.Inicio,
            Screen.Catalog,
            Screen.PatientProducts,
            Screen.About,
            Screen.Profile
        )
    }

    if (mostrarDialogoLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLogout = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoLogout = false
                    userViewModel.logout()
                    onLogout()
                }) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        // TopAppBar global con Logo, Campana (sólo para PACIENTE) y Cierre de Sesión
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Sanaesencia",
                        modifier = Modifier.height(36.dp)
                    )
                },
                actions = {
                    // Campana de Notificaciones sólo visible para PACIENTE
                    if (isPaciente) {
                        IconButton(onClick = {
                            notificationHelper.markAllAsRead()
                            showNotificationSheet = true
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones y Alertas")
                            }
                        }
                    }

                    IconButton(onClick = { mostrarDialogoLogout = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (userRole == UserRole.PACIENTE.name) {
                FloatingActionButton(
                    onClick = { parentNavController.navigate(Screen.Booking.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agendar Cita")
                }
            } else if (userRole == UserRole.PROFESIONAL.name) {
                FloatingActionButton(
                    onClick = { parentNavController.navigate(Screen.Booking.route) },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agendar Paciente")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(260)) },
            exitTransition = { fadeOut(animationSpec = tween(260)) },
            popEnterTransition = { fadeIn(animationSpec = tween(260)) },
            popExitTransition = { fadeOut(animationSpec = tween(260)) }
        ) {
            // Vistas compartidas
            composable(Screen.Inicio.route) {
                DashboardScreen(
                    viewModel = bookingViewModel,
                    role = userRole
                )
            }
            composable(Screen.Profile.route) { ProfileScreen(userViewModel = userViewModel, bookingViewModel = bookingViewModel) }
            composable(Screen.About.route) {
                NosotrosScreen(role = userRole)
            }

            // Vistas de Paciente
            composable(Screen.Catalog.route) {
                ServiciosCatalogoScreen(
                    userViewModel = userViewModel,
                    onAgendar = { parentNavController.navigate(Screen.Booking.route) },
                    onVerProductos = { bottomNavController.navigate(Screen.PatientProducts.route) }
                )
            }
            composable(Screen.PatientProducts.route) {
                PatientProductsScreen(productViewModel = productViewModel)
            }

            // Vistas de Profesional
            composable(Screen.MyPatients.route) {
                MyPatientsScreen(
                    bookingViewModel = bookingViewModel
                )
            }
            composable(Screen.MySchedule.route) { MyScheduleScreen(bookingViewModel = bookingViewModel, userViewModel = userViewModel) }

            // Vistas de Administrador
            composable(Screen.ManageProducts.route) { ManageProductsScreen(viewModel = productViewModel) }
            composable(Screen.ManageSchedule.route) { ManageScheduleScreen(bookingViewModel = bookingViewModel) }
            composable(Screen.AdminProfessionals.route) { AdminProfessionalsScreen(viewModel = userViewModel) }
        }

        // --- SHEET MODAL PARA VER NOTIFICACIONES PUSH Y ALERTAS (SÓLO PACIENTE) ---
        if (isPaciente && showNotificationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                NotificationCenterContent(
                    notifications = notifications,
                    notificationHelper = notificationHelper,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onClose = { showNotificationSheet = false }
                )
            }
        }
    }
}

// ==========================================
// Hoja Modal: Centro de Notificaciones y Alertas (Paciente)
// ==========================================
@Composable
fun NotificationCenterContent(
    notifications: List<AppNotification>,
    notificationHelper: NotificationHelper,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Centro de Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationHelper.hasNotificationPermission()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Permiso de Notificaciones Desactivado",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Para recibir las alertas en la barra de estado de tu teléfono, otorga el permiso de sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Permitir Notificaciones")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    notificationHelper.sendFollowUpNotification(
                        patientName = "Paciente",
                        serviceName = "Terapia Medicina Biorreguladora",
                        customMessage = "Recordatorio: Tu próximo control de seguimiento está sugerido para esta semana."
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Probar Seguimiento", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    notificationHelper.sendPromoNotification(
                        title = "¡Novedad de Temporada!",
                        description = "Obtén 25% de descuento en Aromaterapia y Sanación Holística.",
                        promoCode = "SALUD25"
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Probar Promo", style = MaterialTheme.typography.labelSmall)
            }
        }

        HorizontalDivider()

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes notificaciones registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCardItem(notification)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ==========================================
// Componente de Tarjeta de Notificación Individual
// ==========================================
@Composable
fun NotificationCardItem(notification: AppNotification) {
    val (icon, iconColor, containerColor) = when (notification.category) {
        NotificationCategory.CONFIRMATION -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
        NotificationCategory.SEGUIMIENTO -> Triple(
            Icons.Default.Schedule,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
        NotificationCategory.NOVEDAD -> Triple(
            Icons.Default.Campaign,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall
                )

                if (!notification.promoCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text("Código Promo: ${notification.promoCode}") },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}
