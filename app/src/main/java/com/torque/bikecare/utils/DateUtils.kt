package com.torque.bikecare.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "Just now"
        return displayDateFormat.format(timestamp.toDate())
    }

    fun formatDateTime(timestamp: Timestamp?): String {
        if (timestamp == null) return "Just now"
        return displayDateTimeFormat.format(timestamp.toDate())
    }

    fun todayIso(): String {
        return isoDateFormat.format(Date())
    }

    fun formatIsoDate(iso: String): String {
        return try {
            val date = isoDateFormat.parse(iso)
            date?.let { displayDateFormat.format(it) } ?: iso
        } catch (e: Exception) {
            iso
        }
    }
}
