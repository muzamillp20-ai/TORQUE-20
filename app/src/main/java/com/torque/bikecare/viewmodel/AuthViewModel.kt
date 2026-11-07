package com.torque.bikecare.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.torque.bikecare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = authRepository.currentUser
        )

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun isCurrentUserAdmin(): Boolean {
        val user = currentUser.value ?: return false
        return authRepository.isUserAdmin(user.email)
    }

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        onSuccess: () -> Unit
    ) {
        _uiState.value = AuthUiState.Loading
        authRepository.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            scope = viewModelScope,
            onAuthSuccess = {
                _uiState.value = AuthUiState.Idle
                onSuccess()
            },
            onAuthError = { msg ->
                _uiState.value = AuthUiState.Error(msg)
            },
            onAuthCancelled = {
                _uiState.value = AuthUiState.Idle
            }
        )
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onSuccess: () -> Unit
    ) {
        authRepository.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = viewModelScope,
            onAuthSuccess = onSuccess,
            onUnauthenticated = {
                _uiState.value = AuthUiState.Idle
            }
        )
    }

    fun signOut(
        credentialManager: CredentialManager,
        onComplete: () -> Unit
    ) {
        _uiState.value = AuthUiState.Loading
        authRepository.signOut(
            credentialManager = credentialManager,
            scope = viewModelScope,
            onSignOutComplete = {
                _uiState.value = AuthUiState.Idle
                onComplete()
            }
        )
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}
