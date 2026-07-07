package com.mohdhussain.hrcontacts.data.remote

import com.mohdhussain.hrcontacts.data.remote.dto.AuthResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.ContactRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.ForgotPasswordSendOtpRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.GoogleAuthRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.LoginRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RegisterSendOtpRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RegisterVerifyRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RemoteContact
import com.mohdhussain.hrcontacts.data.remote.dto.ResetPasswordRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequestDto): AuthResponseDto

    // Bare Unit (not Response<Unit>) so Retrofit throws HttpException on 409/429 instead of
    // silently returning a response object whose failure the caller has to remember to check.
    @POST("api/v1/auth/register/send-otp")
    suspend fun registerSendOtp(@Body request: RegisterSendOtpRequestDto)

    @POST("api/v1/auth/register/verify-otp")
    suspend fun registerVerify(@Body request: RegisterVerifyRequestDto): AuthResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/forgot-password/send-otp")
    suspend fun forgotPasswordSendOtp(@Body request: ForgotPasswordSendOtpRequestDto)

    @POST("api/v1/auth/forgot-password/verify-otp")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): AuthResponseDto

    @GET("api/contacts")
    suspend fun getContacts(): List<RemoteContact>

    @POST("api/contacts")
    suspend fun createContact(@Body request: ContactRequestDto): RemoteContact

    @PUT("api/contacts/{id}")
    suspend fun updateContact(@Path("id") id: String, @Body request: ContactRequestDto): RemoteContact

    @DELETE("api/contacts/{id}")
    suspend fun deleteContact(@Path("id") id: String): Response<Unit>

    @GET("api/contacts/changes")
    suspend fun getChanges(@Query("since") since: Long): List<RemoteContact>

    @POST("api/contacts/batch-sync")
    suspend fun batchSync(@Body request: BatchSyncRequestDto): BatchSyncResponseDto
}
