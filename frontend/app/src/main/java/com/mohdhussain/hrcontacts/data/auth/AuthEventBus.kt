package com.mohdhussain.hrcontacts.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Lets a background OkHttp interceptor signal the UI layer (which owns navigation) that the
// session is no longer valid, without the interceptor needing a reference to any Activity/Fragment.
object AuthEventBus {

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    fun notifyUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
