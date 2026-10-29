package com.sanaesencia.app.view.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.util.ContactHelper
import com.sanaesencia.app.data.util.NosotrosManager
import com.sanaesencia.app.viewmodel.ProductViewModel

// ==========================================
// Pantalla del Catálogo de Productos para Pacientes
// ==========================================
@Composable
fun PatientProductsScreen(
    productViewModel: ProductViewModel
) {
    // ------------------------------------------
    // Observación de Productos y Datos de Contacto
    // ------------------------------------------
    val context = LocalContext.current
    val productos by productViewModel.allProducts.collectAsState()
    val nosotrosInfo by NosotrosManager.infoState.collectAsState()

    LaunchedEffect(Unit) {
        NosotrosManager.init(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Productos Naturales",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Línea propia de bienestar y aromaterapia Sanaesencia: kits, roll-ons y brumas artesanales para tu equilibrio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Tarjeta informativa de pedidos por WhatsApp
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Cómo comprar?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Selecciona tu producto y presiona 'Pedir por WhatsApp' para coordinar retiro en Los Vilos o despacho a domicilio.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (productos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Cargando productos de la tienda Sanaesencia...",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(productos, key = { it.id }) { producto ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (producto.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = producto.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            AssistChip(
                                onClick = {},
                                label = { Text(if (producto.isAvailable) "Disponible" else "Agotado") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (producto.isAvailable) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
                                        contentDescription = null,
                                        tint = if (producto.isAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }

                        Text(
                            text = producto.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Valor:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "$${producto.price.toInt()} CLP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = {
                                    val msg = if (producto.isAvailable) {
                                        "Hola Centro Sanaesencia, me interesa adquirir el producto '${producto.name}' por valor de $${producto.price.toInt()} CLP."
                                    } else {
                                        "Hola Centro Sanaesencia, quisiera consultar cuándo habrá stock del producto '${producto.name}'."
                                    }
                                    ContactHelper.openWhatsApp(context, nosotrosInfo.phone, msg)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (producto.isAvailable) Color(0xFF25D366) else MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (producto.isAvailable) "Pedir por WhatsApp" else "Consultar Stock",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
