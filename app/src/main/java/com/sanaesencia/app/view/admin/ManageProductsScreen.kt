package com.sanaesencia.app.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sanaesencia.app.data.model.ProductEntity
import com.sanaesencia.app.viewmodel.ProductViewModel

// ==========================================
// Pantalla de Gestión de Productos y Kits (Administrador)
// ==========================================
@Composable
fun ManageProductsScreen(viewModel: ProductViewModel) {
    // ------------------------------------------
    // Observación de Productos y Estados del Diálogo
    // ------------------------------------------
    val productos by viewModel.allProducts.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<ProductEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ------------------------------------------
        // Barra Superior: Título y Botón de Nuevo Producto
        // ------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Panel de Gestión",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text("Productos y Kits Sanaesencia", style = MaterialTheme.typography.headlineSmall)
            }
            Button(onClick = {
                productoAEditar = null
                mostrarDialogo = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Línea propia de productos naturales: kits, roll-ons y brumas disponibles para pacientes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (productos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No hay productos registrados aún. Presiona 'Nuevo' para agregar el primero.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(producto.name, style = MaterialTheme.typography.titleMedium)
                                Text(producto.description, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Precio: ${producto.price.toInt()} CLP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Switch(
                                    checked = producto.isAvailable,
                                    onCheckedChange = { viewModel.toggleAvailability(producto) }
                                )
                                Text(
                                    if (producto.isAvailable) "Disponible" else "Agotado",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Row {
                                    IconButton(onClick = {
                                        productoAEditar = producto
                                        mostrarDialogo = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteProduct(producto) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        var nombre by remember(productoAEditar) { mutableStateOf(productoAEditar?.name ?: "") }
        var descripcion by remember(productoAEditar) { mutableStateOf(productoAEditar?.description ?: "") }
        var precio by remember(productoAEditar) { mutableStateOf(productoAEditar?.price?.toInt()?.toString() ?: "") }
        val precioNum = precio.toIntOrNull()

        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(if (productoAEditar == null) "Nuevo producto" else "Editar producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.replace("\n", "") },
                        label = { Text("Nombre") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it.replace("\n", "") },
                        label = { Text("Descripción") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it.filter { c -> c.isDigit() } },
                        label = { Text("Precio (CLP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pr = productoAEditar
                        if (pr != null) {
                            viewModel.updateProduct(
                                pr.copy(
                                    name = nombre.trim(),
                                    description = descripcion.trim(),
                                    price = (precioNum ?: 0).toDouble()
                                )
                            )
                        } else {
                            viewModel.addProduct(
                                name = nombre.trim(),
                                description = descripcion.trim(),
                                price = (precioNum ?: 0).toDouble(),
                                isAvailable = true
                            )
                        }
                        mostrarDialogo = false
                    },
                    enabled = nombre.trim().length >= 3 && precioNum != null
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ProductDialog(
    productToEdit: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var price by remember { mutableStateOf(productToEdit?.price?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "10") }
    var imageUrl by remember { mutableStateOf(productToEdit?.imageUrl ?: "") }
    var isAvailable by remember { mutableStateOf(productToEdit?.isAvailable ?: true) }

    val priceNum = price.toDoubleOrNull()
    val stockNum = stock.toIntOrNull()
    val isFormValid = name.trim().length >= 3 && priceNum != null && priceNum > 0 && stockNum != null && stockNum >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productToEdit == null) "Nuevo Producto / Kit" else "Editar Producto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio (CLP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad / Stock Disponible") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de Imagen / Icono (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ProductEntity(
                            id = productToEdit?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            price = priceNum ?: 0.0,
                            imageUrl = imageUrl.trim(),
                            isAvailable = isAvailable,
                            stock = stockNum ?: 10
                        )
                    )
                },
                enabled = isFormValid
            ) {
                Text(if (productToEdit == null) "Guardar" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
