package com.torque.bikecare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Booking(
    val bookingId: String = "",
    val customerUid: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val bikeBrand: String = "",
    val bikeModel: String = "",
    val registrationNumber: String = "",
    val problemDescription: String = "",
    val preferredDate: String = "",
    val preferredTime: String = "",
    val status: String = STATUS_PENDING,
    val statusNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_ACCEPTED = "ACCEPTED"
        const val STATUS_IN_SERVICE = "IN_SERVICE"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_REJECTED = "REJECTED"
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["bookingId"] = bookingId
        map["customerUid"] = customerUid
        map["customerName"] = customerName
        map["customerEmail"] = customerEmail
        map["customerPhone"] = customerPhone
        map["serviceId"] = serviceId
        map["serviceName"] = serviceName
        map["bikeBrand"] = bikeBrand
        map["bikeModel"] = bikeModel
        map["registrationNumber"] = registrationNumber
        map["problemDescription"] = problemDescription
        map["preferredDate"] = preferredDate
        map["preferredTime"] = preferredTime
        map["status"] = status
        if (!statusNote.isNullOrBlank()) map["statusNote"] = statusNote
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }
}
