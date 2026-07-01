package com.mohdhussain.hrcontacts.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact

@Database(entities = [HrContact::class], version = 1, exportSchema = false)
abstract class HrContactDatabase : RoomDatabase() {

    abstract fun hrContactDao(): HrContactDao

    companion object {
        @Volatile
        private var INSTANCE: HrContactDatabase? = null

        fun getDatabase(context: Context): HrContactDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HrContactDatabase::class.java,
                    "hr_contacts.db"
                ).build().also { INSTANCE = it }
            }
    }
}
