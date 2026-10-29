package com.sanaesencia.app.view.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.viewmodel.UserViewModel

// ==========================================
// Pantalla de Catálogo de Servicios y Especialidades
// ==========================================
@Composable
fun ServiciosCatalogoScreen(
    userViewModel: UserViewModel,
    onAgendar: () -> Unit,
    onVerProductos: () -> Unit = {}
) {
    // ------------------------------------------
    // Observación y Agrupación de Profesionales por Especialidad
    // ------------------------------------------
    val profesionales by userViewModel.professionals.collectAsState()

    // Agrupa a los profesionales por especialidad y ordena las especialidades alfabéticamente
    val grupos = profesionales
        .filter { it.specialty.isNotBlank() }
        .groupBy { it.specialty }
        .toList()
        .sortedBy { it.first }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Catálogo de Servicios",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Especialidades del centro, profesionales a cargo y modalidades de atención.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Button(
                onClick = onAgendar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agendar una cita")
            }
        }

        if (grupos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Aún no hay profesionales registrados en el catálogo.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(grupos) { (especialidad, pros) ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = especialidad,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Modalidad: presencial en Los Vilos u online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        pros.forEach { pro ->
                            Text("• ${pro.name}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

    }
}
