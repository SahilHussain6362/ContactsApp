package com.mohdhussain.hrcontacts.data.remote

import com.mohdhussain.hrcontacts.data.remote.dto.AuthResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.BatchSyncResponseDto
import com.mohdhussain.hrcontacts.data.remote.dto.ContactRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.GoogleAuthRequestDto
import com.mohdhussain.hrcontacts.data.remote.dto.RemoteContact
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
