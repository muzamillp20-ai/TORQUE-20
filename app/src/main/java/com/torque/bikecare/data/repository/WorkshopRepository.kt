package com.torque.bikecare.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.torque.bikecare.R
import com.torque.bikecare.data.model.User
import com.torque.bikecare.data.model.WorkshopConfig
import com.torque.bikecare.utils.OperationType
import com.torque.bikecare.utils.handleFirestoreError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkshopRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val configDoc = db.collection("workshop_config").document("main")
    private val usersRef = db.collection("users")

    fun observeConfig(): Flow<WorkshopConfig> {
        return configDoc
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(WorkshopConfig::class.java) ?: WorkshopConfig()
                } else {
                    WorkshopConfig()
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, configDoc.path)
                }
                emit(WorkshopConfig())
            }
    }

    suspend fun updateConfig(config: WorkshopConfig): Result<Unit> {
        return try {
            configDoc.set(config.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, configDoc.path)
            Result.failure(e)
        }
    }

    fun observeAllCustomers(): Flow<List<User>> {
        return usersRef
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, usersRef.path)
                }
                throw error
            }
    }
}
