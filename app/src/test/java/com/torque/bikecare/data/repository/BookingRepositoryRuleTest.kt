package com.torque.bikecare.data.repository

import org.junit.Assert.assertTrue
import com.torque.bikecare.base.FirestoreEmulatorTestBase
import com.torque.bikecare.data.model.Booking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import java.util.UUID

class BookingRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBooking_authenticatedCustomer_createsAndObserves() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BookingRepository(firestore)

        val uniqueBookingId = "bk_${UUID.randomUUID().toString().take(8)}"
        val booking = Booking(
            bookingId = uniqueBookingId,
            customerUid = aliceUid,
            customerName = "Alice Rider",
            customerEmail = ALICE_EMAIL,
            customerPhone = "+1234567890",
            serviceId = "srv_general",
            serviceName = "Full Periodic Maintenance",
            bikeBrand = "Yamaha",
            bikeModel = "MT-15",
            registrationNumber = "MH02AB1234",
            problemDescription = "Brake pad sound",
            preferredDate = "2026-11-20",
            preferredTime = "10:00 AM"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBooking(booking)
        }
        assertTrue(result.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCustomerBookings(aliceUid).first { list ->
                list.any { it.bookingId == uniqueBookingId }
            }
        }
        assertTrue(observed.any { it.bookingId == uniqueBookingId })
    }

    @Test
    fun observeCustomerBookings_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = BookingRepository(firestore)

        val result = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCustomerBookings("unauth_uid").first()
            }
        }
        assertTrue(result.isFailure)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
