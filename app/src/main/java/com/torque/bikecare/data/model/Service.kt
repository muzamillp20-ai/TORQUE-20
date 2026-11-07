package com.torque.bikecare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Service(
    val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "Maintenance",
    val durationMinutes: Long = 60L,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["serviceId"] = serviceId
        map["name"] = name
        map["description"] = description
        map["price"] = price
        map["category"] = category
        map["durationMinutes"] = durationMinutes
        if (!imageUrl.isNullOrBlank()) map["imageUrl"] = imageUrl
        map["isActive"] = isActive
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }
}
