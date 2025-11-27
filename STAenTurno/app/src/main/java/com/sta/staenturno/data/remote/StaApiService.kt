package com.sta.staenturno.data.remote

import com.sta.staenturno.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface StaApiService {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Header("X-Device-ID") deviceId: String,
        @Field("usuario") usuario: String,
        @Field("contrasena") contrasena: String
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @GET("asistencia/hoy")
    suspend fun getAttendanceToday(
        @Header("Authorization") token: String
    ): Response<AttendanceStatusResponse>

    @POST("asistencia")
    suspend fun registerAttendance(
        @Header("Authorization") token: String,
        @Header("X-Device-ID") deviceId: String,
        @Body request: AttendanceRequest
    ): Response<AttendanceRegistrationResponse>

    @FormUrlEncoded
    @POST("auth/cambiarClave")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Field("nueva") nueva: String
    ): Response<ChangePasswordResponse>
}
