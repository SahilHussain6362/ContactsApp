package com.mohdhussain.hrcontacts.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact

@Database(entities = [HrContact::class], version = 2, exportSchema = false)
abstract class HrContactDatabase : RoomDatabase() {

    abstract fun hrContactDao(): HrContactDao

    companion object {
        @Volatile
        private var INSTANCE: HrContactDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hr_contacts ADD COLUMN linkedinProfile TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): HrContactDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HrContactDatabase::class.java,
                    "hr_contacts.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
