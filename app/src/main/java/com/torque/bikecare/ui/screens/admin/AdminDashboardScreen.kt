package com.torque.bikecare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.StatusBadge
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.StatusAccepted
import com.torque.bikecare.ui.theme.StatusCompleted
import com.torque.bikecare.ui.theme.StatusInService
import com.torque.bikecare.ui.theme.StatusPending
import com.torque.bikecare.ui.theme.StatusRejected
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onNavigateToBookings: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSwitchToCustomerView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by adminViewModel.stats.collectAsState()
    val allBookingsState by adminViewModel.allBookings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "Workshop Dashboard",
            subtitle = "Live Workshop Management",
            actions = {
                // Switch to Customer View
                IconButton(
                    onClick = onSwitchToCustomerView,
                    modifier = Modifier.testTag("admin_switch_to_customer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Customer View",
                        tint = TorqueBlue
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Section
            item {
                Text(
                    text = "Live Workshop Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Stats Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Total Bookings",
                            count = stats.total,
                            color = TorqueDarkNavy,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Pending Action",
                            count = stats.pending,
                            color = StatusPending,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Accepted",
                            count = stats.accepted,
                            color = StatusAccepted,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "In Service",
                            count = stats.inService,
                            color = StatusInService,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Completed",
                            count = stats.completed,
                            color = StatusCompleted,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Declined",
                            count = stats.rejected,
                            color = StatusRejected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Nav Sections
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Workshop Operations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMenuCard(
                        title = "Bookings",
                        subtitle = "Manage & Status",
                        icon = Icons.Default.CalendarMonth,
                        onClick = onNavigateToBookings,
                        modifier = Modifier.weight(1f),
                        tag = "admin_nav_bookings"
                    )
                    AdminMenuCard(
                        title = "Services",
                        subtitle = "Catalog & Rates",
                        icon = Icons.Default.Build,
                        onClick = onNavigateToServices,
                        modifier = Modifier.weight(1f),
                        tag = "admin_nav_services"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMenuCard(
                        title = "Customers",
                        subtitle = "Client Directory",
                        icon = Icons.Default.Group,
                        onClick = onNavigateToCustomers,
                        modifier = Modifier.weight(1f),
                        tag = "admin_nav_customers"
                    )
                    AdminMenuCard(
                        title = "Settings",
                        subtitle = "WhatsApp & Info",
                        icon = Icons.Default.Settings,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f),
                        tag = "admin_nav_settings"
                    )
                }
            }

            // Recent Bookings Feed
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )
                    Text(
                        text = "View All",
                        color = TorqueBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToBookings() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            when (val state = allBookingsState) {
                is UiState.Loading -> {
                    item { LoadingState(message = "Syncing live bookings...") }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is UiState.Success -> {
                    val recent = state.data.take(5)
                    if (recent.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No bookings received yet. As customers book services, they will appear here in real time.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(recent, key = { it.bookingId }) { booking ->
                            AdminBookingSummaryCard(
                                booking = booking,
                                onAccept = { adminViewModel.updateBookingStatus(booking.bookingId, Booking.STATUS_ACCEPTED, "Confirmed by workshop") },
                                onReject = { adminViewModel.updateBookingStatus(booking.bookingId, Booking.STATUS_REJECTED, "Slot unavailable") },
                                onCardClick = onNavigateToBookings
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AdminMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TorqueSoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TorqueBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun AdminBookingSummaryCard(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.customerName,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy,
                    fontSize = 14.sp
                )
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${booking.serviceName} • ${booking.bikeBrand} ${booking.bikeModel} (${booking.registrationNumber})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Appointment: ${booking.preferredDate} (${booking.preferredTime})",
                fontSize = 12.sp,
                color = TorqueBlue,
                fontWeight = FontWeight.SemiBold
            )

            if (booking.status == Booking.STATUS_PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_quick_reject_${booking.bookingId}")
                    ) {
                        Text("Decline", color = StatusRejected, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAccepted),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_quick_accept_${booking.bookingId}")
                    ) {
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
