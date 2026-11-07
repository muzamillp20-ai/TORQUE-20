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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.ui.components.EmptyState
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.StatusBadge
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.StatusAccepted
import com.torque.bikecare.ui.theme.StatusCompleted
import com.torque.bikecare.ui.theme.StatusInService
import com.torque.bikecare.ui.theme.StatusRejected
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.utils.DateUtils
import com.torque.bikecare.utils.WhatsAppHelper
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun AdminBookingsScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBookingsState by adminViewModel.allBookings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Pending", "Accepted", "In Service", "Completed", "Rejected")

    var statusActionDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var targetStatus by remember { mutableStateOf<String?>(null) }
    var statusNoteInput by remember { mutableStateOf("") }

    if (statusActionDialogBooking != null && targetStatus != null) {
        val booking = statusActionDialogBooking!!
        val isReject = targetStatus == Booking.STATUS_REJECTED
        AlertDialog(
            onDismissRequest = {
                statusActionDialogBooking = null
                targetStatus = null
            },
            title = {
                Text(
                    text = if (isReject) "Decline Booking" else "Update to $targetStatus",
                    color = TorqueDarkNavy,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Booking #${booking.bookingId.takeLast(6).uppercase()} for ${booking.customerName} (${booking.bikeBrand} ${booking.bikeModel})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = statusNoteInput,
                        onValueChange = { statusNoteInput = it },
                        label = { Text(if (isReject) "Reason for decline (required)" else "Optional note for customer") },
                        placeholder = { Text(if (isReject) "e.g. Slot booked, parts out of stock..." else "e.g. Bike ready for delivery...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_status_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val note = statusNoteInput.trim().ifBlank { null }
                        adminViewModel.updateBookingStatus(booking.bookingId, targetStatus!!, note)
                        statusActionDialogBooking = null
                        targetStatus = null
                        statusNoteInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReject) StatusRejected else TorqueBlue
                    ),
                    modifier = Modifier.testTag("admin_dialog_confirm_status_button")
                ) {
                    Text(if (isReject) "Decline" else "Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        statusActionDialogBooking = null
                        targetStatus = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "Manage Bookings",
            subtitle = "Workshop service appointments",
            onBackClick = onBackClick
        )

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer, bike, plate, or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("admin_bookings_search_input")
        )

        // Status Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TorqueBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_filter_${filter.lowercase().replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = allBookingsState) {
            is UiState.Loading -> {
                LoadingState(message = "Loading bookings...")
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "Error Loading Bookings",
                    description = state.message
                )
            }
            is UiState.Success -> {
                val list = state.data
                val filteredList = list.filter { booking ->
                    val matchesFilter = when (selectedFilter) {
                        "Pending" -> booking.status == Booking.STATUS_PENDING
                        "Accepted" -> booking.status == Booking.STATUS_ACCEPTED
                        "In Service" -> booking.status == Booking.STATUS_IN_SERVICE
                        "Completed" -> booking.status == Booking.STATUS_COMPLETED
                        "Rejected" -> booking.status == Booking.STATUS_REJECTED
                        else -> true
                    }
                    val matchesSearch = searchQuery.isBlank() ||
                            booking.customerName.contains(searchQuery, ignoreCase = true) ||
                            booking.customerPhone.contains(searchQuery, ignoreCase = true) ||
                            booking.bikeBrand.contains(searchQuery, ignoreCase = true) ||
                            booking.bikeModel.contains(searchQuery, ignoreCase = true) ||
                            booking.registrationNumber.contains(searchQuery, ignoreCase = true) ||
                            booking.bookingId.contains(searchQuery, ignoreCase = true)

                    matchesFilter && matchesSearch
                }

                if (filteredList.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Bookings Found",
                        description = if (searchQuery.isNotBlank()) "No records match '$searchQuery'." else "No bookings in '$selectedFilter'."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredList, key = { it.bookingId }) { booking ->
                            AdminBookingDetailCard(
                                booking = booking,
                                onUpdateStatus = { newStatus ->
                                    statusActionDialogBooking = booking
                                    targetStatus = newStatus
                                    statusNoteInput = booking.statusNote ?: ""
                                },
                                onWhatsAppClick = {
                                    WhatsAppHelper.openChat(
                                        context = context,
                                        rawNumber = booking.customerPhone,
                                        message = "Hello ${booking.customerName}, this is TORQUE Workshop regarding your booking #${booking.bookingId.takeLast(6).uppercase()} for ${booking.bikeBrand} ${booking.bikeModel}."
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBookingDetailCard(
    booking: Booking,
    onUpdateStatus: (String) -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Customer + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )
                    Text(
                        text = "${booking.customerPhone} • ${booking.customerEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vehicle and Service
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TorqueSoftBlue, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "${booking.bikeBrand} ${booking.bikeModel} (${booking.registrationNumber})",
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Service: ${booking.serviceName}",
                        fontWeight = FontWeight.SemiBold,
                        color = TorqueBlue,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Slot: ${DateUtils.formatIsoDate(booking.preferredDate)} at ${booking.preferredTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Problem Note
            Text(
                text = "Reported Problem:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TorqueDarkNavy
            )
            Text(
                text = booking.problemDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!booking.statusNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Workshop Note: ${booking.statusNote}",
                    fontSize = 12.sp,
                    color = TorqueBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Customer
                OutlinedButton(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_whatsapp_${booking.bookingId}")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                // Status Transition Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (booking.status) {
                        Booking.STATUS_PENDING -> {
                            OutlinedButton(
                                onClick = { onUpdateStatus(Booking.STATUS_REJECTED) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_btn_reject_${booking.bookingId}")
                            ) {
                                Text("Decline", color = StatusRejected, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onUpdateStatus(Booking.STATUS_ACCEPTED) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAccepted),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_btn_accept_${booking.bookingId}")
                            ) {
                                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Booking.STATUS_ACCEPTED -> {
                            Button(
                                onClick = { onUpdateStatus(Booking.STATUS_IN_SERVICE) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusInService),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_btn_inservice_${booking.bookingId}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Booking.STATUS_IN_SERVICE -> {
                            Button(
                                onClick = { onUpdateStatus(Booking.STATUS_COMPLETED) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_btn_complete_${booking.bookingId}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            // Already completed or rejected
                            Text(
                                text = "Finalized",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
