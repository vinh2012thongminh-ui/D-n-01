package com.vinhho.onthi10ai.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vinhho.onthi10ai.data.model.AppUser
import com.vinhho.onthi10ai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Authenticated(val user: AppUser) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun isAlreadyLoggedIn(): Boolean = authRepository.isRealUserLoggedIn()

    fun isRealAccountLoggedIn(): Boolean = isAlreadyLoggedIn()

    fun clearGuestSessionOnStartup() {
        authRepository.clearGuestSessionIfPresent()
    }

    fun getLastGoogleAccount(): Pair<String, String>? = authRepository.getLastGoogleAccount()

    fun signInWithGoogleIdToken(idToken: String, displayName: String, email: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInWithGoogleIdToken(idToken, displayName, email)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Authenticated(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.") }
            )
        }
    }

    fun signInWithGoogle(displayName: String, email: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInWithGoogle(displayName, email)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Authenticated(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.") }
            )
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.loginWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Authenticated(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Đăng nhập thất bại. Vui lòng thử lại.") }
            )
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.registerWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Authenticated(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Đăng ký tài khoản thất bại.") }
            )
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInAnonymously()
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Authenticated(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Không thể vào chế độ Trải nghiệm nhanh.") }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    }
}
