package com.mohdhussain.hrcontacts.data.remote

import com.mohdhussain.hrcontacts.data.auth.AuthEventBus
import com.mohdhussain.hrcontacts.data.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isAuthEndpoint = original.url.encodedPath.contains("/auth/")

        val request = if (isAuthEndpoint) {
            original
        } else {
            tokenManager.jwt?.let { token ->
                original.newBuilder().addHeader("Authorization", "Bearer $token").build()
            } ?: original
        }

        val response = chain.proceed(request)
        if (!isAuthEndpoint && response.code == 401) {
            AuthEventBus.notifyUnauthorized()
        }
        return response
    }
}
