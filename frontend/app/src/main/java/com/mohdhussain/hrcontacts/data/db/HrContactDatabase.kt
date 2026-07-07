package com.mohdhussain.hrcontacts.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mohdhussain.hrcontacts.data.model.HrContact

@Database(entities = [HrContact::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE hr_contacts_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "company TEXT NOT NULL, " +
                        "mobile TEXT NOT NULL, " +
                        "emails TEXT NOT NULL, " +
                        "linkedinProfile TEXT NOT NULL DEFAULT '')"
                )
                db.query("SELECT id, name, company, mobile, email, linkedinProfile FROM hr_contacts").use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow("id")
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    val companyIndex = cursor.getColumnIndexOrThrow("company")
                    val mobileIndex = cursor.getColumnIndexOrThrow("mobile")
                    val emailIndex = cursor.getColumnIndexOrThrow("email")
                    val linkedinIndex = cursor.getColumnIndexOrThrow("linkedinProfile")
                    while (cursor.moveToNext()) {
                        val email = cursor.getString(emailIndex) ?: ""
                        val emailsJson = Converters.fromEmailsList(
                            if (email.isBlank()) emptyList() else listOf(email)
                        )
                        db.execSQL(
                            "INSERT INTO hr_contacts_new (id, name, company, mobile, emails, linkedinProfile) " +
                                "VALUES (?, ?, ?, ?, ?, ?)",
                            arrayOf<Any>(
                                cursor.getLong(idIndex),
                                cursor.getString(nameIndex),
                                cursor.getString(companyIndex),
                                cursor.getString(mobileIndex),
                                emailsJson,
                                cursor.getString(linkedinIndex) ?: ""
                            )
                        )
                    }
                }
                db.execSQL("DROP TABLE hr_contacts")
                db.execSQL("ALTER TABLE hr_contacts_new RENAME TO hr_contacts")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hr_contacts ADD COLUMN serverId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE hr_contacts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE hr_contacts ADD COLUMN pendingAction TEXT NOT NULL DEFAULT 'CREATE'")
            }
        }

        fun getDatabase(context: Context): HrContactDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HrContactDatabase::class.java,
                    "hr_contacts.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
