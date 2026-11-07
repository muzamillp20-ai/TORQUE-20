package com.torque.bikecare.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {
    /**
     * Opens WhatsApp Click-to-Chat with the given international number and optional message.
     * International number must not contain '+' or dashes when passed to wa.me (e.g. "919876543210").
     */
    fun openChat(context: Context, rawNumber: String?, message: String? = null) {
        val sanitizedNumber = sanitizePhoneNumber(rawNumber)
        if (sanitizedNumber.isBlank()) {
            Toast.makeText(
                context,
                "Workshop WhatsApp number is not configured yet. Please configure it in Admin Settings.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val encodedMessage = message?.let {
                "&text=" + URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val url = "https://api.whatsapp.com/send?phone=$sanitizedNumber$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sanitizePhoneNumber(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        // Remove spaces, hyphens, parentheses, and leading plus sign
        return raw.replace(Regex("[^0-9]"), "")
    }
}
