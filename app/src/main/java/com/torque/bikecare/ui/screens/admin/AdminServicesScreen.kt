package com.torque.bikecare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Service
import com.torque.bikecare.ui.components.ConfirmDialog
import com.torque.bikecare.ui.components.EmptyState
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun AdminServicesScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allServicesState by adminViewModel.allServices.collectAsState()

    var showServiceDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<Service?>(null) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }

    if (showServiceDialog) {
        ServiceFormDialog(
            service = editingService,
            onDismiss = {
                showServiceDialog = false
                editingService = null
            },
            onSave = { service, isEdit ->
                adminViewModel.saveService(service, isEdit)
                showServiceDialog = false
                editingService = null
            }
        )
    }

    if (serviceToDelete != null) {
        ConfirmDialog(
            title = "Delete Service",
            message = "Are you sure you want to delete '${serviceToDelete!!.name}'? Active and historical bookings will retain service data.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                adminViewModel.deleteService(serviceToDelete!!.serviceId)
                serviceToDelete = null
            },
            onDismiss = { serviceToDelete = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingService = null
                    showServiceDialog = true
                },
                containerColor = TorqueBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("admin_add_service_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            TorqueTopBar(
                title = "Service Management",
                subtitle = "Manage packages, prices & availability",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { adminViewModel.seedDefaultServices() },
                        modifier = Modifier.testTag("admin_seed_services_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Seed Standard Services",
                            tint = TorqueBlue
                        )
                    }
                }
            )

            when (val state = allServicesState) {
                is UiState.Loading -> {
                    LoadingState(message = "Loading workshop services...")
                }
                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.Build,
                        title = "Error Loading Services",
                        description = state.message
                    )
                }
                is UiState.Success -> {
                    val services = state.data
                    if (services.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Build,
                            title = "No Services in Catalog",
                            description = "Add a service package or seed standard workshop packages.",
                            actionLabel = "Seed Standard Services",
                            onActionClick = { adminViewModel.seedDefaultServices() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(services, key = { it.serviceId }) { service ->
                                AdminServiceItemCard(
                                    service = service,
                                    onEdit = {
                                        editingService = service
                                        showServiceDialog = true
                                    },
                                    onToggle = { isActive ->
                                        adminViewModel.toggleServiceActive(service.serviceId, isActive)
                                    },
                                    onDelete = {
                                        serviceToDelete = service
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminServiceItemCard(
    service: Service,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TorqueDarkNavy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (service.isActive) Color(0xFFECFDF3) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (service.isActive) "ACTIVE" else "DISABLED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (service.isActive) Color(0xFF027A48) else Color(0xFF64748B)
                            )
                        }
                    }
                    Text(
                        text = "${service.category} • ~${service.durationMinutes} mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$${String.format("%.2f", service.price)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TorqueBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Enabled for Booking",
                        style = MaterialTheme.typography.bodySmall,
                        color = TorqueDarkNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = service.isActive,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TorqueBlue
                        ),
                        modifier = Modifier.testTag("admin_toggle_${service.serviceId}")
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("admin_edit_${service.serviceId}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TorqueBlue)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("admin_delete_${service.serviceId}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD92D20))
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceFormDialog(
    service: Service?,
    onDismiss: () -> Unit,
    onSave: (Service, Boolean) -> Unit
) {
    val isEdit = service != null
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var priceText by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(service?.category ?: "Maintenance") }
    var durationText by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "60") }
    var isActive by remember { mutableStateOf(service?.isActive ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Service Package" else "Add New Service",
                fontWeight = FontWeight.Bold,
                color = TorqueDarkNavy
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_dialog_name_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    placeholder = { Text("Maintenance, Safety, Performance...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price ($) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_dialog_price_input")
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duration (mins) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_dialog_desc_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available to Customers", fontWeight = FontWeight.Medium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFD92D20),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val duration = durationText.toLongOrNull() ?: 60L
                    if (name.isBlank() || description.isBlank() || price == null || price < 0) {
                        error = "Please provide valid name, description, and price."
                        return@Button
                    }
                    val updated = (service ?: Service()).copy(
                        name = name.trim(),
                        description = description.trim(),
                        price = price,
                        category = category.trim(),
                        durationMinutes = duration,
                        isActive = isActive
                    )
                    onSave(updated, isEdit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TorqueBlue),
                modifier = Modifier.testTag("service_dialog_save_button")
            ) {
                Text("Save Service")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
