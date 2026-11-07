package com.torque.bikecare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torque.bikecare.data.model.Booking
import com.torque.bikecare.data.model.Service
import com.torque.bikecare.data.model.WorkshopConfig
import com.torque.bikecare.data.repository.BookingRepository
import com.torque.bikecare.data.repository.ServiceRepository
import com.torque.bikecare.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface BookingSubmissionState {
    object Idle : BookingSubmissionState
    object Submitting : BookingSubmissionState
    data class Success(val bookingId: String) : BookingSubmissionState
    data class Error(val message: String) : BookingSubmissionState
}

class CustomerViewModel(
    private val serviceRepository: ServiceRepository,
    private val bookingRepository: BookingRepository,
    private val workshopRepository: WorkshopRepository,
    private val currentUserId: String
) : ViewModel() {

    val activeServices: StateFlow<UiState<List<Service>>> = serviceRepository
        .observeActiveServices()
        .map<List<Service>, UiState<List<Service>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load services")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val myBookings: StateFlow<UiState<List<Booking>>> = bookingRepository
        .observeCustomerBookings(currentUserId)
        .map<List<Booking>, UiState<List<Booking>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load bookings")) }
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

    private val _selectedService = MutableStateFlow<Service?>(null)
    val selectedService: StateFlow<Service?> = _selectedService.asStateFlow()

    private val _bookingState = MutableStateFlow<BookingSubmissionState>(BookingSubmissionState.Idle)
    val bookingState: StateFlow<BookingSubmissionState> = _bookingState.asStateFlow()

    fun selectService(service: Service?) {
        _selectedService.value = service
    }

    fun resetBookingState() {
        _bookingState.value = BookingSubmissionState.Idle
    }

    fun submitBooking(
        service: Service,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        bikeBrand: String,
        bikeModel: String,
        registrationNumber: String,
        problemDescription: String,
        preferredDate: String,
        preferredTime: String
    ) {
        if (customerName.isBlank() || customerPhone.isBlank() ||
            bikeBrand.isBlank() || bikeModel.isBlank() ||
            registrationNumber.isBlank() || problemDescription.isBlank() ||
            preferredDate.isBlank() || preferredTime.isBlank()
        ) {
            _bookingState.value = BookingSubmissionState.Error("Please fill in all required fields.")
            return
        }

        _bookingState.value = BookingSubmissionState.Submitting
        viewModelScope.launch {
            val booking = Booking(
                customerUid = currentUserId,
                customerName = customerName.trim(),
                customerEmail = customerEmail.trim(),
                customerPhone = customerPhone.trim(),
                serviceId = service.serviceId,
                serviceName = service.name,
                bikeBrand = bikeBrand.trim(),
                bikeModel = bikeModel.trim(),
                registrationNumber = registrationNumber.trim().uppercase(),
                problemDescription = problemDescription.trim(),
                preferredDate = preferredDate.trim(),
                preferredTime = preferredTime.trim()
            )

            val result = bookingRepository.createBooking(booking)
            if (result.isSuccess) {
                _bookingState.value = BookingSubmissionState.Success(result.getOrThrow())
            } else {
                _bookingState.value = BookingSubmissionState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to save booking. Please try again."
                )
            }
        }
    }
}
