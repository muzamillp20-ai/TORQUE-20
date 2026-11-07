package com.torque.bikecare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.torque.bikecare.data.model.WorkshopConfig
import com.torque.bikecare.ui.components.ConfirmDialog
import com.torque.bikecare.ui.components.TorqueTopBar
import com.torque.bikecare.ui.theme.TorqueBlue
import com.torque.bikecare.ui.theme.TorqueDarkNavy
import com.torque.bikecare.viewmodel.AdminViewModel
import com.torque.bikecare.viewmodel.AuthViewModel

@Composable
fun AdminConfigScreen(
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSignOutComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    val workshopConfig by adminViewModel.workshopConfig.collectAsState()
    val actionMessage by adminViewModel.actionMessage.collectAsState()

    var workshopName by remember { mutableStateOf("") }
    var workshopAddress by remember { mutableStateOf("") }
    var workshopPhone by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }

    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workshopConfig) {
        workshopName = workshopConfig.workshopName
        workshopAddress = workshopConfig.workshopAddress
        workshopPhone = workshopConfig.workshopPhone
        whatsappNumber = workshopConfig.whatsappNumber
        adminEmail = workshopConfig.adminEmail
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Admin Sign Out",
            message = "Sign out from the administrator session?",
            confirmLabel = "Sign Out",
            isDestructive = true,
            onConfirm = {
                showSignOutDialog = false
                authViewModel.signOut(credentialManager, onSignOutComplete)
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        TorqueTopBar(
            title = "Workshop Settings",
            subtitle = "Contact details & WhatsApp configuration",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Workshop Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )

                    OutlinedTextField(
                        value = workshopName,
                        onValueChange = { workshopName = it },
                        label = { Text("Workshop Business Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_config_name_input")
                    )

                    OutlinedTextField(
                        value = workshopAddress,
                        onValueChange = { workshopAddress = it },
                        label = { Text("Physical Address") },
                        placeholder = { Text("Shop #, Street, City") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_config_address_input")
                    )

                    OutlinedTextField(
                        value = workshopPhone,
                        onValueChange = { workshopPhone = it },
                        label = { Text("Voice Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_config_phone_input")
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WhatsApp Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )

                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("WhatsApp Number (International Format) *") },
                        placeholder = { Text("+919876543210 (country code + number)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_config_whatsapp_input")
                    )

                    Text(
                        text = "Customers tap 'Contact via WhatsApp' in the app to start a chat directly with this number.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Admin Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TorqueDarkNavy
                    )

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Authorized Administrator Gmail *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_config_email_input")
                    )

                    Text(
                        text = "Only users signed into Google with this email will be allowed into this admin workshop console.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (actionMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF8FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actionMessage!!,
                        color = TorqueBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val updated = workshopConfig.copy(
                        workshopName = workshopName.trim().ifBlank { "TORQUE Workshop" },
                        workshopAddress = workshopAddress.trim(),
                        workshopPhone = workshopPhone.trim(),
                        whatsappNumber = whatsappNumber.trim().ifBlank { "+919876543210" },
                        adminEmail = adminEmail.trim().ifBlank { "muzamillp20@gmail.com" }
                    )
                    adminViewModel.updateWorkshopConfig(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TorqueBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_save_config_button")
            ) {
                Text("Save Workshop Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showSignOutDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD92D20)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_sign_out_button")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFD92D20))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
