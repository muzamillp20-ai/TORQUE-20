package com.torque.bikecare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.data.model.Service
import com.torque.bikecare.data.model.User
import com.torque.bikecare.data.model.WorkshopConfig
import com.torque.bikecare.data.repository.BookingRepository
import com.torque.bikecare.data.repository.ServiceRepository
import com.torque.bikecare.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminDashboardStats(
    val total: Int = 0,
    val pending: Int = 0,
    val accepted: Int = 0,
    val inService: Int = 0,
    val completed: Int = 0,
    val rejected: Int = 0
)

class AdminViewModel(
    private val bookingRepository: BookingRepository,
    private val serviceRepository: ServiceRepository,
    private val workshopRepository: WorkshopRepository
) : ViewModel() {

    val allBookings: StateFlow<UiState<List<Booking>>> = bookingRepository
        .observeAllBookings()
        .map<List<Booking>, UiState<List<Booking>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to observe workshop bookings")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val stats: StateFlow<AdminDashboardStats> = allBookings.map { uiState ->
        if (uiState is UiState.Success) {
            val list = uiState.data
            AdminDashboardStats(
                total = list.size,
                pending = list.count { it.status == Booking.STATUS_PENDING },
                accepted = list.count { it.status == Booking.STATUS_ACCEPTED },
                inService = list.count { it.status == Booking.STATUS_IN_SERVICE },
                completed = list.count { it.status == Booking.STATUS_COMPLETED },
                rejected = list.count { it.status == Booking.STATUS_REJECTED }
            )
        } else {
            AdminDashboardStats()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AdminDashboardStats()
    )

    val allServices: StateFlow<UiState<List<Service>>> = serviceRepository
        .observeAllServices()
        .map<List<Service>, UiState<List<Service>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load services")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val allCustomers: StateFlow<UiState<List<User>>> = workshopRepository
        .observeAllCustomers()
        .map<List<User>, UiState<List<User>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load customers")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val workshopConfig: StateFlow<WorkshopConfig> = workshopRepository
        .observeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = WorkshopConfig()
        )

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun updateBookingStatus(bookingId: String, newStatus: String, note: String? = null) {
        viewModelScope.launch {
            val result = bookingRepository.updateBookingStatus(bookingId, newStatus, note)
            if (result.isSuccess) {
                _actionMessage.value = "Booking updated to $newStatus"
            } else {
                _actionMessage.value = "Failed to update booking: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun saveService(service: Service, isEdit: Boolean) {
        viewModelScope.launch {
            val result = if (isEdit) {
                serviceRepository.updateService(service)
            } else {
                serviceRepository.createService(service)
            }
            if (result.isSuccess) {
                _actionMessage.value = if (isEdit) "Service updated successfully" else "New service created"
            } else {
                _actionMessage.value = "Failed to save service: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun toggleServiceActive(serviceId: String, isActive: Boolean) {
        viewModelScope.launch {
            val result = serviceRepository.toggleServiceActive(serviceId, isActive)
            if (result.isSuccess) {
                _actionMessage.value = if (isActive) "Service enabled" else "Service disabled"
            } else {
                _actionMessage.value = "Failed to toggle service: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            val result = serviceRepository.deleteService(serviceId)
            if (result.isSuccess) {
                _actionMessage.value = "Service deleted"
            } else {
                _actionMessage.value = "Failed to delete service: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun seedDefaultServices() {
        viewModelScope.launch {
            val result = serviceRepository.seedInitialServicesIfEmpty()
            if (result.isSuccess) {
                val count = result.getOrThrow()
                _actionMessage.value = if (count > 0) "Added $count default service packages" else "Catalog already contains services"
            } else {
                _actionMessage.value = "Failed to seed services: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateWorkshopConfig(config: WorkshopConfig) {
        viewModelScope.launch {
            val result = workshopRepository.updateConfig(config)
            if (result.isSuccess) {
                _actionMessage.value = "Workshop settings saved"
            } else {
                _actionMessage.value = "Failed to save workshop settings: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }
}
