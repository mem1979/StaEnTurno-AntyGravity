package com.sta.staenturno.ui.changePassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class ChangePasswordViewModel(
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(nueva: String) {
        if (nueva.length < 8) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 8 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = prefsManager.getToken()
                if (token == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No autenticado")
                    return@launch
                }

                val response = RetrofitClient.apiService.changePassword("Bearer $token", nueva)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al cambiar contraseña: ${response.code()}")
                }
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error de conexión")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error desconocido: ${e.message}")
            }
        }
    }
}
