package com.torque.bikecare.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.ui.components.EmptyState
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.StatusBadge
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.viewmodel.CustomerViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun MyBookingsScreen(
    customerViewModel: CustomerViewModel,
    onBackClick: () -> Unit,
    onBookingClick: (String) -> Unit,
    onNewBookingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myBookingsState by customerViewModel.myBookings.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Completed")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "My Bookings",
            subtitle = "Live tracking and repair history",
            onBackClick = onBackClick
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = TorqueBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = TorqueBlue
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("bookings_tab_${title.lowercase()}")
                )
            }
        }

        when (val state = myBookingsState) {
            is UiState.Loading -> {
                LoadingState(message = "Loading your bookings...")
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "Error Loading Bookings",
                    description = state.message
                )
            }
            is UiState.Success -> {
                val allBookings = state.data
                val filteredBookings = when (selectedTabIndex) {
                    1 -> allBookings.filter {
                        it.status == Booking.STATUS_PENDING ||
                        it.status == Booking.STATUS_ACCEPTED ||
                        it.status == Booking.STATUS_IN_SERVICE
                    }
                    2 -> allBookings.filter {
                        it.status == Booking.STATUS_COMPLETED ||
                        it.status == Booking.STATUS_REJECTED
                    }
                    else -> allBookings
                }

                if (filteredBookings.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = when (selectedTabIndex) {
                            1 -> "No Active Bookings"
                            2 -> "No Past Bookings"
                            else -> "No Bookings Yet"
                        },
                        description = "Book a professional maintenance or repair service for your motorcycle.",
                        actionLabel = "Book a Service",
                        onActionClick = onNewBookingClick
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBookings, key = { it.bookingId }) { booking ->
                            BookingListItemCard(
                                booking = booking,
                                onClick = { onBookingClick(booking.bookingId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingListItemCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("booking_item_${booking.bookingId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: #${booking.bookingId.takeLast(6).uppercase()}",
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slot: ${booking.preferredDate} (${booking.preferredTime})",
                    fontSize = 12.sp,
                    color = TorqueBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
