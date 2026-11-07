package com.torque.bikecare.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.torque.bikecare.ui.theme.StatusCompleted
import com.torque.bikecare.ui.theme.StatusInService
import com.torque.bikecare.ui.theme.StatusPending
import com.torque.bikecare.ui.theme.StatusRejected
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.utils.DateUtils
import com.torque.bikecare.utils.WhatsAppHelper
import com.torque.bikecare.viewmodel.CustomerViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun BookingDetailsScreen(
    bookingId: String,
    customerViewModel: CustomerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val myBookingsState by customerViewModel.myBookings.collectAsState()
    val workshopConfig by customerViewModel.workshopConfig.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "Booking Details",
            subtitle = "#${bookingId.takeLast(8).uppercase()}",
            onBackClick = onBackClick
        )

        when (val state = myBookingsState) {
            is UiState.Loading -> {
                LoadingState(message = "Loading booking details...")
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error Loading Booking",
                    description = state.message
                )
            }
            is UiState.Success -> {
                val booking = state.data.firstOrNull { it.bookingId == bookingId }
                if (booking == null) {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = "Booking Not Found",
                        description = "Could not locate booking #$bookingId in your records."
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status Card & Progress Timeline
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Current Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TorqueDarkNavy
                                    )
                                    StatusBadge(status = booking.status)
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Status Stepper
                                if (booking.status == Booking.STATUS_REJECTED) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3F2)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Booking Declined",
                                                fontWeight = FontWeight.Bold,
                                                color = StatusRejected
                                            )
                                            if (!booking.statusNote.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Reason: ${booking.statusNote}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFB42318)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val currentStep = when (booking.status) {
                                        Booking.STATUS_PENDING -> 1
                                        Booking.STATUS_ACCEPTED -> 2
                                        Booking.STATUS_IN_SERVICE -> 3
                                        Booking.STATUS_COMPLETED -> 4
                                        else -> 1
                                    }
                                    StatusTimeline(currentStep = currentStep)
                                }

                                if (!booking.statusNote.isNullOrBlank() && booking.status != Booking.STATUS_REJECTED) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = TorqueSoftBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Workshop Note:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TorqueBlue
                                            )
                                            Text(
                                                text = booking.statusNote,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TorqueDarkNavy
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Vehicle & Service Information Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                DetailRow(
                                    icon = Icons.Default.Build,
                                    label = "Service Package",
                                    value = booking.serviceName
                                )
                                DetailRow(
                                    icon = Icons.Default.TwoWheeler,
                                    label = "Bike",
                                    value = "${booking.bikeBrand} ${booking.bikeModel} (${booking.registrationNumber})"
                                )
                                DetailRow(
                                    icon = Icons.Default.CalendarMonth,
                                    label = "Scheduled Appointment",
                                    value = "${DateUtils.formatIsoDate(booking.preferredDate)} • ${booking.preferredTime}"
                                )
                                DetailRow(
                                    icon = Icons.Default.Description,
                                    label = "Reported Issue / Requests",
                                    value = booking.problemDescription
                                )
                            }
                        }

                        // WhatsApp Action Card
                        Button(
                            onClick = {
                                WhatsAppHelper.openChat(
                                    context = context,
                                    rawNumber = workshopConfig.whatsappNumber,
                                    message = "Hello TORQUE, I am contacting you regarding my booking #${booking.bookingId.takeLast(6).uppercase()} for my ${booking.bikeBrand} ${booking.bikeModel}."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("booking_whatsapp_contact_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat with Workshop on WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTimeline(currentStep: Int) {
    val steps = listOf("Requested", "Confirmed", "In Service", "Ready")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isDone = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep

            val circleColor = when {
                isDone -> StatusCompleted
                isCurrent -> TorqueBlue
                else -> Color(0xFFE2E8F0)
            }
            val textColor = when {
                isDone || isCurrent -> TorqueDarkNavy
                else -> Color(0xFF94A3B8)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(circleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "$stepNumber",
                            color = if (isCurrent) Color.White else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stepName,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(TorqueSoftBlue, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TorqueBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TorqueDarkNavy
            )
        }
    }
}
