package com.mohdhussain.hrcontacts

import android.app.Application
import com.mohdhussain.hrcontacts.data.remote.RetrofitClient

class HrContactsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
