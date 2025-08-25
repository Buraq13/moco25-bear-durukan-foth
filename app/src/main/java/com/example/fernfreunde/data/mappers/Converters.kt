package com.example.fernfreunde.data.mappers

import androidx.room.TypeConverter
import org.json.JSONArray

enum class SyncStatus { PENDING, SYNCED, FAILED }

class Converters {

    // ***************************************************************** //
    // String Converters                                                 //
    // ***************************************************************** //

    @TypeConverter
    fun stringToList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val arr = JSONArray(value)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list += arr.optString(i)
        return list
    }

    @TypeConverter
    fun listToString(value: List<String>?): String? {
        if (value == null) return null
        val arr = JSONArray()
        value.forEach { arr.put(it) }
        return arr.toString()
    }

    // ***************************************************************** //
    // SyncStatus <-> String Converters                                  //
    // ***************************************************************** //

    @TypeConverter
    fun stringToSyncStatus(value: String?): SyncStatus? =
        value?.let { SyncStatus.valueOf(it) }

    @TypeConverter
    fun syncStatusToString(status: SyncStatus?): String? = status?.name
}