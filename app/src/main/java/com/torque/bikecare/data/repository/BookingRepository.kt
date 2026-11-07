package com.torque.bikecare.data.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.torque.bikecare.R
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.utils.OperationType
import com.torque.bikecare.utils.handleFirestoreError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BookingRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val bookingsRef = db.collection("bookings")

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing booking operations.")
    }

    suspend fun createBooking(booking: Booking): Result<String> {
        val uid = requireUserId()
        val bookingId = if (booking.bookingId.isNotBlank()) booking.bookingId else "bk_${UUID.randomUUID().toString().take(8)}"
        val docRef = bookingsRef.document(bookingId)

        val payload = booking.copy(
            bookingId = bookingId,
            customerUid = uid,
            status = Booking.STATUS_PENDING
        ).toMap()

        return try {
            docRef.set(payload).await()
            Result.success(bookingId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeCustomerBookings(customerUid: String): Flow<List<Booking>> {
        return bookingsRef
            .whereEqualTo("customerUid", customerUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Booking::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, bookingsRef.path)
                }
                throw error
            }
    }

    fun observeAllBookings(): Flow<List<Booking>> {
        return bookingsRef
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Booking::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, bookingsRef.path)
                }
                throw error
            }
    }

    suspend fun getBooking(bookingId: String): Result<Booking?> {
        val docRef = bookingsRef.document(bookingId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(snapshot.toObject(Booking::class.java))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        note: String? = null
    ): Result<Unit> {
        val docRef = bookingsRef.document(bookingId)
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (note != null) {
            updates["statusNote"] = note
        }

        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
