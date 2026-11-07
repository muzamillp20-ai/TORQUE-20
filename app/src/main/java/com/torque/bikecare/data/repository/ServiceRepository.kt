package com.torque.bikecare.data.repository

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.torque.bikecare.R
import com.torque.bikecare.data.model.Service
import com.torque.bikecare.utils.OperationType
import com.torque.bikecare.utils.handleFirestoreError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ServiceRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val servicesRef = db.collection("services")

    /**
     * For customer browsing: only active services
     */
    fun observeActiveServices(): Flow<List<Service>> {
        return servicesRef
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Service::class.java) }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, servicesRef.path)
                }
                throw error
            }
    }

    /**
     * For admin service management: all services
     */
    fun observeAllServices(): Flow<List<Service>> {
        return servicesRef
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Service::class.java) }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, servicesRef.path)
                }
                throw error
            }
    }

    suspend fun createService(service: Service): Result<String> {
        val serviceId = if (service.serviceId.isNotBlank()) service.serviceId else "srv_${UUID.randomUUID().toString().take(8)}"
        val docRef = servicesRef.document(serviceId)
        val payload = service.copy(serviceId = serviceId).toMap()
        return try {
            docRef.set(payload).await()
            Result.success(serviceId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateService(service: Service): Result<Unit> {
        require(service.serviceId.isNotBlank()) { "Service ID must not be blank" }
        val docRef = servicesRef.document(service.serviceId)
        return try {
            val payload = service.toMap()
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleServiceActive(serviceId: String, isActive: Boolean): Result<Unit> {
        val docRef = servicesRef.document(serviceId)
        return try {
            docRef.update(
                mapOf(
                    "isActive" to isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> {
        val docRef = servicesRef.document(serviceId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun seedInitialServicesIfEmpty(): Result<Int> {
        return try {
            val snapshot = servicesRef.limit(1).get().await()
            if (!snapshot.isEmpty) {
                return Result.success(0)
            }

            val defaultServices = listOf(
                Service(
                    serviceId = "srv_general",
                    name = "Full Periodic Maintenance",
                    description = "Comprehensive 36-point check, oil change, spark plug cleanup, brake adjustment, chain lube and wash.",
                    price = 45.0,
                    category = "Maintenance",
                    durationMinutes = 120,
                    isActive = true
                ),
                Service(
                    serviceId = "srv_oil",
                    name = "Premium Engine Oil & Filter",
                    description = "Synthetic engine oil replacement, OEM oil filter change, engine flush and magnetic drain plug inspection.",
                    price = 25.0,
                    category = "Maintenance",
                    durationMinutes = 30,
                    isActive = true
                ),
                Service(
                    serviceId = "srv_brakes",
                    name = "Brake Overhaul & Fluid Bleed",
                    description = "Front and rear brake caliper cleaning, rotor inspection, brake pad replacement and DOT4 fluid flush.",
                    price = 35.0,
                    category = "Safety",
                    durationMinutes = 60,
                    isActive = true
                ),
                Service(
                    serviceId = "srv_chain",
                    name = "Chain & Sprocket Drive Overhaul",
                    description = "Chain slack adjustment, sonic cleaning, O-ring high-temp lubrication and drive sprocket tooth inspection.",
                    price = 20.0,
                    category = "Transmission",
                    durationMinutes = 45,
                    isActive = true
                ),
                Service(
                    serviceId = "srv_tuning",
                    name = "Engine Tuning & Fuel System Diagnostics",
                    description = "Throttle body synchronization, EFI diagnostics, fuel injector cleaner and valve clearance check.",
                    price = 55.0,
                    category = "Performance",
                    durationMinutes = 90,
                    isActive = true
                ),
                Service(
                    serviceId = "srv_inspection",
                    name = "Pre-Trip Complete Safety Audit",
                    description = "Suspension seals, tire tread wear check, wheel bearing play, battery voltage and lighting check.",
                    price = 15.0,
                    category = "Inspection",
                    durationMinutes = 30,
                    isActive = true
                )
            )

            for (service in defaultServices) {
                servicesRef.document(service.serviceId).set(service.toMap()).await()
            }
            Result.success(defaultServices.size)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, servicesRef.path)
            Result.failure(e)
        }
    }
}
