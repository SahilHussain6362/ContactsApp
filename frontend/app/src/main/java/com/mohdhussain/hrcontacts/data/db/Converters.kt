package com.mohdhussain.hrcontacts.data.db

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object Converters {

    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    @JvmStatic
    fun fromEmailsList(emails: List<String>): String = adapter.toJson(emails)

    @TypeConverter
    @JvmStatic
    fun toEmailsList(json: String): List<String> = adapter.fromJson(json) ?: emptyList()
}
