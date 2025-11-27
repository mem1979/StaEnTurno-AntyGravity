package com.sta.staenturno.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.data.remote.RetrofitClient
import com.sta.staenturno.util.DeviceIdProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val mustChangePassword: Boolean = false
)

class LoginViewModel(
    private val deviceIdProvider: DeviceIdProvider,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Usuario y contraseña requeridos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isSuccess = false)
            try {
                val deviceId = deviceIdProvider.getDeviceId()
                val response = RetrofitClient.apiService.login(deviceId, usuario, contrasena)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    prefsManager.saveToken(body.token)
                    
                    if (body.passwordDefault) {
                        _uiState.value = _uiState.value.copy(isLoading = false, mustChangePassword = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    }
                } else {
                    val errorMsg = if (response.code() == 401) {
                        "Credenciales incorrectas o dispositivo no autorizado"
                    } else {
                        "Error en el servidor: ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error de conexión. Verifique su internet.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error desconocido: ${e.message}")
            }
        }
    }
    
    fun onBiometricFailure() {
        _uiState.value = _uiState.value.copy(isSuccess = false, error = "Error en la identificación biométrica")
    }
    
    fun errorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
