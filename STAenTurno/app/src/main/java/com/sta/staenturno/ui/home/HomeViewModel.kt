package com.sta.staenturno.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sta.staenturno.data.local.PrefsManager
import com.sta.staenturno.data.model.AttendanceRequest

import com.sta.staenturno.data.remote.RetrofitClient
import com.sta.staenturno.util.DeviceIdProvider
import com.sta.staenturno.util.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Represents the possible attendance states of the user.
 */
enum class AttendanceState {
    NOT_STARTED,
    WORKING,
    PAUSED,
    FINISHED
}

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val nombreCompleto: String = "",
    val turnoActivoHoy: String = "",
    val attendanceState: AttendanceState = AttendanceState.NOT_STARTED,
    val mensaje: String? = null,
    val ultimoTipo: String? = null,
    val ultimaHora: String? = null,
    val horasTrabajadas: String? = null,
    val yaFichoEntrada: Boolean = false,
    val horaEntrada: String? = null,
    val yaFichoSalida: Boolean = false,
    val horaSalida: String? = null,
    val tieneLicencia: Boolean = false,
    val descripcionLicencia: String? = null,
    val esFeriado: Boolean = false,
    val descripcionFeriado: String? = null,
    val aceptaPausa: Boolean = false
)

class HomeViewModel(
    private val prefsManager: PrefsManager,
    private val deviceIdProvider: DeviceIdProvider,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Persist the attendance state in SharedPreferences */
    private fun persistAttendanceState(state: AttendanceState) {
        prefsManager.saveString(PrefsManager.KEY_ATTENDANCE_STATE, state.name)
    }

    /** Helper to update UI state and persist the attendance state */
    private fun updateAttendanceState(state: AttendanceState) {
        _uiState.update { it.copy(attendanceState = state) }
        persistAttendanceState(state)
    }

    /** Load the home data from the backend and restore any persisted attendance state */
    fun cargarHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = prefsManager.getToken()
                if (token == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No autenticado") }
                    return@launch
                }
                val bearer = "Bearer $token"

                // Load profile
                val meResponse = RetrofitClient.apiService.getMe(bearer)
                if (meResponse.isSuccessful && meResponse.body() != null) {
                    val me = meResponse.body()!!
                    _uiState.update {
                        it.copy(
                            nombreCompleto = me.nombreCompleto,
                            turnoActivoHoy = me.turnoActivoHoy,
                            aceptaPausa = me.aceptaPausa
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar perfil") }
                    return@launch
                }

                // Load attendance for today
                val attResponse = RetrofitClient.apiService.getAttendanceToday(bearer)
                if (attResponse.isSuccessful && attResponse.body() != null) {
                    val att = attResponse.body()!!
                    // Determine state from backend
                    val backendState = when {
                        att.yaFichoSalida -> AttendanceState.FINISHED
                        att.yaFichoEntrada -> AttendanceState.WORKING
                        else -> AttendanceState.NOT_STARTED
                    }
                    
                    // Load persisted state (e.g., PAUSED) only if we are currently WORKING
                    var finalState = backendState
                    var debugMsg = ""
                    if (backendState == AttendanceState.WORKING) {
                        val savedState = prefsManager.getString(PrefsManager.KEY_ATTENDANCE_STATE)
                        debugMsg = "Backend: WORKING, Saved: $savedState"
                        if (!savedState.isNullOrEmpty()) {
                            try {
                                val persisted = AttendanceState.valueOf(savedState)
                                if (persisted == AttendanceState.PAUSED) {
                                    finalState = AttendanceState.PAUSED
                                }
                            } catch (e: Exception) {
                                debugMsg += " (Error parsing)"
                            }
                        }
                    }

                    // Calculate worked hours if shift finished
                    val horasTrabajadas = if (backendState == AttendanceState.FINISHED && att.horaEntrada != null && att.horaSalida != null) {
                        calcularHorasTrabajadas(att.horaEntrada, att.horaSalida)
                    } else null

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attendanceState = finalState,
                            yaFichoEntrada = att.yaFichoEntrada,
                            horaEntrada = att.horaEntrada,
                            yaFichoSalida = att.yaFichoSalida,
                            horaSalida = att.horaSalida,
                            horasTrabajadas = horasTrabajadas,
                            tieneLicencia = att.tieneLicencia,
                            descripcionLicencia = att.descripcionLicencia,
                            esFeriado = att.esFeriado,
                            descripcionFeriado = att.descripcionFeriado,
                            // Show debug info in error field temporarily if needed, or just log it
                            // error = if (debugMsg.isNotEmpty()) debugMsg else null 
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar asistencia") }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Error de conexión") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error desconocido: ${e.message}") }
            }
        }
    }

    /** Register an attendance movement (ENTRADA, PAUSA_INICIO, PAUSA_FIN, SALIDA) */
    fun registrarAsistencia(tipoMovimiento: String) {
        viewModelScope.launch {
            if (!locationProvider.hasLocationPermission()) {
                _uiState.update { it.copy(error = "Se requieren permisos de ubicación") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }

            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudo obtener la ubicación") }
                return@launch
            }

            try {
                val token = prefsManager.getToken() ?: return@launch
                val deviceId = deviceIdProvider.getDeviceId()
                val request = AttendanceRequest(
                    ubicacion = "${location.first},${location.second}",
                    tipoMovimiento = tipoMovimiento
                )
                val response = RetrofitClient.apiService.registerAttendance("Bearer $token", deviceId, request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Determine new state
                    val newState = when (tipoMovimiento) {
                        "ENTRADA" -> AttendanceState.WORKING
                        "PAUSA_INICIO" -> AttendanceState.PAUSED
                        "PAUSA_FIN" -> AttendanceState.WORKING
                        "SALIDA" -> AttendanceState.FINISHED
                        else -> _uiState.value.attendanceState
                    }
                    // Persist state
                    updateAttendanceState(newState)

                    // Update UI fields
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensaje = body.mensaje,
                            ultimoTipo = body.tipo,
                            ultimaHora = body.hora,
                            // Update entry/exit times when appropriate
                            horaEntrada = if (tipoMovimiento == "ENTRADA") body.hora else it.horaEntrada,
                            yaFichoEntrada = if (tipoMovimiento == "ENTRADA") true else it.yaFichoEntrada,
                            horaSalida = if (tipoMovimiento == "SALIDA") body.hora else it.horaSalida,
                            yaFichoSalida = if (tipoMovimiento == "SALIDA") true else it.yaFichoSalida,
                            // Calculate worked hours when finishing shift
                            horasTrabajadas = if (newState == AttendanceState.FINISHED && it.horaEntrada != null) {
                                calcularHorasTrabajadas(it.horaEntrada!!, body.hora)
                            } else it.horasTrabajadas
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al registrar: ${response.code()}") }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Error de conexión") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
            }
        }
    }

    /** Helper to calculate worked hours */
    private fun calcularHorasTrabajadas(horaEntrada: String, horaSalida: String): String {
        return try {
            val entrada = java.time.LocalTime.parse(horaEntrada, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            val salida = java.time.LocalTime.parse(horaSalida, java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            val duracion = java.time.Duration.between(entrada, salida)
            val horas = duracion.toHours()
            val minutos = duracion.toMinutes() % 60
            "${horas}h ${minutos}m"
        } catch (e: Exception) {
            "No disponible"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
