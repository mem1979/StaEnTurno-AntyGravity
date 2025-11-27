package com.sta.staenturno.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("contrasena") val contrasena: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("usuario") val usuario: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("passwordDefault") val passwordDefault: Boolean
)

data class UserResponse(
    @SerializedName("usuario") val usuario: String,
    @SerializedName("turnoActivoHoy") val turnoActivoHoy: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("aceptaPausa") val aceptaPausa: Boolean = false
)

data class AttendanceStatusResponse(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("yaFichoEntrada") val yaFichoEntrada: Boolean,
    @SerializedName("horaEntrada") val horaEntrada: String?,
    @SerializedName("yaFichoSalida") val yaFichoSalida: Boolean = false,
    @SerializedName("horaSalida") val horaSalida: String? = null,
    @SerializedName("tieneLicencia") val tieneLicencia: Boolean = false,
    @SerializedName("tipoLicencia") val tipoLicencia: String? = null,
    @SerializedName("descripcionLicencia") val descripcionLicencia: String? = null,
    @SerializedName("esFeriado") val esFeriado: Boolean = false,
    @SerializedName("descripcionFeriado") val descripcionFeriado: String? = null
)

data class AttendanceRequest(
    @SerializedName("descripcionTipo") val descripcionTipo: String? = null,
    @SerializedName("tipoMovimiento") val tipoMovimiento: String? = null,
    @SerializedName("ubicacion") val ubicacion: String
)

data class AttendanceRegistrationResponse(
    @SerializedName("estado") val estado: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("turnoActivoHoy") val turnoActivoHoy: String
)

data class ChangePasswordRequest(
    @SerializedName("nueva") val nueva: String
)

data class ChangePasswordResponse(
    @SerializedName("success") val success: Boolean
)
