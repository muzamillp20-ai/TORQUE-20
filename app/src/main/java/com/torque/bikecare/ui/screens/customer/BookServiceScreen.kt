package com.torque.bikecare.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.ui.theme.TorqueSoftBlue
import com.torque.bikecare.utils.DateUtils
import com.torque.bikecare.viewmodel.BookingSubmissionState
import com.torque.bikecare.viewmodel.CustomerViewModel
import com.torque.bikecare.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BookServiceScreen(
    customerViewModel: CustomerViewModel,
    initialService: Service?,
    defaultCustomerName: String,
    defaultCustomerEmail: String,
    onBackClick: () -> Unit,
    onBookingSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeServicesState by customerViewModel.activeServices.collectAsState()
    val submissionState by customerViewModel.bookingState.collectAsState()

    var selectedService by remember { mutableStateOf(initialService) }
    var serviceDropdownExpanded by remember { mutableStateOf(false) }

    var customerName by remember { mutableStateOf(defaultCustomerName) }
    var customerPhone by remember { mutableStateOf("") }
    var bikeBrand by remember { mutableStateOf("") }
    var bikeModel by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var problemDescription by remember { mutableStateOf("") }

    // Date generation for upcoming days
    val upcomingDates = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0..7) {
            list.add(format.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    var selectedDate by remember { mutableStateOf(upcomingDates.firstOrNull() ?: DateUtils.todayIso()) }
    var dateDropdownExpanded by remember { mutableStateOf(false) }

    val timeSlots = listOf(
        "09:00 AM - 11:00 AM",
        "11:00 AM - 01:00 PM",
        "02:00 PM - 04:00 PM",
        "04:00 PM - 06:00 PM"
    )
    var selectedTime by remember { mutableStateOf(timeSlots.first()) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(submissionState) {
        when (val state = submissionState) {
            is BookingSubmissionState.Success -> {
                showSuccessDialog = state.bookingId
            }
            is BookingSubmissionState.Error -> {
                validationError = state.message
            }
            else -> {}
        }
    }

    // Auto-select first service if none selected and catalog loads
    LaunchedEffect(activeServicesState) {
        if (selectedService == null && activeServicesState is UiState.Success) {
            selectedService = (activeServicesState as UiState.Success<List<Service>>).data.firstOrNull()
        }
    }

    if (showSuccessDialog != null) {
        AlertDialog(
            onDismissRequest = {
                customerViewModel.resetBookingState()
                onBookingSuccess(showSuccessDialog!!)
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF12B76A),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TorqueDarkNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your service booking has been created and submitted to the workshop.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TorqueSoftBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Booking ID: ${showSuccessDialog}",
                                fontWeight = FontWeight.Bold,
                                color = TorqueBlue,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Scheduled: $selectedDate at $selectedTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = TorqueDarkNavy
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The workshop will review your request and confirm the slot shortly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        customerViewModel.resetBookingState()
                        onBookingSuccess(showSuccessDialog!!)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TorqueBlue),
                    modifier = Modifier.testTag("dialog_view_my_bookings_button")
                ) {
                    Text("View in My Bookings")
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
            title = "Book a Service",
            subtitle = "Schedule an appointment at TORQUE",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Service Selection Card
            Text(
                text = "1. Selected Service Package",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TorqueDarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { serviceDropdownExpanded = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedService?.name ?: "Select a Service",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TorqueDarkNavy
                            )
                            if (selectedService != null) {
                                Text(
                                    text = "$${String.format("%.2f", selectedService!!.price)} • ~${selectedService!!.durationMinutes} mins",
                                    color = TorqueBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                    }

                    if (activeServicesState is UiState.Success) {
                        val services = (activeServicesState as UiState.Success<List<Service>>).data
                        DropdownMenu(
                            expanded = serviceDropdownExpanded,
                            onDismissRequest = { serviceDropdownExpanded = false }
                        ) {
                            services.forEach { service ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(service.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "$${String.format("%.2f", service.price)} - ${service.category}",
                                                fontSize = 12.sp,
                                                color = TorqueBlue
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedService = service
                                        serviceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vehicle Information Card
            Text(
                text = "2. Bike Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TorqueDarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = bikeBrand,
                            onValueChange = { bikeBrand = it },
                            label = { Text("Brand *") },
                            placeholder = { Text("Yamaha, Honda, RE...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bike_brand_input")
                        )
                        OutlinedTextField(
                            value = bikeModel,
                            onValueChange = { bikeModel = it },
                            label = { Text("Model *") },
                            placeholder = { Text("MT-15, Classic 350...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bike_model_input")
                        )
                    }

                    OutlinedTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it.uppercase() },
                        label = { Text("Registration / Plate Number *") },
                        placeholder = { Text("e.g. MH02AB1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bike_registration_input")
                    )

                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        label = { Text("Description of Issue / Requests *") },
                        placeholder = { Text("Explain any noises, brake feel, service preferences, or specific complaints...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bike_problem_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact & Scheduling Card
            Text(
                text = "3. Customer & Appointment Slot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TorqueDarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Your Name *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Contact Phone Number *") },
                        placeholder = { Text("+1 234 567 8900") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input")
                    )

                    // Date selector dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = DateUtils.formatIsoDate(selectedDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred Service Date *") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TorqueBlue) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dateDropdownExpanded = true }
                                .testTag("preferred_date_picker")
                        )
                        DropdownMenu(
                            expanded = dateDropdownExpanded,
                            onDismissRequest = { dateDropdownExpanded = false }
                        ) {
                            upcomingDates.forEach { dateIso ->
                                DropdownMenuItem(
                                    text = { Text(DateUtils.formatIsoDate(dateIso)) },
                                    onClick = {
                                        selectedDate = dateIso
                                        dateDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Time Slot selector dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred Time Window *") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TorqueBlue) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { timeDropdownExpanded = true }
                                .testTag("preferred_time_picker")
                        )
                        DropdownMenu(
                            expanded = timeDropdownExpanded,
                            onDismissRequest = { timeDropdownExpanded = false }
                        ) {
                            timeSlots.forEach { slot ->
                                DropdownMenuItem(
                                    text = { Text(slot) },
                                    onClick = {
                                        selectedTime = slot
                                        timeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3F2)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        color = Color(0xFFB42318),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isSubmitting = submissionState is BookingSubmissionState.Submitting

            Button(
                onClick = {
                    if (selectedService == null) {
                        validationError = "Please select a service package."
                        return@Button
                    }
                    validationError = null
                    customerViewModel.submitBooking(
                        service = selectedService!!,
                        customerName = customerName,
                        customerEmail = defaultCustomerEmail,
                        customerPhone = customerPhone,
                        bikeBrand = bikeBrand,
                        bikeModel = bikeModel,
                        registrationNumber = registrationNumber,
                        problemDescription = problemDescription,
                        preferredDate = selectedDate,
                        preferredTime = selectedTime
                    )
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = TorqueBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_booking_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Confirming & Saving Booking...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Confirm Booking Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
