package com.torque.bikecare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.ui.theme.StatusAccepted
import com.torque.bikecare.ui.theme.StatusCompleted
import com.torque.bikecare.ui.theme.StatusInService
import com.torque.bikecare.ui.theme.StatusPending
import com.torque.bikecare.ui.theme.StatusRejected

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        Booking.STATUS_PENDING -> Triple(StatusPending.copy(alpha = 0.12f), StatusPending, "Pending Approval")
        Booking.STATUS_ACCEPTED -> Triple(StatusAccepted.copy(alpha = 0.12f), StatusAccepted, "Booking Accepted")
        Booking.STATUS_IN_SERVICE -> Triple(StatusInService.copy(alpha = 0.12f), StatusInService, "In Service")
        Booking.STATUS_COMPLETED -> Triple(StatusCompleted.copy(alpha = 0.12f), StatusCompleted, "Service Completed")
        Booking.STATUS_REJECTED -> Triple(StatusRejected.copy(alpha = 0.12f), StatusRejected, "Declined")
        else -> Triple(Color(0xFF64748B).copy(alpha = 0.12f), Color(0xFF64748B), status)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
