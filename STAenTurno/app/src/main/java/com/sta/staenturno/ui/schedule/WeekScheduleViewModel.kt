package com.sta.staenturno.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.data.model.DaySchedule
import com.sta.staenturno.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class WeekScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val desde: String = "",
    val hasta: String = "",
    val dias: Map<String, DaySchedule> = emptyMap()
)

class WeekScheduleViewModel(
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeekScheduleUiState())
    val uiState: StateFlow<WeekScheduleUiState> = _uiState.asStateFlow()

    fun cargarAgenda() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = prefsManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
                    return@launch
                }

                val response = RetrofitClient.apiService.getWeekSchedule("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val schedule = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            desde = schedule.desde,
                            hasta = schedule.hasta,
                            dias = schedule.dias
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar agenda: ${response.code()}") }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Error de conexión") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error desconocido: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
