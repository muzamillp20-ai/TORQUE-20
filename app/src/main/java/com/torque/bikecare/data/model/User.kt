package com.torque.bikecare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val role: String = ROLE_CUSTOMER,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val ROLE_CUSTOMER = "CUSTOMER"
        const val ROLE_ADMIN = "ADMIN"
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["uid"] = uid
        map["name"] = name
        map["email"] = email
        if (!photoUrl.isNullOrBlank()) map["photoUrl"] = photoUrl
        if (!phoneNumber.isNullOrBlank()) map["phoneNumber"] = phoneNumber
        map["role"] = role
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }
}
