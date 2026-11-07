package com.torque.bikecare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WorkshopConfig(
    val id: String = "main",
    val workshopName: String = "TORQUE Workshop",
    val workshopAddress: String = "12 Auto Hub, Service Road",
    val workshopPhone: String = "+91 98765 43210",
    val whatsappNumber: String = "+919876543210",
    val adminEmail: String = "muzamillp20@gmail.com",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["workshopName"] = workshopName
        map["workshopAddress"] = workshopAddress
        map["workshopPhone"] = workshopPhone
        map["whatsappNumber"] = whatsappNumber
        map["adminEmail"] = adminEmail
        map["updatedAt"] = FieldValue.serverTimestamp()
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }
}
