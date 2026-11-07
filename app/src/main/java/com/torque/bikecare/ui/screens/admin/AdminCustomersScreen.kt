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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.torque.bikecare.data.model.User
import com.torque.bikecare.ui.components.EmptyState
import com.torque.bikecare.ui.components.LoadingState
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.utils.DateUtils
import com.torque.bikecare.utils.WhatsAppHelper
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.UiState

@Composable
fun AdminCustomersScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customersState by adminViewModel.allCustomers.collectAsState()
    val allBookingsState by adminViewModel.allBookings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "Customer Directory",
            subtitle = "Registered clients & booking records",
            onBackClick = onBackClick
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("admin_customers_search_input")
        )

        when (val state = customersState) {
            is UiState.Loading -> {
                LoadingState(message = "Loading customer directory...")
            }
            is UiState.Error -> {
                EmptyState(
                    icon = Icons.Default.Group,
                    title = "Error Loading Customers",
                    description = state.message
                )
            }
            is UiState.Success -> {
                val customers = state.data
                val allBookings = if (allBookingsState is UiState.Success) {
                    (allBookingsState as UiState.Success<List<Booking>>).data
                } else emptyList()

                val filtered = customers.filter { user ->
                    searchQuery.isBlank() ||
                            user.name.contains(searchQuery, ignoreCase = true) ||
                            user.email.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Group,
                        title = "No Customers Found",
                        description = if (searchQuery.isNotBlank()) "No clients match '$searchQuery'." else "As riders sign in with Google, they will appear here."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.uid }) { user ->
                            val userBookings = allBookings.filter { it.customerUid == user.uid }
                            AdminCustomerCard(
                                user = user,
                                bookingCount = userBookings.size,
                                onWhatsAppClick = {
                                    val phone = user.phoneNumber ?: userBookings.firstOrNull()?.customerPhone
                                    WhatsAppHelper.openChat(
                                        context = context,
                                        rawNumber = phone,
                                        message = "Hello ${user.name}, this is TORQUE Workshop."
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
private fun AdminCustomerCard(
    user: User,
    bookingCount: Int,
    onWhatsAppClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(TorqueSoftBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase().ifBlank { "U" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TorqueBlue
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$bookingCount service booking${if (bookingCount != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = TorqueBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onWhatsAppClick,
                modifier = Modifier.testTag("admin_customer_whatsapp_${user.uid}")
            ) {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
            }
        }
    }
}
