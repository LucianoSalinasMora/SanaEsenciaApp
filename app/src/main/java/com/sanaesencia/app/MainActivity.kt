package com.sanaesencia.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanaesencia.app.data.repository.AboutRepository
import com.sanaesencia.app.data.local.AppDatabase
import com.sanaesencia.app.data.repository.AppointmentRepository
import com.sanaesencia.app.data.util.NotificationHelper
import com.sanaesencia.app.data.repository.ProductRepository
import com.sanaesencia.app.data.repository.UserRepository
import com.sanaesencia.app.data.model.UserRole
import com.sanaesencia.app.ui.theme.SanaesenciaTheme
import com.sanaesencia.app.view.patient.BookingScreen
import com.sanaesencia.app.view.auth.ForgotPasswordScreen
import com.sanaesencia.app.view.auth.LoginScreen
import com.sanaesencia.app.view.main.MainScreen
import com.sanaesencia.app.view.auth.SignUpScreen
import com.sanaesencia.app.viewmodel.AboutViewModel
import com.sanaesencia.app.viewmodel.AboutViewModelFactory
import com.sanaesencia.app.viewmodel.BookingViewModel
import com.sanaesencia.app.viewmodel.BookingViewModelFactory
import com.sanaesencia.app.viewmodel.ProductViewModel
import com.sanaesencia.app.viewmodel.ProductViewModelFactory
import com.sanaesencia.app.viewmodel.UserViewModel
import com.sanaesencia.app.viewmodel.UserViewModelFactory

// ==========================================
// Actividad Principal de la Aplicación
// ==========================================
class MainActivity : ComponentActivity() {

    // ==========================================
    // Ciclo de Vida: Inicialización de la Actividad
    // ==========================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ------------------------------------------
        // Inicialización de Base de Datos y Repositorios
        // ------------------------------------------
        val database = AppDatabase.getDatabase(this)
        val repository = AppointmentRepository(database.appointmentDao())
        val notificationHelper = NotificationHelper(this)

        // Inicializar los repositorios de datos
        val userRepository = UserRepository(database.userDao())
        val productRepository = ProductRepository(database.productDao())
        val aboutRepository = AboutRepository(database.aboutDao())

        // ------------------------------------------
        // Configuración de la UI con Jetpack Compose
        // ------------------------------------------
        setContent {
            SanaesenciaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ------------------------------------------
                    // Instanciación de ViewModels mediante Factories
                    // ------------------------------------------
                    val bookingViewModel: BookingViewModel = viewModel(
                        factory = BookingViewModelFactory(repository)
                    )

                    val userViewModel: UserViewModel = viewModel(
                        factory = UserViewModelFactory(userRepository)
                    )

                    val productViewModel: ProductViewModel = viewModel(
                        factory = ProductViewModelFactory(productRepository)
                    )

                    // Controlador de navegación global
                    val navController = rememberNavController()

                    // ------------------------------------------
                    // Grafo de Navegación Principal (NavHost)
                    // ------------------------------------------
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(320)
                            ) + fadeIn(animationSpec = tween(320))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(320)
                            ) + fadeOut(animationSpec = tween(320))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(320)
                            ) + fadeIn(animationSpec = tween(320))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(320)
                            ) + fadeOut(animationSpec = tween(320))
                        }
                    ) {

                        // ------------------------------------------
                        // Destino: Pantalla de Inicio de Sesión (Login)
                        // ------------------------------------------
                        composable(Screen.Login.route) {
                            LoginScreen(
                                viewModel = userViewModel,
                                onLoginSuccess = { role ->
                                    navController.navigate(Screen.Main.createRoute(role.name)) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                            )
                        }

                        // ------------------------------------------
                        // Destino: Pantalla de Registro de Nuevos Usuarios
                        // ------------------------------------------
                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                viewModel = userViewModel,
                                onRegisterSuccess = {
                                    Toast.makeText(this@MainActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ------------------------------------------
                        // Destino: Pantalla de Recuperación de Contraseña
                        // ------------------------------------------
                        composable(Screen.ForgotPassword.route) {
                            ForgotPasswordScreen(
                                viewModel = userViewModel,
                                onBack = { navController.popBackStack() },
                                onPasswordResetSuccess = {
                                    Toast.makeText(this@MainActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ------------------------------------------
                        // Destino: Pantalla Principal con Dashboards y Navegación según Rol
                        // ------------------------------------------
                        composable(Screen.Main.route) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: UserRole.PACIENTE.name

                            MainScreen(
                                parentNavController = navController,
                                userRole = role,
                                userViewModel = userViewModel,
                                bookingViewModel = bookingViewModel,
                                productViewModel = productViewModel,
                                notificationHelper = notificationHelper,
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        // ------------------------------------------
                        // Destino: Pantalla de Agendamiento de Citas
                        // ------------------------------------------
                        composable(Screen.Booking.route) {
                            BookingScreen(
                                userViewModel = userViewModel,
                                bookingViewModel = bookingViewModel,
                                onBookingSuccess = { appointment ->
                                    notificationHelper.sendBookingNotification(
                                        serviceName = appointment.serviceType,
                                        date = appointment.date,
                                        timeSlot = appointment.timeSlot
                                    )
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Cita registrada exitosamente",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
