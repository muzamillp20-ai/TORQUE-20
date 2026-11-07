package com.torque.bikecare.ui.screens.customer

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.data.model.Service
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.StatusBadge
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.utils.WhatsAppHelper
import com.torque.bikecare.viewmodel.CustomerViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun CustomerHomeScreen(
    customerViewModel: CustomerViewModel,
    userName: String,
    onBookServiceClick: (Service?) -> Unit,
    onViewServicesClick: () -> Unit,
    onViewBookingsClick: () -> Unit,
    onBookingDetailsClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeServicesState by customerViewModel.activeServices.collectAsState()
    val myBookingsState by customerViewModel.myBookings.collectAsState()
    val workshopConfig by customerViewModel.workshopConfig.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "TORQUE",
            subtitle = "Every Ride. Better Care.",
            actions = {
                // Profile Avatar button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TorqueSoftBlue)
                        .clickable { onProfileClick() }
                        .testTag("home_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase().ifBlank { "U" },
                        color = TorqueBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Workshop Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(TorqueDarkNavy, TorqueBlue)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back, $userName!",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ready for the next ride?",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { onBookServiceClick(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = TorqueBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hero_book_service_button")
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book a Service", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Actions Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Services",
                        icon = Icons.Default.Build,
                        onClick = onViewServicesClick,
                        modifier = Modifier.weight(1f),
                        tag = "quick_services_button"
                    )
                    QuickActionCard(
                        title = "Bookings",
                        icon = Icons.Default.CalendarMonth,
                        onClick = onViewBookingsClick,
                        modifier = Modifier.weight(1f),
                        tag = "quick_bookings_button"
                    )
                    QuickActionCard(
                        title = "WhatsApp",
                        icon = Icons.Default.Chat,
                        onClick = {
                            WhatsAppHelper.openChat(
                                context = context,
                                rawNumber = workshopConfig.whatsappNumber,
                                message = "Hello TORQUE Workshop, I would like to inquire about bike maintenance."
                            )
                        },
                        modifier = Modifier.weight(1f),
                        tag = "quick_whatsapp_button"
                    )
                }
            }

            // Active Booking Tracker Section
            item {
                if (myBookingsState is UiState.Success) {
                    val bookings = (myBookingsState as UiState.Success<List<Booking>>).data
                    val activeBooking = bookings.firstOrNull {
                        it.status == Booking.STATUS_PENDING ||
                        it.status == Booking.STATUS_ACCEPTED ||
                        it.status == Booking.STATUS_IN_SERVICE
                    }

                    if (activeBooking != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Service",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TorqueDarkNavy
                                )
                                Text(
                                    text = "View All",
                                    color = TorqueBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { onViewBookingsClick() }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            ActiveBookingCard(
                                booking = activeBooking,
                                onClick = { onBookingDetailsClick(activeBooking.bookingId) }
                            )
                        }
                    }
                }
            }

            // Available Services Preview Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workshop Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )
                    Text(
                        text = "See Catalog",
                        color = TorqueBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onViewServicesClick() }
                            .testTag("see_all_services_link")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            when (val state = activeServicesState) {
                is UiState.Loading -> {
                    item {
                        LoadingState(message = "Loading services...")
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    val previewList = state.data.take(3)
                    if (previewList.isEmpty()) {
                        item {
                            Text(
                                text = "No services configured yet. The workshop will publish packages soon.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(previewList) { service ->
                            ServicePreviewCard(
                                service = service,
                                onBookClick = { onBookServiceClick(service) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Workshop Info & Location Card
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TorqueSoftBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TorqueBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = workshopConfig.workshopName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TorqueDarkNavy
                                )
                                Text(
                                    text = workshopConfig.workshopAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    WhatsAppHelper.openChat(
                                        context = context,
                                        rawNumber = workshopConfig.whatsappNumber,
                                        message = "Hi, I need assistance with my bike."
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("workshop_card_whatsapp_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp")
                            }

                            if (workshopConfig.workshopPhone.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        WhatsAppHelper.openChat(
                                            context = context,
                                            rawNumber = workshopConfig.workshopPhone
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TorqueSoftBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TorqueBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TorqueDarkNavy
            )
        }
    }
}

@Composable
private fun ActiveBookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("active_booking_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${booking.bookingId.takeLast(6).uppercase()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(status = booking.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = booking.serviceName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TorqueDarkNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${booking.bikeBrand} ${booking.bikeModel} • ${booking.registrationNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${booking.preferredDate} at ${booking.preferredTime}",
                    fontSize = 12.sp,
                    color = TorqueBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ServicePreviewCard(
    service: Service,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${String.format("%.2f", service.price)} • ~${service.durationMinutes} mins",
                    fontSize = 12.sp,
                    color = TorqueBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(containerColor = TorqueSoftBlue, contentColor = TorqueBlue),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("preview_book_${service.serviceId}")
            ) {
                Text("Book", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
