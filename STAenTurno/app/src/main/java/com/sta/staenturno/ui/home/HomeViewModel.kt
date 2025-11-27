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
import kotlinx.coroutines.launch
import java.io.IOException

    enum class AttendanceState {
        NOT_STARTED,
        WORKING,
        PAUSED,
        FINISHED
    }

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

        fun cargarHome() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                try {
                    val token = prefsManager.getToken()
                    if (token == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "No autenticado")
                        return@launch
                    }
                    val bearerToken = "Bearer $token"

                    // Call /auth/me
                    val meResponse = RetrofitClient.apiService.getMe(bearerToken)
                    if (meResponse.isSuccessful && meResponse.body() != null) {
                        val me = meResponse.body()!!
                        _uiState.value = _uiState.value.copy(
                            nombreCompleto = me.nombreCompleto,
                            turnoActivoHoy = me.turnoActivoHoy,
                            aceptaPausa = me.aceptaPausa
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al cargar perfil")
                        return@launch
                    }

                    // Call /asistencia/hoy
                    val attendanceResponse = RetrofitClient.apiService.getAttendanceToday(bearerToken)
                    if (attendanceResponse.isSuccessful && attendanceResponse.body() != null) {
                        val att = attendanceResponse.body()!!
                        
                        // Determine initial state based on backend data
                        val initialState = when {
                            att.yaFichoSalida -> AttendanceState.FINISHED
                            att.yaFichoEntrada -> AttendanceState.WORKING
                            else -> AttendanceState.NOT_STARTED
                        }
                        
                        // Calculate hours if shift is finished
                        val horasTrabajadas = if (initialState == AttendanceState.FINISHED && 
                                                   att.horaEntrada != null && 
                                                   att.horaSalida != null) {
                            calcularHorasTrabajadas(att.horaEntrada, att.horaSalida)
                        } else null
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            attendanceState = initialState,
                            yaFichoEntrada = att.yaFichoEntrada,
                            horaEntrada = att.horaEntrada,
                            yaFichoSalida = att.yaFichoSalida,
                            horaSalida = att.horaSalida,
                            horasTrabajadas = horasTrabajadas,
                            tieneLicencia = att.tieneLicencia,
                            descripcionLicencia = att.descripcionLicencia,
                            esFeriado = att.esFeriado,
                            descripcionFeriado = att.descripcionFeriado
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al cargar asistencia")
                    }

                } catch (e: IOException) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error de conexión")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error desconocido: ${e.message}")
                }
            }
        }

        fun registrarAsistencia(tipoMovimiento: String) {
            viewModelScope.launch {
                if (!locationProvider.hasLocationPermission()) {
                    _uiState.value = _uiState.value.copy(error = "Se requieren permisos de ubicación")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No se pudo obtener la ubicación")
                    return@launch
                }

                try {
                    val token = prefsManager.getToken() ?: return@launch
                    val deviceId = deviceIdProvider.getDeviceId()
                    val locationString = "${location.first},${location.second}"
                    
                    val request = AttendanceRequest(
                        ubicacion = locationString,
                        tipoMovimiento = tipoMovimiento
                    )
                    val response = RetrofitClient.apiService.registerAttendance("Bearer $token", deviceId, request)

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        
                        // Update state based on action
                        val newState = when (tipoMovimiento) {
                            "ENTRADA" -> AttendanceState.WORKING
                            "PAUSA_INICIO" -> AttendanceState.PAUSED
                            "PAUSA_FIN" -> AttendanceState.WORKING
                            "SALIDA" -> AttendanceState.FINISHED
                            else -> _uiState.value.attendanceState
                        }

                        // Calculate worked hours if finishing shift
                        val horasTrabajadas = if (newState == AttendanceState.FINISHED && _uiState.value.horaEntrada != null) {
                            calcularHorasTrabajadas(_uiState.value.horaEntrada!!, body.hora)
                        } else null

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            mensaje = body.mensaje,
                            ultimoTipo = body.tipo,
                            ultimaHora = body.hora,
                            attendanceState = newState,
                            horasTrabajadas = horasTrabajadas,
                            // Update horaEntrada if this was ENTRADA
                            horaEntrada = if (tipoMovimiento == "ENTRADA") body.hora else _uiState.value.horaEntrada,
                            yaFichoEntrada = if (tipoMovimiento == "ENTRADA") true else _uiState.value.yaFichoEntrada
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al registrar: ${response.code()}")
                    }
                } catch (e: IOException) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error de conexión")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error: ${e.message}")
                }
            }
        }
        
        private fun calcularHorasTrabajadas(horaEntrada: String, horaSalida: String): String {
            return try {
                // Parse times in HH:mm format
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
            _uiState.value = _uiState.value.copy(error = null)
        }
    }
